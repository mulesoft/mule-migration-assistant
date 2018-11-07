package com.mulesoft.tools

import java.util
import java.util.Date

import com.mulesoft.tools.ast._
import com.mulesoft.tools.{ast => mel}
import org.mule.weave.v2.codegen.CodeGenerator
import org.mule.weave.v2.grammar._
import org.mule.weave.v2.parser.ast.logical.{AndNode, OrNode}
import org.mule.weave.v2.parser.annotation.{EnclosedMarkAnnotation, InfixNotationFunctionCallAnnotation, QuotedStringAnnotation}
import org.mule.weave.v2.parser.ast.functions.FunctionCallParametersNode
import org.mule.weave.v2.parser.ast.header.HeaderNode
import org.mule.weave.v2.parser.ast.header.directives.{VersionDirective, VersionMajor, VersionMinor}
import org.mule.weave.v2.parser.ast.structure.schema.{SchemaNode, SchemaPropertyNode}
import org.mule.weave.v2.parser.ast.types.TypeReferenceNode
import org.mule.weave.v2.parser.ast.variables.{NameIdentifier, VariableReferenceNode}
import org.mule.weave.v2.parser.{ast => dw}

import scala.util.{Failure, Success, Try}

object Migrator {

  val CLASS_PROPERTY_NAME = "class"

  def bindingContextVariable: List[String] = List("message", "exception", "payload", "flowVars", "sessionVars", "recordVars", "null");

  def toDataweaveAst(expressionNode: mel.MelExpressionNode): dw.AstNode = {
    expressionNode match {
      case mel.StringNode(literal) => toDataweaveStringNode(literal)
      case mel.NumberNode(literal) => toDataweaveNumberNode(literal)
      case mel.BooleanNode(literal) => toDataweaveBooleanNode(literal)
      case mel.IdentifierNode(literal) => toDataweaveNameIdentifierNode(literal)
      case mel.VariableReferenceNode(literal) => toDataweaveNameIdentifierNode(resolveName(literal))
      case mel.BinaryOperatorNode(left, right, operatorType) => toDataweaveBinaryOperatorNode(left, right, operatorType)
      case mel.MapNode(elements) => toDataweaveMapNode(elements)
      case mel.KeyValuePairNode(key, value) => toDataweaveKeyValuePairNode(key, value)
      case mel.ListNode(elements) => toDataweaveListNode(elements)
      case mel.EnclosedExpression(expression) => toDataweaveEnclosedExpressionNode(expression)
      case mel.ConstructorNode(canonicalName, arguments) => toDataweaveConstructorNode(canonicalName, arguments)
    }
  }

  private def toDataweaveStringNode(literal: String) = {
    dw.structure.StringNode(literal).annotate(QuotedStringAnnotation('''))
  }

  private def toDataweaveNumberNode(literal: String) = {
    dw.structure.NumberNode(literal)
  }

  private def toDataweaveBooleanNode(literal: Boolean) = {
    dw.structure.BooleanNode(literal.toString)
  }

  private def toDataweaveNameIdentifierNode(literal: String) = {
    dw.variables.NameIdentifier(literal)
  }

  private def toDataweaveBinaryOperatorNode(left: MelExpressionNode, right: MelExpressionNode, operatorType: Int) = {
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

  private def toDataweaveMapNode(elements: Seq[KeyValuePairNode]) = {
    dw.structure.ObjectNode(elements.map(toDataweaveAst))
  }

  private def toDataweaveKeyValuePairNode(key: StringNode, value: MelExpressionNode) = {
    val keyNode = dw.structure.KeyNode(key.literal)
    dw.structure.KeyValuePairNode(keyNode, toDataweaveAst(value))
  }

  private def toDataweaveListNode(elements: Seq[MelExpressionNode]) = {
    dw.structure.ArrayNode(elements.map(toDataweaveAst))
  }

  private def toDataweaveEnclosedExpressionNode(expression: MelExpressionNode) = {
    toDataweaveAst(expression).annotate(EnclosedMarkAnnotation())
  }

  private def toDataweaveConstructorNode(canonicalName: CanonicalNameNode, arguments: Seq[MelExpressionNode]) = {
    val theClassToCreate = Try(Thread.currentThread().getContextClassLoader.loadClass(canonicalName.name))
    theClassToCreate match {
      case Failure(_) => {
        toGenericConstructor(canonicalName, arguments)
      }
      case Success(loadedClass) => {
        if (isMap(loadedClass, arguments)) {
          toMapConstructor(canonicalName)
        } else if (isList(loadedClass, arguments)) {
          toListConstructor(canonicalName)
        } else if (isDate(loadedClass, arguments)) {
          toDateConstructor
        } else {
          toGenericConstructor(canonicalName, arguments)
        }
      }
    }
  }

  private def toGenericConstructor(canonicalName: CanonicalNameNode, arguments: Seq[MelExpressionNode]) = {
    val variableReferenceNode = VariableReferenceNode(NameIdentifier(canonicalName.name.replaceAll("\\.", "::") + "::new", Some("java")))
    dw.functions.FunctionCallNode(variableReferenceNode, FunctionCallParametersNode(arguments.map(toDataweaveAst)))
  }

  private def toDateConstructor = {
    dw.functions.FunctionCallNode(VariableReferenceNode(NameIdentifier("now")))
  }

  private def isDate(loadedClass: Class[_], arguments: Seq[MelExpressionNode]) = {
    classOf[Date].isAssignableFrom(loadedClass) && arguments.isEmpty
  }

  private def toListConstructor(canonicalName: CanonicalNameNode) = {
    val objectNode = dw.structure.ArrayNode(Seq())
    val classValue = dw.structure.StringNode(canonicalName.name)
    //class: "java.util.ArrayList"
    val schemaProperty = SchemaPropertyNode(NameIdentifier(CLASS_PROPERTY_NAME), classValue)
    val arrayTypeRef = TypeReferenceNode(NameIdentifier("Array"), None, Some(SchemaNode(Seq(schemaProperty))))
    dw.operators.BinaryOpNode(AsOpId, objectNode, arrayTypeRef)
  }

  private def toMapConstructor(canonicalName: CanonicalNameNode) = {
    val objectNode = dw.structure.ObjectNode(Seq())
    val classValue = dw.structure.StringNode(canonicalName.name)
    //class: "java.util.HashMap"
    val schemaProperty = SchemaPropertyNode(NameIdentifier(CLASS_PROPERTY_NAME), classValue)
    val objectTypeRef = TypeReferenceNode(NameIdentifier("Object"), None, Some(SchemaNode(Seq(schemaProperty))))
    dw.operators.BinaryOpNode(AsOpId, objectNode, objectTypeRef)
  }

  private def isList(loadedClass: Class[_], arguments: Seq[MelExpressionNode]) = {
    classOf[util.List[_]].isAssignableFrom(loadedClass) && arguments.isEmpty
  }

  private def isMap(loadedClass: Class[_], arguments: Seq[MelExpressionNode]) = {
    classOf[util.Map[_, _]].isAssignableFrom(loadedClass) && arguments.isEmpty
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
    val documentNode = dw.structure.DocumentNode(HeaderNode(Seq(VersionDirective(VersionMajor("2"), VersionMinor("0")))), bodyNode)
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
