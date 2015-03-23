package edu.berkeley.cs.boom.bloomscala.codegen.c4

import edu.berkeley.cs.boom.bloomscala.codegen.CodeGenerator
import edu.berkeley.cs.boom.bloomscala.ast._
import edu.berkeley.cs.boom.bloomscala.analysis.{Stratum, DepAnalyzer, Stratifier}
import edu.berkeley.cs.boom.bloomscala.typing.FieldType


object C4CodeGenerator extends DatalogCodeGenerator {
  final def generateCode(orig_program: Program, stratifier: Stratifier, depAnalyzer: DepAnalyzer): CharSequence = {
    val program = orig_program

    val tables = program.declarations.map { decl =>
      val cols = decl.keys ++ decl.values
      val typs = cols.map { m =>
        m.typ match {
          case FieldType(s) => text(s)
        }
      }
      "define" <> parens(decl.name <> comma <+> braces(ssep(typs, ", "))) <> semi
    }

    val rules = genProgram(program, false)
    val doc = (rules ++ tables).toSeq.reduce(_ <@@> _)
    super.pretty(doc)
  }
}