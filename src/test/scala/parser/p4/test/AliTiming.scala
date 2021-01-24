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

class AliTiming extends FunSuite {
  test("SimpleRouter") {
    val startTime = System.currentTimeMillis()
    val dir = "inputs/simple-router-testing/"
    val p4 = s"$dir/simple_router.p4"
    val dataplane = s"$dir/commands.txt"
    val res = ControlFlowInterpreter(
      p4,
      dataplane,
      Map[Int, String](1 -> "veth0", 2 -> "veth1"),
      "router",
      optAdditionalInitCode = Some((x, _) => {
        new SymbolicRegistersInitFactory(x).initCode()
      }))
    val port = 1
    val ib = InstructionBlock(
      Forward(s"router.input.$port")
    )
    val codeAwareInstructionExecutor = CodeAwareInstructionExecutor(res.instructions(), res.links(), solver = new Z3BVSolver)
    val (initial, _) = codeAwareInstructionExecutor.runToCompletion(InstructionBlock(
      res.allParserStatesInstruction()
    ), State.clean, verbose = true)
    codeAwareInstructionExecutor.FAIL_STOP = false
    codeAwareInstructionExecutor.FAIL_FILTER = "Cannot resolve reference".r
    executeSilently(ib, initial, codeAwareInstructionExecutor)
    println(s"${System.currentTimeMillis() - startTime}")
  }

  test("NDP") {
    val startTime = System.currentTimeMillis()
    val dir = "ali_inputs/ndp/"
    val p4 = s"$dir/ndp_router-ppc.p4"
    val dataplane = s"$dir/commands.txt"
    val res = ControlFlowInterpreter(
      p4,
      dataplane,
      Map[Int, String](1 -> "veth0", 2 -> "veth1"),
      "router",
      optAdditionalInitCode = Some((x, _) => {
        new SymbolicRegistersInitFactory(x).initCode()
      }))
    val port = 1
    val ib = InstructionBlock(
      Forward(s"router.input.$port")
    )
    val codeAwareInstructionExecutor = CodeAwareInstructionExecutor(res.instructions(), res.links(), solver = new Z3BVSolver)
    val (initial, _) = codeAwareInstructionExecutor.runToCompletion(InstructionBlock(
      res.allParserStatesInstruction()
    ), State.clean, verbose = true)
    codeAwareInstructionExecutor.FAIL_STOP = false
    codeAwareInstructionExecutor.FAIL_FILTER = "Cannot resolve reference".r
    executeSilently(ib, initial, codeAwareInstructionExecutor)
    println(s"${System.currentTimeMillis() - startTime}")
  }

  test("NetPaxosAcceptor") {
    val startTime = System.currentTimeMillis()
    val dir = "ali_inputs/netpaxos/"
    val p4 = s"$dir/acceptor-ppc-cyq.p4"
    val dataplane = s"$dir/acceptor-commands.txt"
    val res = ControlFlowInterpreter(
      p4,
      dataplane,
      Map[Int, String](1 -> "veth0", 2 -> "veth1"),
      "router",
      optAdditionalInitCode = Some((x, _) => {
        new SymbolicRegistersInitFactory(x).initCode()
      }))
    val port = 1
    val ib = InstructionBlock(
      Forward(s"router.input.$port")
    )
    val codeAwareInstructionExecutor = CodeAwareInstructionExecutor(res.instructions(), res.links(), solver = new Z3BVSolver)
    val (initial, _) = codeAwareInstructionExecutor.runToCompletion(InstructionBlock(
      res.allParserStatesInstruction()
    ), State.clean, verbose = true)
    codeAwareInstructionExecutor.FAIL_STOP = false
    codeAwareInstructionExecutor.FAIL_FILTER = "Cannot resolve reference".r
    executeSilently(ib, initial, codeAwareInstructionExecutor)
    println(s"${System.currentTimeMillis() - startTime}")
  }

  test("NetPaxosCoordinator") {
    val startTime = System.currentTimeMillis()
    val dir = "ali_inputs/netpaxos/"
    val p4 = s"$dir/coordinator-ppc.p4"
    val dataplane = s"$dir/coordinator-commands.txt"
    val res = ControlFlowInterpreter(
      p4,
      dataplane,
      Map[Int, String](1 -> "veth0", 2 -> "veth1"),
      "router",
      optAdditionalInitCode = Some((x, _) => {
        new SymbolicRegistersInitFactory(x).initCode()
      }))
    val port = 1
    val ib = InstructionBlock(
      Forward(s"router.input.$port")
    )
    val codeAwareInstructionExecutor = CodeAwareInstructionExecutor(res.instructions(), res.links(), solver = new Z3BVSolver)
    val (initial, _) = codeAwareInstructionExecutor.runToCompletion(InstructionBlock(
      res.allParserStatesInstruction()
    ), State.clean, verbose = true)
    codeAwareInstructionExecutor.FAIL_STOP = false
    codeAwareInstructionExecutor.FAIL_FILTER = "Cannot resolve reference".r
    executeSilently(ib, initial, codeAwareInstructionExecutor)
    println(s"${System.currentTimeMillis() - startTime}")
  }

