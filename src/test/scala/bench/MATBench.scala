package bench

import java.io.File

import org.change.v2.abstractnet.mat.tree.Node
import org.change.v2.abstractnet.mat.condition.{Range => MATRange}
import org.change.v2.abstractnet.optimized.router.OptimizedRouter
import org.change.v2.analysis.constraint.{Constraint, EQ_E, GTE_E, GT_E, LTE_E}
import org.change.v2.analysis.executor.CodeAwareInstructionExecutor
import org.change.v2.analysis.executor.solvers.Z3Solver
import org.change.v2.analysis.expression.concrete.{ConstantValue, SymbolicValue}
import org.change.v2.analysis.memory.State
import org.change.v2.analysis.processingmodels.{Instruction, instructions}
import org.change.v2.analysis.processingmodels.instructions._
import org.change.v3.parsers.OpenFlowRuleParser.MatchRule
import org.change.v3.parsers.{MatchValue, OpenFlowRuleParser, SingleValue, ValueRange}
import org.scalameter.api._
import org.scalameter.picklers.noPickler._

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object MATBench extends Bench.LocalTime {

  val path = "src/main/resources/routing_tables/"
  val files = Gen.enumeration("file")("small.txt", "medium.txt")

  val masks = for {
    file <- files
    completePath = path + file
    masks = OptimizedRouter.getRoutingEntries(new File(completePath))
    sortedMasks = masks.map(_._1).map(interval => MATRange(interval._1, interval._2)).sortBy(- _.generality)
  } yield sortedMasks

  performance of "MAT Tree" in {
    measure method "construct" in {
      using(masks) config(exec.benchRuns -> 3) in {
        maskSet => Node.makeForest(maskSet)
          println("done")
      }
    }
  }
}

object OpenFlowMATBenchs {
  def main(args: Array[String]): Unit = {
    for {
      openFlowTableFile <- new File("src/main/resources/openflow_rules").listFiles()
      parsedOpenFlowEntries = OpenFlowRuleParser.parse(openFlowTableFile.getAbsolutePath)
    } {
      import scala.collection.mutable.{Map => MutableMap}
      val fieldToEntriesMap = MutableMap[String, ArrayBuffer[MATRange]]()
      for {
        openFlowEntry <- parsedOpenFlowEntries
        (field, value) <- openFlowEntry
      } fieldToEntriesMap.put(field, fieldToEntriesMap.getOrElse(field, ArrayBuffer.empty).+=(value match {
        case SingleValue(v) => MATRange(v,v)
        case ValueRange(l,u) => MATRange(l,u)
      }))

      println(s"File is $openFlowTableFile, size is ${parsedOpenFlowEntries.length}")
      for {
        (fieldName, ranges) <- fieldToEntriesMap
      } {
        import org.scalameter._
        val time = measure {
          Node.makeForest(ranges)
        }
        println(s"Total time for field $fieldName: $time")
      }
    }
  }

  def matchValueToConstraint(mv: MatchValue): FloatingConstraint = mv match {
    case ValueRange(l, u) => :&:(
      GTE_E(ConstantValue(l)),
      LTE_E(ConstantValue(u))
    )
    case SingleValue(v) => EQ_E(ConstantValue(v))
  }

  def matchRuleToIfElseSEFL(mr: MatchRule, ifInstruction: Instruction = NoOp, elseInstruction: Instruction = NoOp): Instruction =
    mr.foldRight(ifInstruction) {
      (crtCondition, rest) => If(Constrain(crtCondition._1, matchValueToConstraint(crtCondition._2)),
        rest,
        elseInstruction
      )
    }

  def matchRuleToForkSEFL(mr: MatchRule, ifInstruction: Instruction = NoOp) = InstructionBlock({
    val constraintInstructions: Seq[Instruction] = mr.map {
      crtCondition => {
        Constrain(crtCondition._1, matchValueToConstraint(crtCondition._2))
      }
    }.toSeq

    constraintInstructions.:+(ifInstruction)
  })

  def matchRuleSetToIfElseSEFL(mrs: Seq[MatchRule], withSymbolAllocation: Boolean  = false): Instruction = {
    val conditionVariableNames = mrs.flatMap(_.keySet).toSet

    val ifElseInstructionChain = mrs.foldRight(NoOp: Instruction) { (crtRule, elseInstruction) => {
        matchRuleToIfElseSEFL(crtRule, ifInstruction = NoOp, elseInstruction = elseInstruction)
      }
    }

    if (withSymbolAllocation)
      prependSymbolAllocation(conditionVariableNames.toSet, ifElseInstructionChain)
    else
      ifElseInstructionChain
  }

  private val rander = new Random()

  def allocateSymbols(symbolNames: Set[String]): Seq[Instruction] =
    symbolNames.toSeq.flatMap(name => List(AllocateSymbol(name), AssignNamedSymbol(name, ConstantValue(rander.nextInt()))))

  def matchRuleSetToForkSEFL(mrs: Seq[MatchRule], withSymbolAllocation: Boolean  = false): Instruction = {
    val conditionVariableNames = mrs.flatMap(_.keySet).toSet

    val fork = Fork(mrs.map (matchRuleToForkSEFL(_, NoOp)))

    if (withSymbolAllocation) prependSymbolAllocation(conditionVariableNames, fork) else fork
  }

