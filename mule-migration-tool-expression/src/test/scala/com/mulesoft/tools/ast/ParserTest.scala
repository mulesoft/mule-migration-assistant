package com.mulesoft.tools.ast

import java.io.File
import java.net.URL
import java.util.Enumeration

import scala.collection.JavaConverters._
import com.mulesoft.tools.{MelGrammar, MelParserHelper}
import org.parboiled2.ParserInput
import org.scalatest.{FunSpec, Matchers}

import scala.io.Source._
import scala.util.{Failure, Success}

class ParserTest extends FunSpec with Matchers {


  scenarios.foreach {
    case (fileName, weaveFile, astFile) =>
      it(fileName) {
        val expected = fromFile(astFile).mkString
        val result = MelParserHelper.parse(fromFile(weaveFile).mkString)
        val tree = MelExpressionEmitter.toString(result)
        if (tree.trim != expected.trim)
          println(tree)
        tree.trim shouldBe expected.trim
      }
  }


  def scenarios: Seq[(String, File, File)] = {
    val testClass: Class[ParserTest] = classOf[ParserTest]
    val folders = getFoldersForTest(testClass)
    val toSeq: Seq[(String, File, File)] = folders
      .flatMap((folder) => folder.listFiles.filter(acceptTest)
        .groupBy(_.getName.dropRight(4))
        .flatMap((entry: (String, Array[File])) => {
          val ast: Array[File] = entry._2.filter(_.getName.endsWith(".ast"))
          val wev: Array[File] = entry._2.filter((f) => f.getName.endsWith(".mel"))
          if (ast.nonEmpty && wev.nonEmpty)
            Some((entry._1, wev.head, ast.head))
          else {
            None
          }
        }))
    toSeq.sortBy(_._1)
  }

  def getFoldersForTest(testClass: Class[_]): Seq[File] = {
    val url: Enumeration[URL] = testClass.getClassLoader.getResources(testClass.getPackage.getName.replaceAll("\\.", "/"))
    val folders = url.asScala.toSeq.flatMap((url) => {
      if (url.getProtocol.startsWith("file"))
        Some(new File(url.toURI))
      else
        None
    })
    folders
  }

  def acceptTest(file: File): Boolean = {
    val fileName: String = file.getName
    val testsToRun = Option(System.getProperty("testToRun")) match {
      case Some(x) => x.split(",")
      case None => Array[String]()
    }
    val testsToSkip = Option(System.getProperty("testToSkip")) match {
      case Some(x) => x.split(",")
      case None => Array[String]()
    }

    val accepted: Boolean = {
      (testsToRun.isEmpty || testsToRun.exists(fileName.contains)) &&
        testsToSkip.forall(!fileName.startsWith(_))
    }



    //We use .ldw for some cases where de intellij plugins consumes 100% of cpu
    (fileName.endsWith(".mel") || fileName.endsWith(".ast")) && accepted

  }

}