  test("NetCache") {
    val startTime = System.currentTimeMillis()
    val dir = "ali_inputs/netcache/"
    val p4 = s"$dir/netcache-ppc-nohash.p4"
    val dataplane = s"$dir/commands-short.txt"
    val res = ControlFlowInterpreter(
      p4,
      dataplane,
      Map[Int, String](1 -> "veth0", 2 -> "veth1"),
      "router",
      optAdditionalInitCode = Some((x, _) => {
        new SymbolicRegistersInitFactory(x).initCode()
      }))
    val port = 1
    val ib = InstructionBlock(
      Forward(s"router.input.$port")
    )
    val codeAwareInstructionExecutor = CodeAwareInstructionExecutor(res.instructions(), res.links(), solver = new Z3BVSolver)
    val (initial, _) = codeAwareInstructionExecutor.runToCompletion(InstructionBlock(
      res.allParserStatesInstruction()
    ), State.clean, verbose = true)
    codeAwareInstructionExecutor.FAIL_STOP = false
    codeAwareInstructionExecutor.FAIL_FILTER = "Cannot resolve reference".r
    executeSilently(ib, initial, codeAwareInstructionExecutor)
    println(s"${System.currentTimeMillis() - startTime}")
  }

  test("Flowlet") {
    val startTime = System.currentTimeMillis()
    val dir = "ali_inputs/flowlet/"
    val p4 = s"$dir/flowlet_switching-ppc-nohash.p4"
    val dataplane = s"$dir/commands.txt"
    val res = ControlFlowInterpreter(
      p4,
      dataplane,
      Map[Int, String](1 -> "veth0", 2 -> "veth1"),
      "router",
      optAdditionalInitCode = Some((x, _) => {
        new SymbolicRegistersInitFactory(x).initCode()
      }))
    val port = 1
    val ib = InstructionBlock(
      Forward(s"router.input.$port")
    )
    val codeAwareInstructionExecutor = CodeAwareInstructionExecutor(res.instructions(), res.links(), solver = new Z3BVSolver)
    val (initial, _) = codeAwareInstructionExecutor.runToCompletion(InstructionBlock(
      res.allParserStatesInstruction()
    ), State.clean, verbose = true)
    codeAwareInstructionExecutor.FAIL_STOP = false
    codeAwareInstructionExecutor.FAIL_FILTER = "Cannot resolve reference".r
    executeSilently(ib, initial, codeAwareInstructionExecutor)
    println(s"${System.currentTimeMillis() - startTime}")
  }

  test("SwitchNoINT") {
    val startTime = System.currentTimeMillis()
    val dir = "inputs/big-switch/"
    val p4 = s"$dir/switch-noint.p4"
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
    val (initial, _) = codeAwareInstructionExecutor.runToCompletion(InstructionBlock(
      res.allParserStatesInstruction()
    ), State.clean, verbose = true)
    codeAwareInstructionExecutor.FAIL_STOP = true
    codeAwareInstructionExecutor.FAIL_FILTER = "Cannot resolve reference".r
    executeSilently(ib, initial, codeAwareInstructionExecutor)
    println(s"${System.currentTimeMillis() - startTime}")
  }

  test("SwitchINT") {
    val startTime = System.currentTimeMillis()
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
    val (initial, _) = codeAwareInstructionExecutor.runToCompletion(InstructionBlock(
      res.allParserStatesInstruction()
    ), State.clean, verbose = true)
    codeAwareInstructionExecutor.FAIL_STOP = true
    codeAwareInstructionExecutor.FAIL_FILTER = "Cannot resolve reference".r
    executeSilently(ib, initial, codeAwareInstructionExecutor)
    println(s"${System.currentTimeMillis() - startTime}")
  }

  test("SwitchNoINT-NoStop") {
    val startTime = System.currentTimeMillis()
    val dir = "inputs/big-switch/"
    val p4 = s"$dir/switch-noint.p4"
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
    val (initial, _) = codeAwareInstructionExecutor.runToCompletion(InstructionBlock(
      res.allParserStatesInstruction()
    ), State.clean, verbose = true)
    codeAwareInstructionExecutor.FAIL_STOP = false
    codeAwareInstructionExecutor.FAIL_FILTER = "Cannot resolve reference".r
    executeSilently(ib, initial, codeAwareInstructionExecutor)
    println(s"${System.currentTimeMillis() - startTime}")
  }

  test("SwitchINT-NoStop") {
    val startTime = System.currentTimeMillis()
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
    val (initial, _) = codeAwareInstructionExecutor.runToCompletion(InstructionBlock(
      res.allParserStatesInstruction()
    ), State.clean, verbose = true)
    codeAwareInstructionExecutor.FAIL_STOP = false
    codeAwareInstructionExecutor.FAIL_FILTER = "Cannot resolve reference".r
    executeSilently(ib, initial, codeAwareInstructionExecutor)
    println(s"${System.currentTimeMillis() - startTime}")
  }
}

