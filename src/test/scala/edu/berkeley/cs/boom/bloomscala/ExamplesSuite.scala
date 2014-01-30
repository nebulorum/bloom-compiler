package edu.berkeley.cs.boom.bloomscala

import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.PropSpec
import scala.io.Source
import java.io.File
import edu.berkeley.cs.boom.bloomscala.codegen.js.RxJsCodeGenerator
import edu.berkeley.cs.boom.bloomscala.codegen.CodeGenerator
import edu.berkeley.cs.boom.bloomscala.codegen.dataflow.GraphvizDataflowPrinter

/**
 * Test suite that ensures that the example programs compile to each of
 * the target languages without displaying errors or crashing
 */
class ExamplesSuite extends PropSpec with TableDrivenPropertyChecks {

  val resourcesPath = Compiler.getClass.getClassLoader.getResource("examples").getPath
  val exampleFiles = new File(resourcesPath).listFiles.filter(_.getName.endsWith(".bloom"))
  assert(exampleFiles.length !== 0)

  val examples = Table(
    "program",
    exampleFiles: _*
  )

  def compilesWithBackend(backend: CodeGenerator)(file: File) {
    Compiler.messaging.resetmessages()
    val intermediate = Compiler.compileToIntermediateForm(Source.fromFile(file).mkString)
    assert(Compiler.messaging.messagecount === 0)
    Compiler.generateCode(intermediate, backend)
  }

  property("Should compile with RxJs backend") {
    forAll(examples) { compilesWithBackend(RxJsCodeGenerator) }
  }

  property("Should compile with GraphViz backend") {
    forAll(examples) { compilesWithBackend(GraphvizDataflowPrinter) }
  }
}