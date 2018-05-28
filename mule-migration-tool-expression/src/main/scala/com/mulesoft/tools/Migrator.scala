package com.mulesoft.tools

import com.mulesoft.tools.{ast => mel}
import org.mule.weave.v2.codegen.CodeGenerator
import org.mule.weave.v2.grammar._
import org.mule.weave.v2.parser.annotation.QuotedStringAnnotation
import org.mule.weave.v2.parser.ast.header.HeaderNode
import org.mule.weave.v2.parser.{ast => dw}

object Migrator {

  def bindingContextVariable: List[String] = List("message", "exception", "payload", "message.payload", "flowVars", "sessionVars", "null");

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
          case mel.OperatorType.plus => {
            //TODO this with string is not going to work requires ++
            dw.operators.BinaryOpNode(AdditionOpId, toDataweaveAst(left), toDataweaveAst(right))
          }
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
    }
  }

  def migrate(melScript: String): String = {
    val expressionNode = MelParserHelper.parse(melScript)
    val bodyNode = toDataweaveAst(expressionNode)
    val documentNode = dw.structure.DocumentNode(HeaderNode(Seq()), bodyNode)
    CodeGenerator.generate(documentNode)
  }

  def resolveName(literal: String): String = {
    if(bindingContextVariable.exists(name => literal.equals(name)))
      literal
    else
      "vars." + literal
  }
}
