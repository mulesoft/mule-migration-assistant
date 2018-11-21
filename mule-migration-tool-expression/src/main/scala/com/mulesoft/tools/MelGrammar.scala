package com.mulesoft.tools

import com.mulesoft.tools.ast._
import org.parboiled2.{Parser, Rule1, _}

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
  private val createConstructorNode = (canonicalName: CanonicalNameNode, arguments: Seq[MelExpressionNode]) => ConstructorNode(canonicalName, arguments)
  private val createCanonicalNameNode = (name: String) => CanonicalNameNode(name)


  private val whiteSpaceChar = CharPredicate(" \f\t")
  private val newLineChar = CharPredicate("\r\n")
  val whiteSpaceOrNewLineChar = whiteSpaceChar ++ newLineChar

  def root: Rule1[MelExpressionNode] = rule {
    ws ~ expression ~ ws ~ EOI
  }

  def expression: Rule1[MelExpressionNode] = rule {
    logicalExpression ~ ws
  }

  def logicalExpression = rule {
    instanceOfExpression ~ optional((orToken | andToken) ~ root ~> createBinaryOp)
  }

  def instanceOfExpression = rule {
    comparableExpression ~ optional(instanceOfToken ~ root ~> createBinaryOp)
  }

  def comparableExpression = rule {
    sum ~ optional(comparableToken ~ root ~> createBinaryOp)
  }

  def sum: Rule1[MelExpressionNode] = namedRule("Math Operator") {
    multiplicativeExpr ~ optional(ws ~ oneOrMore(plusSubExpr | minusSubExpr | rightShiftSubExpr | leftShiftSubExpr).separatedBy(ws))
  }

  def plusSubExpr = namedRule("+") {
    plusToken ~ ws ~ push(OperatorType.plus) ~!~ multiplicativeExpr ~> createBinaryOp
  }

  def minusSubExpr = namedRule("-") {
    minusToken ~ push(OperatorType.minus) ~!~ multiplicativeExpr ~> createBinaryOp
  }

  def rightShiftSubExpr = namedRule(">>") {
    rightShiftToken ~ push(OperatorType.rightShift) ~!~ multiplicativeExpr ~> createBinaryOp
  }

  def leftShiftSubExpr = namedRule("<<") {
    leftShiftToken ~ push(OperatorType.leftShift) ~!~ multiplicativeExpr ~> createBinaryOp
  }

  def multiplicationSubExpr = namedRule("*") {
    multiplicationToken ~ push(OperatorType.multiplication) ~!~ simpleExpressions ~> createBinaryOp
  }

  def divisionSubExpr = namedRule("/") {
    divisionToken ~ push(OperatorType.division) ~!~ simpleExpressions ~> createBinaryOp
  }

  def multiplicativeExpr: Rule1[MelExpressionNode] = namedRule("Math Operator") {
    simpleExpressions ~ optional(ws ~ oneOrMore(multiplicationSubExpr | divisionSubExpr).separatedBy(ws))
  }

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

  def enclosedExpression: Rule1[MelExpressionNode] = rule {
    ws ~ "(" ~ ws ~ expression ~ ws ~ ")" ~> createEnclosedExpression
  }

  def instanceOfToken = rule {
    ws ~ "instanceOf" ~ push(OperatorType.instanceOf)
  }

  def comparableToken = rule {
    equalsToken | notEqualsToken | lessThanOrEqualToken | greaterThanOrEqualToken | lessThanToken | greaterThanToken
  }

  def andToken = rule {
    ws ~ "&&" ~ push(OperatorType.and)
  }

  def orToken = rule {
    ws ~ "||" ~ push(OperatorType.or)
  }

  def plusToken = rule {
    ws ~ ch('+')
  }

  def multiplicationToken = rule {
    ws ~ ch('*')
  }

  def divisionToken = rule {
    ws ~ ch('/')
  }

  def equalsToken = rule {
    ws ~ "==" ~ push(OperatorType.equals)
  }

  def notEqualsToken = rule {
    ws ~ "!=" ~ push(OperatorType.notEquals)
  }

  def lessThanOrEqualToken = rule {
    ws ~ "<=" ~ push(OperatorType.lessThanOrEqual)
  }

  def greaterThanOrEqualToken = rule {
    ws ~ ">=" ~ push(OperatorType.greaterThanOrEqual)
  }

  def lessThanToken = rule {
    ws ~ ch('<') ~ push(OperatorType.lessThan)
  }

  def greaterThanToken = rule {
    ws ~ ch('>') ~ push(OperatorType.greaterThan)
  }

  def minusToken = rule {
    ws ~ ch('-')
  }

  def leftShiftToken = rule {
    ws ~ str("<<")
  }

  def rightShiftToken = rule {
    ws ~ str(">>")
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
    clearSB() ~ ws ~ legalIdentifierName ~ push(sb.toString) ~> createIdentifierNode
  }

  def varReference: Rule1[VariableReferenceNode] = rule {
    clearSB() ~ ws ~ legalIdentifierName ~ push(sb.toString) ~> createVariableReferenceNode
  }

  def legalCanonicalName: Rule0 = rule {
    zeroOrMore((CharPredicate.AlphaNum | ch('.')) ~ appendSB())
  }

  def canonicalNameNode: Rule1[CanonicalNameNode] = rule {
    clearSB() ~ ws ~ legalCanonicalName ~ push(sb.toString) ~> createCanonicalNameNode
  }

  def simpleExpressions: Rule1[MelExpressionNode] = rule {
    booleanNode | numberNode | values
  }

  def values = rule {
    (constructor | stringNode | stringNodeSimple | list | map | varReference | enclosedExpression) ~ zeroOrMore(selector)
  }

  def constructor: Rule1[ConstructorNode] = rule {
    (ws ~ str("new") ~ ws ~ canonicalNameNode ~ ws ~ ch('(') ~ ws ~ ((ch(')') ~ push(Seq())) | oneOrMore(simpleExpressions).separatedBy(',') ~ ws ~ ch(')'))) ~> createConstructorNode
  }

  def selector = rule {
    dot | subscript
  }

  def dot = rule {
    ws ~ ch('.') ~ push(OperatorType.dot) ~ ws ~ (identifierNode | stringNode | stringNodeSimple) ~> createBinaryOp
  }

  def subscript = rule {
    ws ~ ch('[') ~ ws ~ push(OperatorType.subscript) ~ simpleExpressions ~ ws ~ ch(']') ~ ws ~> createBinaryOp
  }
}
