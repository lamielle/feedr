package feedr.model

import java.util.UUID.randomUUID

case class Feed(id: String,
                applications: List[Application] = List[Application]())

case class Application(name: Editable[String],
                       version: Editable[String] = Editable(""),
                       description: Editable[String] = Editable("")) {
   private var _id = randomUUID().toString
   def id = _id
}

case class Editable[T](value: T)

object Helpers {
   implicit def editableString(str: String) = Editable[String](str)
}
