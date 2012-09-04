package feedr.lib

import net.liftweb.actor.LiftActor
import net.liftweb.common.{Full, Empty, Logger}
import net.liftweb.http.ListenerManager
import net.liftweb.squerylrecord.RecordTypeMode._

import org.scalastuff.scalabeans.Preamble._
import org.scalastuff.scalabeans.PropertyDescriptor

import feedr.lib.FeedManager.{RequestFeedId, NewApplication, RequestFeed, EditApplication}
import feedr.model.{FeedModel, FeedrSchema, Application, Feed}

object FeedManager {
   // Messages a FeedManager instance responds to
   case class RequestFeedId()
   case class RequestFeed()
   case class NewApplication()
   case class EditApplication[T](id: String, property: PropertyDescriptor, value: T)

   // Messages listeners of FeedManager instances need to respond to
   case class ApplicationAdded(application: Application)
   case class ApplicationEdited(application: Application, property: PropertyDescriptor)
}

// Custom builder that copies all fields of the given object
object FeedrBeanBuilder {
   val logger = Logger(this.getClass)
   def newBuilder[T <: AnyRef](copy: T)(implicit mf: Manifest[T]) = {
      logger.debug("newBuilder for %s".format(copy))
      val desc = descriptorOf[T](manifest[T])
      val builder = desc.newBuilder()
      for (property <- desc.properties) {
         logger.debug("setting property %s to value %s".format(property, desc.get(copy, property.name)))
         builder.set(property, desc.get(copy, property.name))
      }
      builder
   }
}

// One FeedManager per feed: manages modifications of the feed it represents.
class FeedManager(private var mFeedId: Long) extends LiftActor with ListenerManager with Logger {
   override def lowPriority = {
      case RequestFeedId() => {
         debug("Message received: FeedManager(%s)::RequestFeedId()".format(mFeedId))
         reply(mFeedId)
      }
      case RequestFeed() => {
         debug("Message received: FeedManager(%s)::RequestFeed()".format(mFeedId))
         // Query for the feed with id mFeedId and return it as a Feed if available
         // or return Empty if the feed doesn't exist.
         transaction {
            reply(from(FeedrSchema.feeds)(feed =>
               where(feed.id === mFeedId) select(feed)).headOption match {
               case Some(feedModel: FeedModel) => Full(Feed(feedModel))
               case _ => Empty
            })
         }
      }
      case NewApplication() => {
         debug("Message received: FeedManager(%s)::NewApplication()".format(mFeedId))
         //TODO: Reimplement adding new applications.
         //val newApplication = Application("")
         //mFeed = Feed(mFeed.id, newApplication :: mFeed.applications)
         //updateListeners(ApplicationAdded(newApplication, mFeed))
      }
      case EditApplication(id, property, value) => {
         debug("Message received: FeedManager(%s)::EditApplicationName(%s, %s, %s)".format(mFeedId, id, property, value))
         // TODO: Replace linear search through the applications list with
         // something better if needed.
         // TODO: Reimplement application editing.
         /*val optionApp = mFeed.applications find {app: Application => app.id == id}
         optionApp match {
            case Some(app: Application) => {
               val builder = FeedrBeanBuilder.newBuilder(app)
               builder.set(property, Editable(value))
               // Build the modified application.
               val result = builder.result()
               val newApp = result.asInstanceOf[Application]
               // Replace the old application with the new one.
               val newAppsList = mFeed.applications.map(curApp => if (curApp == app) newApp else curApp)
               mFeed = Feed(mFeed.id, newAppsList)
               updateListeners(ApplicationEdited(newApp, property, mFeed))
            }
            case _ => debug("No application with id %s found!".format(id))
         }*/
      }
   }

   // Not used, all updates to listeners are sent when updateListeners is called
   override def createUpdate = AnyRef
}