  def matchRuleSetToGroupingForkSEFL(
                                      mrs: Seq[MatchRule],
                                      groupingFactor: Int = 1,
                                      withSymbolAllocation: Boolean  = false
                                    ): Instruction = {
    val conditionVariableNames = mrs.flatMap(_.keySet).toSet

    val forkBranches = for {
      crtGroup <- mrs.grouped(groupingFactor)
      byFiledName = crtGroup.take(groupingFactor).flatMap(_.toIterable).groupBy(_._1)
    } yield InstructionBlock {
        byFiledName.map { variablePlusConditions => {
          Constrain(variablePlusConditions._1, :|:(variablePlusConditions._2.map(v => matchValueToConstraint(v._2)).toList))
        }
        // After the constrain sequence, the action should go here.
      }
    }

    val fork = Fork(forkBranches.toIterable)
    if (withSymbolAllocation) prependSymbolAllocation(conditionVariableNames, fork) else fork
  }

  def getMeasurerRuleSet(measurer: Seq[MatchRule] => Unit): String => Unit =
    measurer /*compose { x: Seq[MatchRule] => x.take(20) } */ compose OpenFlowRuleParser.parse

  def measureIfElseSEFL(mrs: Seq[MatchRule]): Unit = {
    measureSEFL(matchRuleSetToIfElseSEFL(mrs, withSymbolAllocation = true))
  }

  def measureGroupingFork(groupingFactor: Int)(mrs: Seq[MatchRule]): Unit = {
    measureSEFL(matchRuleSetToGroupingForkSEFL(mrs, groupingFactor = groupingFactor, withSymbolAllocation = true))
  }

  def measureForkSEFL(mrs: Seq[MatchRule]): Unit = {
    measureSEFL(matchRuleSetToForkSEFL(mrs, withSymbolAllocation = true))
  }

  def measureSEFL(instruction: Instruction): Unit = {
    val executor = new CodeAwareInstructionExecutor(Map("" -> instruction), new Z3Solver())

    import org.scalameter._
    val time = measure {
      val r = executor.executeForward(Forward(""), State.clean)
      println(s"Found ${r._1.length} successful and ${r._2.length} failed.")
    }
    println(s"Total execution time was $time")
  }

  def prependSymbolAllocation(symbols: Set[String], rest: Instruction): Instruction =
    InstructionBlock {
      allocateSymbols(symbols).:+(rest)
    }
}

object RunMeasure {
  def main(args: Array[String]): Unit = {
    import bench.OpenFlowMATBenchs._
    val f = "src/main/resources/openflow_rules/of-classbench-10000.of"
//    getMeasurerRuleSet(measureForkSEFL)(f)
//    getMeasurerRuleSet(measureIfElseSEFL)(f)
    for {
      i <- 0 to Math.log(2000).toInt
      gf = Math.pow(2,i).toInt
    } {
      println(s"Grouping factor is $gf")
      getMeasurerRuleSet(measureGroupingFork(gf))(f)
    }
  }
}

/**
  * Grouping factor is 1
  * Found 2275 successful and 0 failed.
  * Total execution time was 40870.426196 ms
  * Grouping factor is 2
  * Found 1138 successful and 0 failed.
  * Total execution time was 25807.145434 ms
  * Grouping factor is 4
  * Found 569 successful and 0 failed.
  * Total execution time was 14166.305767 ms
  * Grouping factor is 8
  * Found 285 successful and 0 failed.
  * Total execution time was 7819.066127 ms
  * Grouping factor is 16
  * Found 143 successful and 0 failed.
  * Total execution time was 4687.729818 ms
  * Grouping factor is 32
  * Found 72 successful and 0 failed.
  * Total execution time was 2677.167867 ms
  * Grouping factor is 64
  * Found 36 successful and 0 failed.
  * Total execution time was 1658.142943 ms
  * Grouping factor is 128
  * Found 18 successful and 0 failed.
  * Total execution time was 970.821281 ms
  *
  * Constant packet, fields set to 1000
  *
  * Grouping factor is 1
  * Found 0 successful and 0 failed.
  * Total execution time was 286.468828 ms
  * Grouping factor is 2
  * Found 0 successful and 0 failed.
  * Total execution time was 4469.276481 ms
  * Grouping factor is 4
  * Found 0 successful and 0 failed.
  * Total execution time was 2220.540831 ms
  * Grouping factor is 8
  * Found 0 successful and 0 failed.
  * Total execution time was 1145.014971 ms
  * Grouping factor is 16
  * Found 0 successful and 0 failed.
  * Total execution time was 589.025815 ms
  * Grouping factor is 32
  * Found 0 successful and 0 failed.
  * Total execution time was 299.081117 ms
  * Grouping factor is 64
  * Found 0 successful and 0 failed.
  * Total execution time was 163.998172 ms
  * Grouping factor is 128
  * Found 0 successful and 0 failed.
  * Total execution time was 107.916304 ms
  *
  * Constant packet, fields set to random int
  *
  * Grouping factor is 1
  * Found 0 successful and 0 failed.
  * Total execution time was 376.697122 ms
  * Grouping factor is 2
  * Found 0 successful and 0 failed.
  * Total execution time was 4375.455706 ms
  * Grouping factor is 4
  * Found 0 successful and 0 failed.
  * Total execution time was 2155.003284 ms
  * Grouping factor is 8
  * Found 0 successful and 0 failed.
  * Total execution time was 1103.464227 ms
  * Grouping factor is 16
  * Found 0 successful and 0 failed.
  * Total execution time was 562.230139 ms
  * Grouping factor is 32
  * Found 0 successful and 0 failed.
  * Total execution time was 289.679838 ms
  * Grouping factor is 64
  * Found 0 successful and 0 failed.
  * Total execution time was 162.373852 ms
  * Grouping factor is 128
  * Found 0 successful and 0 failed.
  * Total execution time was 97.058297 ms
  */