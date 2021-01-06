package parser.p4.test

import java.io.{BufferedInputStream, BufferedOutputStream, FileOutputStream, PrintStream}
import java.util

import org.change.parser.p4._
import org.change.parser.p4.parser.{DFSState, StateExpander}
import org.change.utils.prettifier.JsonUtil
import org.change.v2.analysis.executor.{CodeAwareInstructionExecutor, DecoratedInstructionExecutor, OVSExecutor}
import org.change.v2.analysis.executor.solvers.Z3BVSolver
import org.change.v2.analysis.expression.concrete.ConstantValue
import org.change.v2.analysis.memory.{State, Tag}
import org.change.v2.analysis.processingmodels.instructions._
import org.change.v2.executor.clickabstractnetwork.ClickExecutionContext
import org.change.v2.p4.model.updated.instance.MetadataInstance
import org.change.v2.p4.model.{Switch, SwitchInstance}
import org.scalatest.FunSuite

import scala.collection.JavaConversions._
import org.change.parser.p4.P4PrettyPrinter._
import org.change.parser.p4.tables._
import org.change.parser.p4
import org.change.parser.p4.factories.FullTableFactory

class AliTests extends FunSuite {
  test("NDP") {
    val dir = "ali_inputs/ndp/"
    val p4 = s"$dir/ndp_router-ppc.p4"
    val dataplane = s"$dir/commands.txt"
    val res = ControlFlowInterpreter(p4, dataplane, Map[Int, String](1 -> "veth0", 2 -> "veth1"), "router")
    val port = 1
    val ib = InstructionBlock(
      Forward(s"router.input.$port")
    )
    val codeAwareInstructionExecutor = CodeAwareInstructionExecutor(res.instructions(), res.links(), solver = new Z3BVSolver)
    val (initial, _) = codeAwareInstructionExecutor.
      runToCompletion(InstructionBlock(res.allParserStatesInstruction()), State.clean, verbose = true)
    val (ok: List[State], failed: List[State]) = executeAndPrintStats(ib, initial, codeAwareInstructionExecutor)
    printResults(dir, port, ok, failed, "soso")
  }

}
