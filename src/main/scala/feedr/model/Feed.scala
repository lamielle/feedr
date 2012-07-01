package feedr.model

import java.util.UUID

class Feed(id: String,
           applications: List[Application]) {
}

class Application(id: UUID,
                  version: String,
                  description: String) {
  //id = UUID.randomUUID()
  //version = ""
  //description = ""
}

