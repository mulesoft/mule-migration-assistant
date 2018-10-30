package com.mulesoft.tools.ast

sealed trait MelExpressionNode {
  def children: Seq[MelExpressionNode] = Seq()
}

case class StringNode(literal: String) extends MelExpressionNode

case class NumberNode(literal: String) extends MelExpressionNode

case class BooleanNode(literal: Boolean) extends MelExpressionNode

case class IdentifierNode(literal: String) extends MelExpressionNode

case class VariableReferenceNode(literal: String) extends MelExpressionNode

case class EnclosedExpression(expression: MelExpressionNode) extends MelExpressionNode {
  override def children: Seq[MelExpressionNode] = Seq(expression)
}

case class BinaryOperatorNode(left: MelExpressionNode, right: MelExpressionNode, operatorType: Int) extends MelExpressionNode {
  override def children: Seq[MelExpressionNode] = Seq(left, right)
}

case class MapNode(elements: Seq[KeyValuePairNode]) extends MelExpressionNode {
  override def children: Seq[MelExpressionNode] = elements
}

case class KeyValuePairNode(key: StringNode, value: MelExpressionNode) extends MelExpressionNode {
  override def children: Seq[MelExpressionNode] = Seq(key, value)
}

case class ListNode(elements: Seq[MelExpressionNode]) extends MelExpressionNode {
  override def children: Seq[MelExpressionNode] = elements
}

object OperatorType {
  val plus = 0
  val minus = 1
  val dot = 2
  val and = 3
  val or = 4
  val subscript = 5
  val equals = 6
  val notEquals = 7
  val lessThanOrEqual = 8
  val greaterThanOrEqual = 9
  val lessThan = 10
  val greaterThan = 11
  val multiplication = 12
  val division = 13
  val leftShift = 14
  val rightShift = 15
}