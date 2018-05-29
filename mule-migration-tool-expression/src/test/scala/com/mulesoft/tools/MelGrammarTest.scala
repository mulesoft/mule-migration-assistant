package com.mulesoft.tools

import com.mulesoft.tools.ast._
import org.scalatest.{FlatSpec, Matchers}

class MelGrammarTest extends FlatSpec with Matchers {

  "MelGrammar" should "parse an empty list" in {
    MelParserHelper.parse("[ ]") shouldBe ListNode(Seq())
  }

  it should "parse a string" in {
    MelParserHelper.parse("\"abc\"") shouldBe StringNode("abc")
  }

  it should "parse a string simple quoted" in {
    MelParserHelper.parse("\'abc\'") shouldBe StringNode("abc")
  }

  it should "parse an empty string" in {
    MelParserHelper.parse("\"\"") shouldBe StringNode("")
  }

  it should "parse a positive integer number" in {
    List("1", "100", "12345678987654321", "000000", "+100").foreach(n => MelParserHelper.parse(n) shouldBe NumberNode(n))
  }

  it should "parse a negative integer number" in {
    List("-1", "-2", "-1000000000000000").foreach(n => MelParserHelper.parse(n) shouldBe NumberNode(n))
  }

  it should "parse a floating poing number" in {
    List("1.0", "1.", "-1.", "0.0", "100.43124312", "-1000.09595959", " 1.0", "1.   ", "    -1.   ", "0.0", "- 333.", "     +       1000.09595959     ").foreach(n => MelParserHelper.parse(n) shouldBe NumberNode(n.replace(" ", "")))
  }

  it should "parse a number appended with f" in {
    List("1.0f", "+1.0f", "- 1.0f").foreach(n => MelParserHelper.parse(n) shouldBe NumberNode(n.replace(" ", "")))
  }

  it should "parse $ as an identifier" in {
    MelParserHelper.parse("$") shouldBe VariableReferenceNode("$")
  }

  it should "parse _ as an identifier" in {
    MelParserHelper.parse("_") shouldBe VariableReferenceNode("_")
  }

  it should "parse alphabet letters as identifier" in {
    (('a' to 'z') ++ ('A' to 'Z')).foreach(c => MelParserHelper.parse(c.toString) shouldBe VariableReferenceNode(c.toString))
  }

  it should "parse these elements as identifiers" in {
    List("_aaa", "_aaa#", "_a###", "_a11", "a123").foreach(id => MelParserHelper.parse(id) shouldBe VariableReferenceNode(id))
  }

  it should "should fail when parsing these inputs" in {
    List("1a", "1_").foreach(input => assertThrows[RuntimeException] { MelParserHelper.parse(input) } )
  }

  it should "parse list with one element" in {
    MelParserHelper.parse("[ \"a\" ]") shouldBe ListNode(Seq(StringNode("a")))
  }

  it should "parse a map with one key-value pair" in {
    MelParserHelper.parse("[\"a\" : \"b\"]") shouldBe MapNode(Seq(KeyValuePairNode(StringNode("a"), StringNode("b"))))
  }

  it should "parse a map with two key-value pairs" in {
    MelParserHelper.parse("[\"a\" : \"b\", \"c\" : \"d\"]") shouldBe MapNode(Seq(KeyValuePairNode(StringNode("a"), StringNode("b")), KeyValuePairNode(StringNode("c"), StringNode("d"))))
  }

  it should "parse an expression with dot syntax" in {
    MelParserHelper.parse("a.b. c") shouldBe BinaryOperatorNode(BinaryOperatorNode(VariableReferenceNode("a"),IdentifierNode("b"),2),IdentifierNode("c"),2)
  }

  it should "parse an expression with subscript selector" in {
    MelParserHelper.parse("a[3]") shouldBe BinaryOperatorNode(VariableReferenceNode("a"),NumberNode("3"),4)
  }

  it should "parse an enclosed expression" in {
    MelParserHelper.parse("(payload)") shouldBe EnclosedExpression(VariableReferenceNode("payload"))
  }
}
