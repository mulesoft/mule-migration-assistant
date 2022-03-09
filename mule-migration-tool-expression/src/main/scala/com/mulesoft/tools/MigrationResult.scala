package com.mulesoft.tools

import com.mulesoft.tools.Migrator.DEFAULT_HEADER
import org.mule.weave.v2.codegen.CodeGenerator
import org.mule.weave.v2.parser.ast.header.HeaderNode
import org.mule.weave.v2.parser.{ast => dw}

class MigrationResult(val dwAstNode: dw.AstNode, val metadata: MigrationMetadata = Empty()) {

  def getGeneratedCode(headerNode: HeaderNode): String = {
    CodeGenerator.generate(dw.structure.DocumentNode(headerNode, dwAstNode))
  }

  def getGeneratedCode(): String = {
    getGeneratedCode(DEFAULT_HEADER)
  }
}
