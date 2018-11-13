package com.mulesoft.tools

import org.mule.weave.v2.codegen.CodeGenerator
import org.mule.weave.v2.parser.ast.header.HeaderNode
import org.mule.weave.v2.parser.ast.header.directives.{VersionDirective, VersionMajor, VersionMinor}
import org.mule.weave.v2.parser.{ast => dw}

class MigrationResult(val dwAstNode: dw.AstNode, val metadata: MigrationMetadata = Empty()) {
  def getGeneratedCode(): String = {
    val documentNode = dw.structure.DocumentNode(HeaderNode(Seq(VersionDirective(VersionMajor("2"), VersionMinor("0")))), dwAstNode)
    CodeGenerator.generate(documentNode)
  }
}
