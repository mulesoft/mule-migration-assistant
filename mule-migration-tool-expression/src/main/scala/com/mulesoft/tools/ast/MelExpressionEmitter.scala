package com.mulesoft.tools.ast

object MelExpressionEmitter {

  def toString(melExpressionNode: MelExpressionNode, indent: Int = 0): String = {
    "|" + ("--" * indent) +
      (melExpressionNode match {
        case StringNode(literal) => "StringNode(" + literal + ")"
        case BooleanNode(literal) => "BooleanNode(" + literal + ")"
        case IdentifierNode(literal) => "IdentifierNode(" + literal + ")"
        case VariableReferenceNode(literal) => "VariableReferenceNode(" + literal + ")"
        case NumberNode(literal) => "NumberNode(" + literal + ")"
        case BinaryOperatorNode(_, _, operatorType) => {
          "BinaryOperatorNode(" + operatorType + ")"
        }
        case _ => melExpressionNode.getClass.getSimpleName
      }) +
      melExpressionNode.children.map(toString(_, indent + 1)).foldLeft("")(_ + sys.props("line.separator") + _)
  }

}