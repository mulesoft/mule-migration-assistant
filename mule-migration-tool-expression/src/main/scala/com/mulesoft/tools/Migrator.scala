package com.mulesoft.tools

import java.util
import java.util.Date

import com.mulesoft.tools.ast._
import com.mulesoft.tools.{ast => mel}
import org.mule.weave.v2.grammar._
import org.mule.weave.v2.parser.ast.logical.{AndNode, OrNode}
import org.mule.weave.v2.parser.annotation.{EnclosedMarkAnnotation, InfixNotationFunctionCallAnnotation, QuotedStringAnnotation}
import org.mule.weave.v2.parser.ast.functions.FunctionCallParametersNode
import org.mule.weave.v2.parser.ast.header.HeaderNode
import org.mule.weave.v2.parser.ast.structure.schema.{SchemaNode, SchemaPropertyNode}
import org.mule.weave.v2.parser.ast.types.TypeReferenceNode
import org.mule.weave.v2.parser.ast.variables.{NameIdentifier, VariableReferenceNode}
import org.mule.weave.v2.parser.{ast => dw}

import scala.util.{Failure, Success, Try}

object Migrator {

  val CLASS_PROPERTY_NAME = "class"

  def bindingContextVariable: List[String] = List("message", "exception", "payload", "flowVars", "sessionVars", "recordVars", "null");

  var counter = 0

