package com.mulesoft.tools

import org.mule.weave.v2.codegen.CodeGenerator
import org.mule.weave.v2.parser.ast.header.HeaderNode
import org.mule.weave.v2.parser.ast.header.directives.{VersionDirective, VersionMajor, VersionMinor}
import org.mule.weave.v2.parser.{ast => dw}

class MigrationResult(val dwAstNode: dw.AstNode, val metadata: MigrationMetadata = Empty()) {
  val DEFAULT_HEADER = HeaderNode(Seq(VersionDirective(VersionMajor("2"), VersionMinor("0"))))

  def getGeneratedCode(headerNode: HeaderNode): String = {
    CodeGenerator.generate(dw.structure.DocumentNode(headerNode, dwAstNode))
  }

  def getGeneratedCode(): String = {
    // avoid duplicating DW header
    if (defaultHeaderAlreadyDefined(dwAstNode)) {
      return CodeGenerator.generate(dwAstNode)
    }

    getGeneratedCode(DEFAULT_HEADER)
  }

  private def defaultHeaderAlreadyDefined(root: dw.AstNode): Boolean = {
    Option(dwAstNode.children()).isDefined && !dwAstNode.children().isEmpty && dwAstNode.children().head == DEFAULT_HEADER
  }
}
