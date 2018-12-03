package com.mulesoft.tools

sealed trait MigrationMetadata {
  def children: Seq[MigrationMetadata] = Seq()
}

case class Empty() extends MigrationMetadata

case class DefaultMigrationMetadata(override val children: Seq[MigrationMetadata]) extends MigrationMetadata

case class JavaModuleRequired() extends MigrationMetadata

case class NonMigratable(reason: String) extends MigrationMetadata