  def toDataweaveAst(expressionNode: mel.MelExpressionNode): MigrationResult = {
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
      case mel.IfNode(ifExpr, condition, elseExpr) => toDataweaveIfNode(ifExpr, condition, elseExpr)
      case mel.MethodInvocationNode(canonicalName, arguments) => toDataweaveMethodInvocation(canonicalName, arguments)
      case mel.PropertyNode(name) => toDataweaveProperty(name.map(_.literal).mkString("."))
    }
  }

  def toEquals(variableReferenceNode: mel.VariableReferenceNode, arguments: Seq[MelExpressionNode]): MigrationResult = {
    toDataweaveBinaryOpNode(EqOpId, variableReferenceNode, arguments.head, new DefaultMigrationMetadata(Seq()))
  }

  private def toDataweaveMethodInvocation(canonicalName: CanonicalNameNode, arguments: Seq[MelExpressionNode]) = {
    val name = canonicalName.name
    val lastDot = name.lastIndexOf('.')

    val candidateToCanonicalName = name.patch(lastDot , "", name.length() - lastDot)
    val methodName = name.patch(0 , "", lastDot+1)
    val candidateToClass = Try(Thread.currentThread().getContextClassLoader.loadClass(candidateToCanonicalName))

    candidateToClass match {
      case Failure(_) => {
        methodName match {
          case "length" => toFunction("length", candidateToCanonicalName)
          case "size" => toFunction("sizeOf", candidateToCanonicalName)
          case "equals" => toEquals(mel.VariableReferenceNode(candidateToCanonicalName), arguments)
          case _ => {
            counter += 1
            val reference = "$" + counter
            new MigrationResult(toDataweaveStringNode(reference).dwAstNode, DefaultMigrationMetadata(Seq(NonMigratable("expressions.methodInvocation"))))
          }
        }
      }
      case Success(loadedClass) => toFunctionCall(methodName, CanonicalNameNode(candidateToCanonicalName), arguments)
    }

  }


  private def toFunction(functionName: String, candidateToCanonicalName: String) = {
    new MigrationResult(dw.functions.FunctionCallNode(VariableReferenceNode(NameIdentifier(functionName)), FunctionCallParametersNode(Seq(NameIdentifier(candidateToCanonicalName)))))
  }

  private def toDataweaveIfNode(ifExpr: MelExpressionNode, condition: MelExpressionNode, elseExpr: MelExpressionNode) = {
    val ifRes = toDataweaveAst(ifExpr)
    val conditionRes = toDataweaveAst(condition)
    val elseRes = toDataweaveAst(elseExpr)
    val metadata = ifRes.metadata.children ++ conditionRes.metadata.children ++ elseRes.metadata.children
    new MigrationResult(dw.conditional.IfNode(ifRes.dwAstNode, conditionRes.dwAstNode, elseRes.dwAstNode), DefaultMigrationMetadata(metadata))
  }


  private def toDataweaveStringNode(literal: String) = {
    new MigrationResult(dw.structure.StringNode(literal).annotate(QuotedStringAnnotation(''')))
  }

  private def toDataweaveNumberNode(literal: String) = {
    new MigrationResult(dw.structure.NumberNode(literal))
  }

  private def toDataweaveBooleanNode(literal: Boolean) = {
    new MigrationResult(dw.structure.BooleanNode(literal.toString))
  }

  private def toDataweaveNameIdentifierNode(literal: String) = {
    new MigrationResult(dw.variables.NameIdentifier(literal))
  }

  def toInstanceOf(left: MelExpressionNode, right: MelExpressionNode): MigrationResult = {
    val lRes = toDataweaveAst(left)
    val rRes = toDataweaveAst(right)
    val variableReferenceNode = VariableReferenceNode(NameIdentifier("Java::isInstanceOf"))
    val metadata = lRes.metadata.children ++ rRes.metadata.children
    val classNameNode = dw.structure.StringNode(rRes.getGeneratedCode(HeaderNode(Seq())).replaceFirst("---\nvars\\.", "")).annotate(QuotedStringAnnotation('''))
    new MigrationResult(dw.functions.FunctionCallNode(variableReferenceNode, FunctionCallParametersNode(Seq(lRes.dwAstNode, classNameNode))), DefaultMigrationMetadata(JavaModuleRequired() +: metadata))
  }

  private def toDataweaveBinaryOperatorNode(left: MelExpressionNode, right: MelExpressionNode, operatorType: Int): MigrationResult = {
    operatorType match {
      case mel.OperatorType.minus => toDataweaveBinaryOpNode(SubtractionOpId, left, right)
      case mel.OperatorType.dot => toDataweaveBinaryOpNode(ValueSelectorOpId, left, right)
      case mel.OperatorType.subscript => toDataweaveBinaryOpNode(DynamicSelectorOpId, left, right)
      case mel.OperatorType.plus => toDataweaveBinaryOpNode(AdditionOpId, left, right)
      case mel.OperatorType.equals => toDataweaveBinaryOpNode(EqOpId, left, right)
      case mel.OperatorType.notEquals => toDataweaveBinaryOpNode(NotEqOpId, left, right)
      case mel.OperatorType.lessThanOrEqual => toDataweaveBinaryOpNode(LessOrEqualThanOpId, left, right)
      case mel.OperatorType.greaterThanOrEqual => toDataweaveBinaryOpNode(GreaterOrEqualThanOpId, left, right)
      case mel.OperatorType.lessThan => toDataweaveBinaryOpNode(LessThanOpId, left, right)
      case mel.OperatorType.greaterThan => toDataweaveBinaryOpNode(GreaterThanOpId, left, right)
      case mel.OperatorType.and => toDataweaveAndNode(left, right)
      case mel.OperatorType.or => toDataweaveOrNode(left, right)
      case mel.OperatorType.multiplication => toDataweaveBinaryOpNode(MultiplicationOpId, left, right)
      case mel.OperatorType.division => toDataweaveBinaryOpNode(DivisionOpId, left, right)
      case mel.OperatorType.instanceOf => toInstanceOf(left, right)
    }
  }

  private def toDataweaveOrNode(left: MelExpressionNode, right: MelExpressionNode): MigrationResult = {
    val lRes = toDataweaveAst(left)
    val rRes = toDataweaveAst(right)
    new MigrationResult(OrNode(lRes.dwAstNode, rRes.dwAstNode), DefaultMigrationMetadata(lRes.metadata.children ++ rRes.metadata.children))
  }

  private def toDataweaveAndNode(left: MelExpressionNode, right: MelExpressionNode): MigrationResult = {
    val lRes = toDataweaveAst(left)
    val rRes = toDataweaveAst(right)
    new MigrationResult(AndNode(lRes.dwAstNode, rRes.dwAstNode), DefaultMigrationMetadata(lRes.metadata.children ++ rRes.metadata.children))
  }

  private def toDataweaveBinaryOpNode(opId: BinaryOpIdentifier, left: MelExpressionNode, right: MelExpressionNode, metadata: MigrationMetadata = Empty()): MigrationResult = {
    val lRes = toDataweaveAst(left)
    val rRes = toDataweaveAst(right)
    new MigrationResult(dw.operators.BinaryOpNode(opId, lRes.dwAstNode, rRes.dwAstNode), DefaultMigrationMetadata(lRes.metadata.children ++ rRes.metadata.children))
  }

  private def toDataweaveMapNode(elements: Seq[KeyValuePairNode]): MigrationResult = {
    val result = elements.map(toDataweaveAst)
    new MigrationResult(dw.structure.ObjectNode(result.map(r => r.dwAstNode)), DefaultMigrationMetadata(result.flatMap(r => r.metadata.children)))
  }

  private def toDataweaveKeyValuePairNode(key: StringNode, value: MelExpressionNode): MigrationResult = {
    val result = toDataweaveAst(value)
    val keyNode = dw.structure.KeyNode(key.literal)
    new MigrationResult(dw.structure.KeyValuePairNode(keyNode, result.dwAstNode), DefaultMigrationMetadata(result.metadata.children))
  }

  private def toDataweaveListNode(elements: Seq[MelExpressionNode]): MigrationResult = {
    val result = elements.map(toDataweaveAst)
    new MigrationResult(dw.structure.ArrayNode(result.map(r => r.dwAstNode)), DefaultMigrationMetadata(result.flatMap(r => r.metadata.children)))
  }

  private def toDataweaveEnclosedExpressionNode(expression: MelExpressionNode): MigrationResult = {
    val result = toDataweaveAst(expression)
    new MigrationResult(result.dwAstNode.annotate(EnclosedMarkAnnotation()), DefaultMigrationMetadata(result.metadata.children))
  }

  private def toDataweaveConstructorNode(canonicalName: CanonicalNameNode, arguments: Seq[MelExpressionNode]): MigrationResult = {
    val theClassToCreate = Try(Thread.currentThread().getContextClassLoader.loadClass(canonicalName.name))
    theClassToCreate match {
      case Failure(_) => toGenericConstructor(canonicalName, arguments)
      case Success(loadedClass) => {
        if (isMap(loadedClass, arguments)) toMapConstructor(canonicalName)
        else if (isList(loadedClass, arguments)) toListConstructor(canonicalName)
        else if (isDate(loadedClass, arguments)) toDateConstructor
        else toGenericConstructor(canonicalName, arguments)
      }
    }
  }

  def toDataweaveProperty(name: String): MigrationResult = {
    val functionCall = dw.functions.FunctionCallNode(VariableReferenceNode(NameIdentifier("p")), FunctionCallParametersNode(Seq(toDataweaveStringNode(name).dwAstNode)))
    new MigrationResult(functionCall, DefaultMigrationMetadata(Seq()))
  }

  private def toGenericConstructor(canonicalName: CanonicalNameNode, arguments: Seq[MelExpressionNode]): MigrationResult = {
    toFunctionCall("new", canonicalName, arguments)
  }

  private def toFunctionCall(methodName: String, canonicalName: CanonicalNameNode, arguments: Seq[MelExpressionNode]) = {
    val variableReferenceNode = VariableReferenceNode(NameIdentifier(canonicalName.name.replaceAll("\\.", "::") + "::" + methodName, Some("java")))
    val result = arguments.map(toDataweaveAst)
    new MigrationResult(dw.functions.FunctionCallNode(variableReferenceNode, FunctionCallParametersNode(result.map(r => r.dwAstNode))), DefaultMigrationMetadata(result.flatMap(r => r.metadata.children)))
  }

  private def toDateConstructor: MigrationResult = {
    new MigrationResult(dw.functions.FunctionCallNode(VariableReferenceNode(NameIdentifier("now"))))
  }

  private def toListConstructor(canonicalName: CanonicalNameNode): MigrationResult = {
    val objectNode = dw.structure.ArrayNode(Seq())
    val classValue = dw.structure.StringNode(canonicalName.name)
    //class: "java.util.ArrayList"
    val schemaProperty = SchemaPropertyNode(NameIdentifier(CLASS_PROPERTY_NAME), classValue)
    val arrayTypeRef = TypeReferenceNode(NameIdentifier("Array"), None, Some(SchemaNode(Seq(schemaProperty))))
    new MigrationResult(dw.operators.BinaryOpNode(AsOpId, objectNode, arrayTypeRef))
  }

  private def toMapConstructor(canonicalName: CanonicalNameNode): MigrationResult = {
    val objectNode = dw.structure.ObjectNode(Seq())
    val classValue = dw.structure.StringNode(canonicalName.name)
    //class: "java.util.HashMap"
    val schemaProperty = SchemaPropertyNode(NameIdentifier(CLASS_PROPERTY_NAME), classValue)
    val objectTypeRef = TypeReferenceNode(NameIdentifier("Object"), None, Some(SchemaNode(Seq(schemaProperty))))
    new MigrationResult(dw.operators.BinaryOpNode(AsOpId, objectNode, objectTypeRef))
  }

  private def isDate(loadedClass: Class[_], arguments: Seq[MelExpressionNode]) = {
    classOf[Date].isAssignableFrom(loadedClass) && arguments.isEmpty
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

  def migrate(melScript: String): MigrationResult = {
    counter = 0
    val expressionNode = MelParserHelper.parse(removeNullPayload(melScript))
    val result = toDataweaveAst(expressionNode)
    val bodyNode = resolveStringConcatenation(result.dwAstNode)
    new MigrationResult(bodyNode, result.metadata)
  }

  def resolveName(literal: String): String = {
    if (bindingContextVariable.exists(name => literal.startsWith(name)))
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
