package com.mulesoft.tools

import com.mulesoft.tools.ast.MelExpressionNode
import org.parboiled2.ParserInput
import org.parboiled2.Parser.DeliveryScheme.Either

import scala.util.{Failure, Success}

object MelParserHelper {

  def parse(content: String): MelExpressionNode = {
    val input = ParserInput(content)
    val grammar = new MelGrammar(input)
    val triedNode = grammar.root.run()
    triedNode match {
      case Left(exception) => throw new RuntimeException(exception.format(input))
      case Right(value) => value
    }
  }

}
