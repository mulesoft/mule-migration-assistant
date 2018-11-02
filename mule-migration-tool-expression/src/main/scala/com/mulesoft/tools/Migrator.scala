package com.mulesoft.tools

import com.mulesoft.tools.ast.{EnclosedExpression, MelExpressionNode, StringNode}
import com.mulesoft.tools.{ast => mel}
import org.mule.weave.v2.codegen.CodeGenerator
import org.mule.weave.v2.grammar._
import org.mule.weave.v2.parser.ast.logical.{AndNode, OrNode}
import org.mule.weave.v2.parser.annotation.{EnclosedMarkAnnotation, InfixNotationFunctionCallAnnotation, QuotedStringAnnotation}
import org.mule.weave.v2.parser.ast.functions.FunctionCallParametersNode
import org.mule.weave.v2.parser.ast.header.HeaderNode
import org.mule.weave.v2.parser.ast.variables.VariableReferenceNode
import org.mule.weave.v2.parser.{ast => dw}

object Migrator {

  def bindingContextVariable: List[String] = List("message", "exception", "payload", "flowVars", "sessionVars", "recordVars", "null");

  def toDataweaveAst(expressionNode: mel.MelExpressionNode): dw.AstNode = {
    expressionNode match {
      case mel.StringNode(literal) => dw.structure.StringNode(literal).annotate(QuotedStringAnnotation('''))
      case mel.NumberNode(literal) => dw.structure.NumberNode(literal)
      case mel.BooleanNode(literal) => dw.structure.BooleanNode(literal.toString)
      case mel.IdentifierNode(literal) => dw.variables.NameIdentifier(literal)
      case mel.VariableReferenceNode(literal) => dw.variables.NameIdentifier(resolveName(literal))
      case mel.BinaryOperatorNode(left, right, operatorType) => {
        operatorType match {
          case mel.OperatorType.minus => dw.operators.BinaryOpNode(SubtractionOpId, toDataweaveAst(left), toDataweaveAst(right))
          case mel.OperatorType.dot => dw.operators.BinaryOpNode(ValueSelectorOpId, toDataweaveAst(left), toDataweaveAst(right))
          case mel.OperatorType.subscript => dw.operators.BinaryOpNode(DynamicSelectorOpId, toDataweaveAst(left), toDataweaveAst(right))
          case mel.OperatorType.plus => dw.operators.BinaryOpNode(AdditionOpId, toDataweaveAst(left), toDataweaveAst(right))
          case mel.OperatorType.equals => dw.operators.BinaryOpNode(EqOpId, toDataweaveAst(left), toDataweaveAst(right))
          case mel.OperatorType.notEquals => dw.operators.BinaryOpNode(NotEqOpId, toDataweaveAst(left), toDataweaveAst(right))
          case mel.OperatorType.lessThanOrEqual => dw.operators.BinaryOpNode(LessOrEqualThanOpId, toDataweaveAst(left), toDataweaveAst(right))
          case mel.OperatorType.greaterThanOrEqual => dw.operators.BinaryOpNode(GreaterOrEqualThanOpId, toDataweaveAst(left), toDataweaveAst(right))
          case mel.OperatorType.lessThan => dw.operators.BinaryOpNode(LessThanOpId, toDataweaveAst(left), toDataweaveAst(right))
          case mel.OperatorType.greaterThan => dw.operators.BinaryOpNode(GreaterThanOpId, toDataweaveAst(left), toDataweaveAst(right))
          case mel.OperatorType.and => AndNode(toDataweaveAst(left), toDataweaveAst(right))
          case mel.OperatorType.or => OrNode(toDataweaveAst(left), toDataweaveAst(right))
          case mel.OperatorType.multiplication => dw.operators.BinaryOpNode(MultiplicationOpId, toDataweaveAst(left), toDataweaveAst(right))
          case mel.OperatorType.division => dw.operators.BinaryOpNode(DivisionOpId, toDataweaveAst(left), toDataweaveAst(right))
        }
      }
      case mel.MapNode(elements) => {
        dw.structure.ObjectNode(elements.map(toDataweaveAst))
      }
      case mel.KeyValuePairNode(key, value) => {
        val keyNode = dw.structure.KeyNode(key.literal)
        dw.structure.KeyValuePairNode(keyNode, toDataweaveAst(value))
      }
      case mel.ListNode(elements) => {
        dw.structure.ArrayNode(elements.map(toDataweaveAst))
      }
      case mel.EnclosedExpression(expression) => {
        toDataweaveAst(expression).annotate(EnclosedMarkAnnotation())
      }
    }
  }

  def forceConcatenation(expressionNode: dw.AstNode): dw.AstNode = {
    expressionNode match {
      case dw.operators.BinaryOpNode(AdditionOpId, left, right) => {
        dw.functions.FunctionCallNode(VariableReferenceNode("++"), FunctionCallParametersNode(Seq(forceConcatenation(left), forceConcatenation(right)))).annotate(InfixNotationFunctionCallAnnotation())
      }
      case _ => expressionNode
    }
  }

  def resolveStringConcatenation(expressionNode: dw.AstNode): dw.AstNode = {
    expressionNode match {
      case dw.operators.BinaryOpNode(operatorType, left, right) => {
        operatorType match {
          case AdditionOpId => {
            if (isStringType(left) || isStringType(right)) {
              return dw.functions.FunctionCallNode(VariableReferenceNode("++"), FunctionCallParametersNode(Seq(forceConcatenation(left), forceConcatenation(right)))).annotate(InfixNotationFunctionCallAnnotation())
            } else expressionNode
          }
          case _ => expressionNode
        }
      }
      case _ => expressionNode
    }
    expressionNode.children.map(resolveStringConcatenation)
    return expressionNode
  }

  def removeNullPayload(melScript: String): String = {
    melScript.replaceAll("NullPayload\\.getInstance\\(\\)", "null")
  }

  def migrate(melScript: String): String = {
    val expressionNode = MelParserHelper.parse(removeNullPayload(melScript))
    val bodyNode = resolveStringConcatenation(toDataweaveAst(expressionNode))
    val documentNode = dw.structure.DocumentNode(HeaderNode(Seq()), bodyNode)
    CodeGenerator.generate(documentNode)
  }

  def resolveName(literal: String): String = {
    if (bindingContextVariable.exists(name => literal.equals(name)))
      literal
    else
      "vars." + literal
  }

  def isStringType(left: MelExpressionNode): Boolean = {
    left match {
      //TODO check cast and .toString()
      case StringNode(_) => true
      case EnclosedExpression(expression) => isStringType(expression)
      case _ => false
    }
  }

  def isStringType(node: dw.AstNode): Boolean = {
    node match {
      //TODO check cast and .toString()
      case dw.structure.StringNode(_) => true
      case _ => node.children().exists(isStringType)
    }
  }
}
