package feedr.model

import java.util.UUID
import java.util.UUID.randomUUID

import net.liftweb.record.{MetaRecord, Record}
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.record.field.{LongField, StringField}
import net.liftweb.squerylrecord.RecordTypeMode._

import org.squeryl.Schema

case class Editable[T](value: T)

object Helpers {
   implicit def editableString(str: String) = Editable[String](str)
}

case class Feed(id: Long,
                applications: Seq[Application] = Seq[Application]())
object Feed {
   def apply(feedModel: FeedModel) =
      new Feed(feedModel.id,
         feedModel.applications.map({applicationModel: ApplicationModel => Application(applicationModel)}).toSeq)
}
class FeedModel private() extends Record[FeedModel] with KeyedRecord[Long] {
   override def meta = FeedModel

   val idField = new LongField(this)
   lazy val applications = FeedrSchema.feedToApplications.left(this)
}
object FeedModel extends FeedModel with MetaRecord[FeedModel]

case class Application(name: Editable[String],
                       version: Editable[String] = Editable(""),
                       description: Editable[String] = Editable(""),
                       private var _id: String = randomUUID.toString) {
   def id = _id
}
object Application {
   def apply(applicationModel: ApplicationModel) = {
      val appName = Editable(applicationModel.name.value)
      val appVersion = Editable(applicationModel.name.value)
      val appDescription = Editable(applicationModel.name.value)
      new Application(appName, appVersion, appDescription)
   }
}
class ApplicationModel private() extends Record[ApplicationModel] with KeyedRecord[String] {
   override def meta = ApplicationModel

   val idField = new StringField(this, 36) {
      override val defaultValue = UUID.randomUUID.toString
   }
   val name = new StringField(this, 100)
   val version = new StringField(this, 100)
   val description = new StringField(this, 256)
   val feedId = new LongField(this)
   lazy val feed = FeedrSchema.feedToApplications.right(this)
}
object ApplicationModel extends ApplicationModel with MetaRecord[ApplicationModel]

object FeedrSchema extends Schema {
   val feeds = table[FeedModel]
   val applications = table[ApplicationModel]
   val feedToApplications =
      oneToManyRelation(feeds, applications).via((feed, app) => feed.id === app.feedId)
}
