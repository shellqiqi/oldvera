package bench

import java.io.File

import bench.MATBench.{masks, using}
import org.change.v2.abstractnet.mat.tree.Node
import org.change.v2.abstractnet.mat.condition.{Range => MATRange}
import org.change.v2.abstractnet.optimized.router.OptimizedRouter
import org.change.v3.parsers.{OpenFlowRuleParser, SingleValue, ValueRange}
import org.scalameter.api._
import org.scalameter.picklers.noPickler._

import scala.collection.mutable.ArrayBuffer

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
}