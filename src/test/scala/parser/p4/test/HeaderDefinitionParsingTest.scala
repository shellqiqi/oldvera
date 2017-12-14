package parser.p4.test

import java.io.{BufferedInputStream, BufferedOutputStream, FileOutputStream, PrintStream}
import java.util

import org.change.parser.p4._
import org.change.parser.p4.parser.{DFSState, StateExpander}
import org.change.utils.prettifier.JsonUtil
import org.change.v2.analysis.executor.{DecoratedInstructionExecutor, OVSExecutor}
import org.change.v2.analysis.executor.solvers.Z3BVSolver
import org.change.v2.analysis.expression.concrete.ConstantValue
import org.change.v2.analysis.memory.{State, Tag}
import org.change.v2.analysis.processingmodels.instructions._
import org.change.v2.executor.clickabstractnetwork.ClickExecutionContext
import org.change.v2.p4.model.updated.instance.MetadataInstance
import org.change.v2.p4.model.updated.program.P4Program
import org.change.v2.p4.model.{Switch, SwitchInstance}
import org.scalatest.FunSuite

import scala.collection.JavaConversions._

class HeaderDefinitionParsingTest extends FunSuite {

  test("vlan_t example is parsed correctly") {
    val p4 = "src/main/resources/p4s/tests/vlan_t.p4"
    val res = P4ParserRunner.parse(p4)

    assert(res.declaredHeaders.size == 1)

    val vlantHeader = res.declaredHeaders.head._2
    assert(vlantHeader.length == 32)
    assert(
      Seq("pcp", "cfi", "vid", "ethertype").forall(f => vlantHeader.fields.values.exists(f == _._1))
    )
  }

  test("local_metadata example is parsed correctly") {
    val p4 = "src/main/resources/p4s/tests/local_metadata.p4"
    val res = P4ParserRunner.parse(p4)

    assert(res.declaredHeaders.size == 1)

    val vlantHeader = res.declaredHeaders.head._2
    assert(vlantHeader.length == 24)
    assert(
      Seq("bad_packet", "cpu_code").forall(f => vlantHeader.fields.values.exists(f == _._1))
    )
  }

  test("metadata and header instances can be parsed - vlan and local_metadata example") {
    val p4 = "src/main/resources/p4s/tests/header_and_metadata_instance.p4"
    val res = P4ParserRunner.parse(p4)

    assert(res.declaredHeaders.size == 2)
    assert(res.headerInstances.size == 2)
    assert(res.headerInstances("inner_vlan_tag").layout.headerName == "vlan_t")
    assert(res.headerInstances("local_metadata").asInstanceOf[MetadataInstance].values("bad_packet") == 1)
  }

  test("actions can be parsed - registrar is not empty") {
    val p4 = "inputs/simple-router/simple_router.p4"
    val res = P4ParserRunner.parse(p4)

    assert(res.actionRegistrar.getDeclaredActions.iterator().hasNext)
    assert(res.actionRegistrar.getAction("_drop") != null)
    for (x <- res.actionRegistrar.getDeclaredActions) {
      println(x)
    }
  }


  test("switch spec can be parsed - actions, reg defs and field lists are there") {
    val p4 = "inputs/simple-router/simple_router.p4"
    val dataplane = "inputs/simple-router/commands.txt"
    val res = SwitchInstance.fromP4AndDataplane(p4, dataplane, util.Arrays.asList("veth0", "veth1"))
    assert(res.getSwitchSpec.getActionRegistrar.getAction("_drop") != null)
    assert(res.getSwitchSpec.getActionRegistrar.getAction("set_dmac").getParameterList.size() == 1)
    assert(res.getSwitchSpec.getRegisterSpecificationMap != null)
    assert(res.flowInstanceIterator("ipv4_lpm").size() > 0)

    for (x <- res.getDeclaredTables) {
//      println(x.getTable + " " + x.getFireAction + " - " + x.getMatchParams + " - " + x.getActionParams)
      var i = 0
      for (y <- res.flowInstanceIterator(x)) {
        val fireAction = new FireAction(x, i, res).symnetCode()
        println(s"$i@$x")
        println(fireAction)
        i = i + 1
      }
      assert(res.getDefaultAction(x) != null)
      val p4ActionCall = new FireDefaultAction(x, res)
      println(s"default@$x")
      println(p4ActionCall.symnetCode())
    }
  }

  test("NAT parsing without data plane config") {
    val p4 = "inputs/simple-nat/simple_nat-ppc.p4"

    val p4Switch = Switch.fromFile(p4)

    val p4Program = P4Program.fromP4File(p4)

    val expd = new StateExpander(p4Switch, "start").doDFS(DFSState(0))
    StateExpander.generateAllPossiblePackets(expd, p4Switch)

    println("hello")
  }

  test("NAT spec can be parsed - actions, reg defs and field lists are there") {
    val p4 = "inputs/simple-nat/simple_nat.p4"
    val dataplane = "inputs/simple-nat/commands.txt"
    val res = SwitchInstance.fromP4AndDataplane(p4, dataplane, "nat", util.Arrays.asList("veth0", "veth1"))
    assert(res.getSwitchSpec.getActionRegistrar.getAction("_drop") != null)
    assert(res.getSwitchSpec.getActionRegistrar.getAction("set_dmac").getParameterList.size() == 1)
    assert(res.getSwitchSpec.getRegisterSpecificationMap != null)
    assert(res.flowInstanceIterator("ipv4_lpm").size() > 0)
  }

  test("SWITCH - new packet initialzier") {
    val p4 = "inputs/simple-nat/simple_nat.p4"
    val dataplane = "inputs/simple-nat/commands.txt"
    val res = SwitchInstance.fromP4AndDataplane(p4, dataplane, "nat", util.Arrays.asList("veth0", "veth1"))

    val initializeCode = new InitializeCode(res)
    println(JsonUtil.toJson(initializeCode.switchInitializePacketEnter(0)))
  }
  test("SWITCH - global initialzier") {
    val p4 = "inputs/register/register.p4"
    val dataplane = "inputs/register/commands.txt"
    val res = SwitchInstance.fromP4AndDataplane(p4, dataplane, "nat", util.Arrays.asList("veth0", "veth1"))

    val initializeCode = new InitializeCode(res)
    println(JsonUtil.toJson(initializeCode.switchInitializeGlobally()))
  }

  test("SWITCH - reg actions") {
    val p4 = "inputs/register/register.p4"
    val dataplane = "inputs/register/commands.txt"
    val res = SwitchInstance.fromP4AndDataplane(p4, dataplane, "nat", util.Arrays.asList("veth0", "veth1"))

    for (x <- res.getDeclaredTables) {
      //      println(x.getTable + " " + x.getFireAction + " - " + x.getMatchParams + " - " + x.getActionParams)
      var i = 0
      for (y <- res.flowInstanceIterator(x)) {
        val fireAction = new FireAction(x, i, res).symnetCode()
        println(s"$i@$x")
        println(JsonUtil.toJson(fireAction))
        i = i + 1
      }
    }
  }


  test("SWITCH - full table flow run #1") {
    val p4 = "inputs/simple-router/simple_router.p4"
    val dataplane = "inputs/simple-router/commands.txt"
    val res = SwitchInstance.fromP4AndDataplane(p4, dataplane, "nat", util.Arrays.asList("veth0", "veth1"))

    for (tab <- res.getDeclaredTables) {
      println(JsonUtil.toJson(new FullTable(tab, res).fullAction()))
    }
  }
}