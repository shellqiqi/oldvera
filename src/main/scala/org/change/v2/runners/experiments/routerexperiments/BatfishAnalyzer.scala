package org.change.v2.runners.experiments.routerexperiments

import java.io.File

import org.change.v2.analysis.executor.CodeAwareInstructionExecutor
import org.change.v2.analysis.memory.State
import org.change.v2.analysis.memory.State.{ipSymb, start}
import org.change.v2.analysis.processingmodels.Instruction
import org.change.v2.analysis.processingmodels.instructions.{Forward, InstructionBlock}


object BatfishAnalyzer {
  def main(args: Array[String]): Unit = {
    val fattree10 = new File("/home/radu/0/projects/batfish/containers/symnet/testrigs/fattree10/environments/env_default")
//    val model = fibFolderToSEFL(new File(fattree10))

    val fattree10Topo = new File("/home/radu/0/projects/batfish/containers/symnet/testrigs/fattree10/testrig_topology")

    quickAndDirtyRun(batfishFibsToSEFL(fattree10, fattree10Topo))
  }

  def quickAndDirtyRun(sefl: (Map[String, Instruction], Map[String, String])): Unit = {
    val executor = CodeAwareInstructionExecutor(CodeAwareInstructionExecutor.flattenProgram(sefl._1, sefl._2))

    val r = executor.executeForward(Forward(sefl._1.keys.head), symbolicIpState, v = true)

    println(r._1.length)
  }

  lazy val symbolicIpState = InstructionBlock(
    start,
    ipSymb
  )(State.clean, true)._1.head
}

