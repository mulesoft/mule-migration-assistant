package com.mulesoft.tools

import com.mulesoft.tools.ast._
import org.parboiled2.{Parser, Rule1, _}
import org.parboiled2.Parser.DeliveryScheme.Either

class MelGrammar(val input: ParserInput) extends Parser with StringBuilding {

  private val createEnclosedExpression = (expression: MelExpressionNode) => EnclosedExpression(expression)
  private val createStringNode = (value: String) => StringNode(value)
  private val createNumberNode = (value: String) => NumberNode(value)
  private val createBooleanNode = (value: Boolean) => BooleanNode(value)
  private val createIdentifierNode = (value: String) => IdentifierNode(value)
  private val createVariableReferenceNode = (value: String) => VariableReferenceNode(value)
  private val createBinaryOp = (left: MelExpressionNode, operatorType: Int, right: MelExpressionNode) => BinaryOperatorNode(left, right, operatorType)
  private val createKeyValuePairNode = (key: StringNode, value: MelExpressionNode) => KeyValuePairNode(key, value)
  private val createMapNode = (elements: Seq[KeyValuePairNode]) => MapNode(elements)
  private val createListNode = (elements: Seq[MelExpressionNode]) => ListNode(elements)


  private val whiteSpaceChar = CharPredicate(" \f\t")
  private val newLineChar = CharPredicate("\r\n")
  val whiteSpaceOrNewLineChar = whiteSpaceChar ++ newLineChar

  def ws: Rule0 = rule {
    quiet(zeroOrMore(whiteSpaceOrNewLineChar))
  }

  def fws: Rule0 = rule {
    quiet(oneOrMore(whiteSpaceOrNewLineChar))
  }

  def map = rule {
    map1 | map2
  }

  def map1: Rule1[MapNode] = rule {
    (ws ~ ch('[') ~ ws ~ oneOrMore(keyValuePair).separatedBy(',') ~ ws ~ ch(']')) ~> createMapNode
  }

  def map2: Rule1[MapNode] = rule {
    (ws ~ ch('{') ~ ws ~ oneOrMore(keyValuePair).separatedBy(',') ~ ws ~ ch('}')) ~> createMapNode
  }

  def list: Rule1[ListNode] = rule {
    (ws ~ ch('[') ~ ws ~ ((ch(']') ~ push(Seq())) | oneOrMore(simpleExpressions).separatedBy(',') ~ ws ~ ch(']'))) ~> createListNode
  }

  def keyValuePair: Rule1[KeyValuePairNode] = rule {
    ws ~ (stringNode | stringNodeSimple) ~ ws ~ ch(':') ~ ws ~ simpleExpressions ~ ws ~> createKeyValuePairNode
  }

  def root: Rule1[MelExpressionNode] = rule {
    ws ~  expression ~ ws ~ EOI
  }

  def enclosedExpression: Rule1[MelExpressionNode] = rule {
    ws ~ "(" ~ ws ~ expression ~ ws ~ ")" ~> createEnclosedExpression
  }

  def expression: Rule1[MelExpressionNode] = rule {
    sum ~ ws
  }

  def sum = rule {
    simpleExpressions ~ optional((plusToken ~ push(OperatorType.plus) | minusToken ~ push(OperatorType.minus)) ~ root ~> createBinaryOp)
  }


  def plusToken = rule {
    ws ~ ch('+')
  }

  def minusToken = rule {
    ws ~ ch('-')
  }

  def numberNode: Rule1[NumberNode] = rule {
    ws ~ clearSB() ~ sign ~ digits ~ optional(ch('.') ~ appendSB()) ~ optional(digits) ~ optional(ch('f') ~ appendSB()) ~ push(sb.toString) ~> createNumberNode
  }

  def sign = rule {
    ws ~ optional((ch('-') | '+') ~ appendSB()) ~ ws
  }

  def digits = rule {
    oneOrMore(CharPredicate.Digit ~ appendSB())
  }

  def stringNode: Rule1[StringNode] = rule {
    ws ~ ch('"') ~ clearSB() ~ zeroOrMore(doubleQuoteEscapedChar | noneOf("\"") ~ appendSB()) ~ ch('"') ~ push(sb.toString) ~> createStringNode
  }

  def stringNodeSimple: Rule1[StringNode] = rule {
    ws ~ ch('\'') ~ clearSB() ~ zeroOrMore(doubleQuoteEscapedChar | noneOf("\'") ~ appendSB()) ~ ch('\'') ~ push(sb.toString) ~> createStringNode
  }

  def doubleQuoteEscapedChar() = rule {
    str("\\\"")
  }

  def booleanNode: Rule1[BooleanNode] = rule {
    ws ~ ((str("true") ~ push(true)) | (str("false") ~ push(false))) ~> createBooleanNode
  }

  def legalIdentifierName: Rule0 = rule {
    ((CharPredicate.Alpha | ch('$') | ch('_')) ~ appendSB()) ~ zeroOrMore((CharPredicate.AlphaNum | ch('$') | ch('_')) ~ appendSB()) ~ zeroOrMore(ch('#') ~ appendSB())
  }

  def identifierNode: Rule1[IdentifierNode] = rule {
    clearSB() ~ legalIdentifierName ~ push(sb.toString) ~> createIdentifierNode
  }

  def varReference: Rule1[VariableReferenceNode] = rule {
    clearSB() ~ legalIdentifierName ~ push(sb.toString) ~> createVariableReferenceNode
  }

  def simpleExpressions: Rule1[MelExpressionNode] = rule {
    booleanNode | numberNode | values
  }

  def values = rule {
    (stringNode | stringNodeSimple | list | map | varReference | enclosedExpression) ~ zeroOrMore(dot) ~ zeroOrMore(subscript)
  }

  def dot = rule {
    ws ~ ch('.') ~ push(OperatorType.dot) ~ ws ~ (identifierNode | stringNode | stringNodeSimple) ~> createBinaryOp
  }

  def subscript = rule {
    ws ~ ch('[') ~ ws ~ push(OperatorType.subscript) ~ simpleExpressions ~ ws ~ ch(']') ~ ws ~> createBinaryOp
  }

}
