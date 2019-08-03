package org.change.v2.verification

import java.io.PrintStream

import org.change.parser.p4.ControlFlowInterpreter
import org.change.parser.p4.factories.SymbolicRegistersInitFactory
import org.change.utils.prettifier.JsonUtil
import org.change.v2.analysis.executor.CodeAwareInstructionExecutor
import org.change.v2.analysis.executor.solvers.Z3BVSolver
import org.change.v2.analysis.memory.State
import org.change.v2.analysis.processingmodels.instructions._

import java.io.{BufferedOutputStream, File, FileOutputStream}

import org.change.v2.analysis.processingmodels.Instruction

/**
  * Created by mateipopovici on 14/12/17.
  */
object P4Tester {

}