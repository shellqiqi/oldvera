package parser.p4.test

import org.change.parser.p4.ControlFlowInterpreter
import org.change.parser.p4.factories.SymbolicRegistersInitFactory
import org.change.parser.p4.parser.SwitchBasedParserGenerator
import org.change.parser.p4.tables.SymbolicSwitchInstance
import org.change.v2.analysis.executor.CodeAwareInstructionExecutor
import org.change.v2.analysis.executor.solvers.Z3BVSolver
import org.change.v2.analysis.memory.State
import org.change.v2.analysis.memory.TagExp.IntImprovements
import org.change.v2.analysis.processingmodels.instructions._
import org.change.v2.p4.model.Switch
import org.scalatest.FunSuite

class AliTests extends FunSuite {
  test("Simple Router") {
    val dir = "inputs/simple-router-testing/"
    val p4 = s"$dir/simple_router.p4"
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
    // CodeAwareInstructionExecutor.DEBUG = true // Set DEBUG to true to print SEFL instructions
    val (initial, _) = codeAwareInstructionExecutor.
      runToCompletion(InstructionBlock(res.allParserStatesInstruction()), State.clean, verbose = true)
    codeAwareInstructionExecutor.FAIL_STOP = true
    codeAwareInstructionExecutor.FAIL_FILTER = "Cannot resolve reference".r
    val (ok: List[State], failed: List[State]) = executeAndPrintStats(ib, initial, codeAwareInstructionExecutor)
    // printResults(dir, port, ok, failed, "ndp")
  }

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
    codeAwareInstructionExecutor.FAIL_STOP = true
    codeAwareInstructionExecutor.FAIL_FILTER = "Cannot resolve reference".r
    val (ok: List[State], failed: List[State]) = executeAndPrintStats(ib, initial, codeAwareInstructionExecutor)
    // printResults(dir, port, ok, failed, "ndp")
  }

  test("NetPaxos-acceptor") { // ERROR: Stack overflow
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
    // printResults(dir, port, ok, failed, "acceptor")
  }

  test("NetPaxos-acceptor-debug") {
    val dir = "ali_inputs/netpaxos/"
    val p4 = s"$dir/acceptor-ppc-cyq.p4" // Modified P4 file
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
    // CodeAwareInstructionExecutor.DEBUG = true // Set DEBUG to true to print SEFL instructions
    codeAwareInstructionExecutor.FAIL_STOP = true
    codeAwareInstructionExecutor.FAIL_FILTER = "Cannot resolve reference".r
    val (ok: List[State], failed: List[State]) = executeAndPrintStats(ib, initial, codeAwareInstructionExecutor)
    // printResults(dir, port, ok, failed, "acceptor")
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
    codeAwareInstructionExecutor.FAIL_STOP = true
    codeAwareInstructionExecutor.FAIL_FILTER = "Cannot resolve reference".r
    val (ok: List[State], failed: List[State]) = executeAndPrintStats(ib, initial, codeAwareInstructionExecutor)
    // printResults(dir, port, ok, failed, "coordinator")
  }

  test("NetChain") { // Failed
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
    // printResults(dir, port, ok, failed, "netchain")
  }

  test("NetChain-debug") { // Deparser loop
    val dir = "ali_inputs/netchain/"
    val p4 = s"$dir/netchain-ppc-cyq.p4"
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
    // CodeAwareInstructionExecutor.DEBUG = true // Set DEBUG to true to print SEFL instructions
    val (ok: List[State], failed: List[State]) = executeAndPrintStats(ib, initial, codeAwareInstructionExecutor)
    // printResults(dir, port, ok, failed, "netchain-debug")
  }

  test("NetCache") { // Failed because hash fields
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
    // printResults(dir, port, ok, failed, "netcache")
  }

  test("NetCache-nohash") {
    val dir = "ali_inputs/netcache/"
    val p4 = s"$dir/netcache-ppc-nohash.p4"
    val dataplane = s"$dir/commands-short.txt"
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
    codeAwareInstructionExecutor.FAIL_STOP = true
    codeAwareInstructionExecutor.FAIL_FILTER = "Cannot resolve reference".r
    val (ok: List[State], failed: List[State]) = executeAndPrintStats(ib, initial, codeAwareInstructionExecutor)
    // printResults(dir, port, ok, failed, "netcache-nohash")
  }

  test("Flowlet") { // Failed because hash field not supported
    val dir = "ali_inputs/flowlet/"
    val p4 = s"$dir/flowlet_switching-ppc.p4"
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
    // printResults(dir, port, ok, failed, "flowlet")
  }

  test("Flowlet-nohash") {
    val dir = "ali_inputs/flowlet/"
    val p4 = s"$dir/flowlet_switching-ppc-nohash.p4"
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
    codeAwareInstructionExecutor.FAIL_STOP = true
    codeAwareInstructionExecutor.FAIL_FILTER = "Cannot resolve reference".r
    val (ok: List[State], failed: List[State]) = executeAndPrintStats(ib, initial, codeAwareInstructionExecutor)
    // printResults(dir, port, ok, failed, "flowlet-nohash")
  }

  test("Switch-w-reg") { // too slow with registers
    val dir = "inputs/big-switch/"
    val p4 = s"$dir/switch-ppc-orig.p4"
    val dataplane = s"$dir/pd-L2Test.txt"
    val ifaces = Map[Int, String](
      0 -> "veth0", 1 -> "veth2",
      2 -> "veth4", 3 -> "veth6",
      4 -> "veth8", 5 -> "veth10",
      6 -> "veth12", 7 -> "veth14",
      8 -> "veth16", 64 -> "veth250"
    )
    val res = ControlFlowInterpreter(
      p4,
      dataplane,
      ifaces,
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
    // printResults(dir, port, ok, failed, "switch")
  }

  test("Switch-wo-reg") { // too slow without registers
    val dir = "inputs/big-switch/"
    val p4 = s"$dir/switch-ppc-orig.p4"
    val dataplane = s"$dir/pd-L2Test.txt"
    val ifaces = Map[Int, String](
      0 -> "veth0", 1 -> "veth2",
      2 -> "veth4", 3 -> "veth6",
      4 -> "veth8", 5 -> "veth10",
      6 -> "veth12", 7 -> "veth14",
      8 -> "veth16", 64 -> "veth250"
    )
    val res = ControlFlowInterpreter(
      p4,
      dataplane,
      ifaces,
      "router")
    val port = 1
    val ib = InstructionBlock(
      Forward(s"router.input.$port")
    )
    val codeAwareInstructionExecutor = CodeAwareInstructionExecutor(res.instructions(), res.links(), solver = new Z3BVSolver)
    val (initial, _) = codeAwareInstructionExecutor.
      runToCompletion(InstructionBlock(res.allParserStatesInstruction()), State.clean, verbose = true)
    val (ok: List[State], failed: List[State]) = executeAndPrintStats(ib, initial, codeAwareInstructionExecutor)
    // printResults(dir, port, ok, failed, "switch")
  }

  test("Switch-parser-sefl") { // without registers, with deterministic SEFL parser code
    val dir = "inputs/big-switch/"
    val p4 = s"$dir/switch-ppc-orig.p4"
    val dataplane = s"$dir/pd-L2Test.txt"
    val ifaces = Map[Int, String](
      0 -> "veth0", 1 -> "veth2",
      2 -> "veth4", 3 -> "veth6",
      4 -> "veth8", 5 -> "veth10",
      6 -> "veth12", 7 -> "veth14",
      8 -> "veth16", 64 -> "veth250"
    )
    val sw = Switch.fromFile(p4)
    val switchInstance = SymbolicSwitchInstance.fromFileWithSyms("router",
      ifaces,
      Map.empty,
      sw,
      dataplane)
    val port = 1
    val res = new ControlFlowInterpreter(switchInstance, switch = sw,
      optParserGenerator = Some(
        new SwitchBasedParserGenerator(switch = sw,
          switchInstance = switchInstance, codeFilter = Some((x : String) => {
            x.contains("parse_ethernet") &&
              x.contains("parse_ipv4") &&
              x.contains("parse_tcp")
          }))
      )
    )
    val ib = InstructionBlock(
      Forward(s"router.input.$port")
    )
    val codeAwareInstructionExecutor = CodeAwareInstructionExecutor(res.instructions(), res.links(), solver = new Z3BVSolver)
    val (initial, _) = codeAwareInstructionExecutor.
      runToCompletion(InstructionBlock(
        res.allParserStatesInstruction()
      ), State.clean, verbose = true)
    val (ok: List[State], failed: List[State]) = executeAndPrintStats(ib, initial, codeAwareInstructionExecutor)
    // printResults(dir, port, ok, failed, "switch")
  }

  test("Switch-parser-gen") { // without registers, with deterministic SEFL parser code and packet types
    val dir = "inputs/big-switch/"
    val p4 = s"$dir/switch-ppc-orig.p4"
    val dataplane = s"$dir/pd-L2Test.txt"
    val ifaces = Map[Int, String](
      0 -> "veth0", 1 -> "veth2",
      2 -> "veth4", 3 -> "veth6",
      4 -> "veth8", 5 -> "veth10",
      6 -> "veth12", 7 -> "veth14",
      8 -> "veth16", 64 -> "veth250"
    )
    val sw = Switch.fromFile(p4)
    val switchInstance = SymbolicSwitchInstance.fromFileWithSyms("router",
      ifaces,
      Map.empty,
      sw,
      dataplane)
    val port = 1
    val res = new ControlFlowInterpreter(switchInstance, switch = sw,
      optParserGenerator = Some(
        new SwitchBasedParserGenerator(switch = sw,
          switchInstance = switchInstance, codeFilter = Some((x : String) => {
            x.contains("parse_ethernet") &&
              x.contains("parse_ipv4") &&
              x.contains("parse_tcp")
          }))
      )
    )
    val ib = InstructionBlock(
      Forward(s"router.input.$port")
    )
    val codeAwareInstructionExecutor = CodeAwareInstructionExecutor(res.instructions(), res.links(), solver = new Z3BVSolver)
    val (initial, _) = codeAwareInstructionExecutor.
      runToCompletion(InstructionBlock(
        CreateTag("START", 0),
        Call("router.generator.parse_ethernet.parse_ipv4.parse_tcp")
      ), State.clean, verbose = true)
    codeAwareInstructionExecutor.FAIL_STOP = true
    codeAwareInstructionExecutor.FAIL_FILTER = "Cannot resolve reference".r
    val (ok: List[State], failed: List[State]) = executeAndPrintStats(ib, initial, codeAwareInstructionExecutor)
    // printResults(dir, port, ok, failed, "switch")
  }
}
