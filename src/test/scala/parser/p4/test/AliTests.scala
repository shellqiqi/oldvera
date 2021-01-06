package parser.p4.test

import com.sun.org.apache.xml.internal.utils.StringToIntTable

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
import org.change.parser.p4.factories.{FullTableFactory, SymbolicRegistersInitFactory}

class AliTests extends FunSuite {
  test("NDP") {
    val dir = "ali_inputs/ndp/"
    val p4 = s"$dir/ndp_router-ppc.p4"
    val dataplane = s"$dir/commands.txt"
    val res = ControlFlowInterpreter(
      p4,
      dataplane,
      Map[Int, String](1 -> "veth0", 2 -> "veth1"),
      "router",
      optAdditionalInitCode = Some((x, y) => {
        new SymbolicRegistersInitFactory(x).initCode()
      }))
    val port = 1
    val ib = InstructionBlock(
      Forward(s"router.input.$port")
    )
    val codeAwareInstructionExecutor = CodeAwareInstructionExecutor(res.instructions(), res.links(), solver = new Z3BVSolver)
    val (initial, _) = codeAwareInstructionExecutor.
      runToCompletion(InstructionBlock(res.allParserStatesInstruction()), State.clean, verbose = true)
    val (ok: List[State], failed: List[State]) = executeAndPrintStats(ib, initial, codeAwareInstructionExecutor)
    printResults(dir, port, ok, failed, "ndp")
  }

  test("NetPaxos-acceptor") {
    val dir = "ali_inputs/netpaxos/"
    val p4 = s"$dir/acceptor-ppc.p4"
    val dataplane = s"$dir/acceptor-commands.txt"
    val res = ControlFlowInterpreter(
      p4,
      dataplane,
      Map[Int, String](1 -> "veth0", 2 -> "veth1"),
      "router",
      optAdditionalInitCode = Some((x, y) => {
        new SymbolicRegistersInitFactory(x).initCode()
      }))
    val port = 1
    val ib = InstructionBlock(
      Forward(s"router.input.$port")
    )
    val codeAwareInstructionExecutor = CodeAwareInstructionExecutor(res.instructions(), res.links(), solver = new Z3BVSolver)
    val (initial, _) = codeAwareInstructionExecutor.
      runToCompletion(InstructionBlock(res.allParserStatesInstruction()), State.clean, verbose = true)
    val (ok: List[State], failed: List[State]) = executeAndPrintStats(ib, initial, codeAwareInstructionExecutor)
    printResults(dir, port, ok, failed, "acceptor")
  }

  test("NetPaxos-coordinator") {
    val dir = "ali_inputs/netpaxos/"
    val p4 = s"$dir/coordinator-ppc.p4"
    val dataplane = s"$dir/coordinator-commands.txt"
    val res = ControlFlowInterpreter(
      p4,
      dataplane,
      Map[Int, String](1 -> "veth0", 2 -> "veth1"),
      "router",
      optAdditionalInitCode = Some((x, y) => {
        new SymbolicRegistersInitFactory(x).initCode()
      }))
    val port = 1
    val ib = InstructionBlock(
      Forward(s"router.input.$port")
    )
    val codeAwareInstructionExecutor = CodeAwareInstructionExecutor(res.instructions(), res.links(), solver = new Z3BVSolver)
    val (initial, _) = codeAwareInstructionExecutor.
      runToCompletion(InstructionBlock(res.allParserStatesInstruction()), State.clean, verbose = true)
    val (ok: List[State], failed: List[State]) = executeAndPrintStats(ib, initial, codeAwareInstructionExecutor)
    printResults(dir, port, ok, failed, "coordinator")
  }

  test("NetChain") {
    val dir = "ali_inputs/netchain/"
    val p4 = s"$dir/netchain-ppc.p4"
    val dataplane = s"$dir/commands_3.txt"
    val res = ControlFlowInterpreter(
      p4,
      dataplane,
      Map[Int, String](1 -> "veth0", 2 -> "veth1"),
      "router",
      optAdditionalInitCode = Some((x, y) => {
        new SymbolicRegistersInitFactory(x).initCode()
      }))
    val port = 1
    val ib = InstructionBlock(
      Forward(s"router.input.$port")
    )
    val codeAwareInstructionExecutor = CodeAwareInstructionExecutor(res.instructions(), res.links(), solver = new Z3BVSolver)
    val (initial, _) = codeAwareInstructionExecutor.
      runToCompletion(InstructionBlock(res.allParserStatesInstruction()), State.clean, verbose = true)
    val (ok: List[State], failed: List[State]) = executeAndPrintStats(ib, initial, codeAwareInstructionExecutor)
    printResults(dir, port, ok, failed, "netchain")
  }

  test("NetCache") {
    val dir = "ali_inputs/netcache/"
    val p4 = s"$dir/netcache-ppc.p4"
    val dataplane = s"$dir/commands.txt"
    val res = ControlFlowInterpreter(
      p4,
      dataplane,
      Map[Int, String](1 -> "veth0", 2 -> "veth1"),
      "router",
      optAdditionalInitCode = Some((x, y) => {
        new SymbolicRegistersInitFactory(x).initCode()
      }))
    val port = 1
    val ib = InstructionBlock(
      Forward(s"router.input.$port")
    )
    val codeAwareInstructionExecutor = CodeAwareInstructionExecutor(res.instructions(), res.links(), solver = new Z3BVSolver)
    val (initial, _) = codeAwareInstructionExecutor.
      runToCompletion(InstructionBlock(res.allParserStatesInstruction()), State.clean, verbose = true)
    val (ok: List[State], failed: List[State]) = executeAndPrintStats(ib, initial, codeAwareInstructionExecutor)
    printResults(dir, port, ok, failed, "netcache")
  }
}
