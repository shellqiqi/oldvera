package org.change.v2.runners.experiments.routerexperiments

import java.io.File

import org.change.v2.analysis.executor.CodeAwareInstructionExecutor
import org.change.v2.analysis.expression.concrete.ConstantValue
import org.change.v2.analysis.memory.State
import org.change.v2.analysis.memory.State.{ipSymb, start}
import org.change.v2.analysis.processingmodels.Instruction
import org.change.v2.analysis.processingmodels.instructions.{Assign, AssignRaw, Forward, InstructionBlock}


object BatfishAnalyzer {
  def main(args: Array[String]): Unit = {
    val fattree10 = new File("/home/radu/0/projects/batfish/containers/symnet/testrigs/fattree10/environments/env_default")
//    val model = fibFolderToSEFL(new File(fattree10))

    val fattree10Topo = new File("/home/radu/0/projects/batfish/containers/symnet/testrigs/fattree10/testrig_topology")

    val seflModel = batfishFibsToSEFL(fattree10, fattree10Topo)

    quickAndDirtyRun(seflModel)
  }

  def quickAndDirtyRun(sefl: (Map[String, Instruction], Map[String, String])): Unit = {
    val executor = CodeAwareInstructionExecutor(CodeAwareInstructionExecutor.flattenProgram(sefl._1, sefl._2))

    val r = executor.executeForward(Forward("edge-30-in"), symbolicIpState, v = true)

    println(s"Success: ${r._1.length}")

    for {
      state <- (r._1)
    } println(state.history.reverse.mkString(" - ") /* + state.errorCause.get.take(100) */)
  }


  import org.change.v2.util.conversion.RepresentationConversion._
  import org.change.v2.util.canonicalnames._
  lazy val symbolicIpState = InstructionBlock(
    start,
    ipSymb
    ,AssignRaw(IPDst, ConstantValue(ipToNumber("70.0.124.1")))
  )(State.clean, true)._1.head
}

