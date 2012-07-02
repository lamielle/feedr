package feedr.model

case class Feed(id: String, applications: List[Application] = List[Application]())

case class Application(name: String,
                       version: String,
                       description: String)
