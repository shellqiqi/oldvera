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

  def executeAndPrintStats(ib: Instruction, initial: List[State], codeAwareInstructionExecutor : CodeAwareInstructionExecutor): (List[State], List[State]) = {
    val init = System.currentTimeMillis()
    println("Ok now " + initial.size)
    val (ok, failed) = initial.foldLeft((Nil, Nil): (List[State], List[State]))((acc, init) => {
      var first = ib
      var crt = init
      var continue = true
      var o = List.empty[State]
      var f = List.empty[State]
      while (continue) {
        val (o1, f1, c) = codeAwareInstructionExecutor.run(first, crt, verbose = true)
        o = o1 ++ o
        f = f1 ++ f
        continue = c
        if (continue) {
          val (x, y) = codeAwareInstructionExecutor.pop().get
          crt = x
          first = y
          System.out.println("now running " + crt.history.head)
        }
      }
      (acc._1 ++ o, acc._2 ++ f)
    })
    println(s"Failed # ${failed.size}, Ok # ${ok.size}")
    println(s"Time is ${System.currentTimeMillis() - init}ms")
    (ok, failed)
  }

  def printResults(dir: String, port: Int, ok: List[State], failed: List[State], okBase: String): Unit = {
    val psok = new BufferedOutputStream(new FileOutputStream(s"$dir/ok-port$port-$okBase.json"))
    JsonUtil.toJson(ok, psok)
    psok.close()
    val relevant = failed
    val psko = new BufferedOutputStream(new FileOutputStream(s"$dir/fail-port$port-$okBase.json"))
    JsonUtil.toJson(relevant, psko)
    psko.close()

    import org.change.v2.analysis.memory.jsonformatters.StateToJson._
    import spray.json._
    val psokpretty = new PrintStream(s"$dir/ok-port$port-pretty-$okBase.json")
    psokpretty.println(ok.toJson(JsonWriter.func2Writer[List[State]](u => {
      JsArray(u.map(_.toJson).toVector)
    })).prettyPrint)
    psokpretty.close()


    val pskopretty = new PrintStream(s"$dir/fail-port$port-pretty-$okBase.json")
    pskopretty.println(relevant.toJson(JsonWriter.func2Writer[List[State]](u => {
      JsArray(u.map(_.toJson).toVector)
    })).prettyPrint)
    pskopretty.close()
  }

  lazy val output = new PrintStream(new FileOutputStream(new File("sefl.output")))

  def main(args: Array[String]): Unit = {
    println("=== Stuck states ===")
    val dir = "inputs/register"
    val p4 = s"$dir/register.p4"
    val dataplane = s"$dir/commands.txt"

    val res = ControlFlowInterpreter(p4, dataplane, Map[Int, String](1 -> "veth0", 2 -> "veth1", 11 -> "cpu"), "router",
      optAdditionalInitCode = Some((x, y) => {
        new SymbolicRegistersInitFactory(x).initCode()
      }))
    //res.instructions() foreach {case (port, i) => println (port + "-->" + Policy.show(i))}
    //print(res.instructions())
    val port = 1
    val ib = InstructionBlock(
      Forward(s"router.input.$port")
    )
    val codeAwareInstructionExecutor = CodeAwareInstructionExecutor(res.instructions(), res.links(), solver = new Z3BVSolver)
    val (initial, _) = codeAwareInstructionExecutor.
      execute(InstructionBlock(
        res.allParserStatesInstruction()
      ), State.clean, verbose = true)
    val (ok: List[State], failed: List[State]) = executeAndPrintStats(ib, initial, codeAwareInstructionExecutor)

    printResults(dir, port, ok, failed, "soso")
  }

  // val code = InstructionBlock(
  //   // At address 0 the L3 header starts
  //   CreateTag("L3HeaderStart", 0),
  //   // Also mark IP Src and IP Dst fields and allocate memory
  //   CreateTag("IPSrc", Tag("L3HeaderStart") + 96),
  //   // For raw memory access (via tags or ints), space has to be allocated beforehand.
  //   Allocate(Tag("IPSrc"), 32),
  //   CreateTag("IPDst", Tag("L3HeaderStart") + 128),
  //   Allocate(Tag("IPDst"), 32),


  //   //Initialize IPSrc and IPDst
  //   Assign(Tag("IPSrc"), ConstantValue(ipToNumber("127.0.0.1"))),
  //   Assign(Tag("IPDst"), SymbolicValue()),

  //   // If destination is 8.8.8.8, rewrite the Src address and forward it
  //   // otherwise, drop it
  //   If(Constrain(Tag("IPDst"), :==:(ConstantValue(ipToNumber("8.8.8.8")))),
  //     Assign(Tag("IPSrc"), SymbolicValue()),
  //     Fail("Packet dropped")
  //   )
  // )
}