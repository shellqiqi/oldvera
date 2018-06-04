package org.change.v3.parsers

import org.change.utils.RepresentationConversion

import scala.io.Source

object OpenFlowRuleParser {

  type MatchRule = Map[String, MatchValue]
  type ValueParser = String => MatchValue

  def getParser(fieldName: String): ValueParser = fieldName match {
    case _ if fieldName.startsWith("tp") || fieldName.equalsIgnoreCase("nw_proto") =>
      {v => SingleValue(Integer.parseInt(v))}
    case _ if fieldName.startsWith("nw") && ! fieldName.equalsIgnoreCase("nw_proto") => {
      v: String => {
        val splitted = v.split("/")
        val ip = splitted(0)
        val mask = splitted(1)
        val (l, u) = RepresentationConversion.ipAndMaskToInterval(ip, mask)
        if (l == u)
          SingleValue(l)
        else
          ValueRange(l,u)
      }
    }
    case _ if fieldName.startsWith("dl") =>
      {v => SingleValue(RepresentationConversion.macToNumber(v))}
    case "eth_type" =>
      {v => SingleValue(Integer.parseInt(v.drop(2), 16))} // drop the 0X, and use base 16
    case _ => ???
  }

  def parse(openFlowFile: String): Seq[MatchRule] = {
    for {
      line <- Source.fromFile(openFlowFile).getLines()
    } yield {
      for {
        matchPair <-line.split("\\s*,\\s*")
        tokens = matchPair.split("=")
        fieldName = tokens(0)
        conditionValue = tokens(1)
      } yield fieldName -> getParser(fieldName)(conditionValue)
    }.toMap
  }.toSeq
}

sealed trait MatchValue
case class SingleValue(value: Long) extends MatchValue
case class ValueRange(min: Long, max: Long) extends MatchValue

object ParserRunner {
  def main(args: Array[String]): Unit = {
    for {
      rule <- OpenFlowRuleParser.parse("src/main/resources/openflow_rules/of-classbench-1.of")
    } println(rule)
  }
}