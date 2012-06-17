package feedr.model

class Feed(version: Int, description: String, applications: List[Application]) {

}

class Application(name: String,
                  vendor: String,
                  version: String,
                  description: String)
//                  categories: List[String])

