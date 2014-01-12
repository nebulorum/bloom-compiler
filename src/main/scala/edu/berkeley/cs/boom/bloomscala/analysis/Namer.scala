package edu.berkeley.cs.boom.bloomscala.analysis

import edu.berkeley.cs.boom.bloomscala.parser.AST._
import org.kiama.attribution.Attribution._
import org.kiama.util.Messaging
import org.kiama.attribution.Attributable

class Namer(val messaging: Messaging) {

  import messaging.message

  lazy val declaration: CollectionRef => CollectionDeclaration =
    attr {
      case cr @ CollectionRef(name) => cr->lookup(name) match {
        case md: MissingDeclaration =>
          message(cr, s"Unknown collection $name")
          md
        case cd => cd
      }
    }

  lazy val shortNameBindings: MappedCollectionTarget => Seq[CollectionRef] =
    attr {
      case JoinedCollection(a, b, _) => Seq(a, b)
      case cr: CollectionRef => Seq(cr)
    }

  lazy val lookup: String => Attributable => CollectionDeclaration =
    paramAttr {
      name => {
        case mc @ MappedCollection(target, shortNames, _) =>
          val shortNameTargets: Seq[CollectionRef] = shortNameBindings(target)
          if (shortNameTargets.size != shortNames.size) {
            message(mc, s"Wrong number of short names; expected ${shortNameTargets.size} " +
                        s"but got ${shortNames.size}")
          }
          val bindings = shortNames.zip(shortNameTargets).toMap
          bindings.get(name).map(_->lookup(name)).getOrElse(mc.parent->lookup(name))
        case Program(nodes) =>
          val declarations = nodes.filter(_.isInstanceOf[CollectionDeclaration])
            .map(_.asInstanceOf[CollectionDeclaration])
          val decl = declarations.find(_.name == name)
          decl.getOrElse(new MissingDeclaration())
        case n => n.parent->lookup(name)
      }
    }

}
