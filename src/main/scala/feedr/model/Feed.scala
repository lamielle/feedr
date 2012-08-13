package feedr.model

import java.util.UUID.randomUUID

case class Feed(id: String, applications: List[Application] = List[Application]())

object Application {
   def createApplication =
      Application(randomUUID().toString, "", "", "")
   def createApplication(name: String, version: String, description: String) =
      Application(randomUUID().toString, name, version, description)
}

case class Application(id: String,
                       name: String,
                       version: String,
                       description: String)
