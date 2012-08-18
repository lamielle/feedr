package feedr.lib

import net.liftweb.actor.LiftActor
import net.liftweb.common.{Full, Logger}
import net.liftweb.http.ListenerManager

import org.scalastuff.scalabeans.Preamble._
import org.scalastuff.scalabeans.PropertyDescriptor

import feedr.lib.FeedManager.{ApplicationEdited, EditApplication, ApplicationAdded, RequestFeed, NewApplication}
import feedr.model.{Editable, Application, Feed}
import feedr.model.Helpers._

object FeedManager {
   // Messages a FeedManager instance responds to
   case class RequestFeed()
   case class NewApplication()
   case class EditApplication[T](id: String, property: PropertyDescriptor, value: T)

   // Messages listeners of FeedManager instances need to respond to
   case class ApplicationAdded(application: Application, feed: Feed)
   case class ApplicationEdited(application: Application, property: PropertyDescriptor, feed: Feed)
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

// One FeedManager per feed: manages modifications of the feed it represents
class FeedManager(private var mFeed: Feed) extends LiftActor with ListenerManager with Logger {
   override def lowPriority = {
      case RequestFeed() => {
         debug("Message received: FeedManager(%s)::RequestFeed()".format(mFeed.id))
         reply(Full(mFeed))
      }
      case NewApplication() => {
         debug("Message received: FeedManager(%s)::NewApplication()".format(mFeed.id))
         val newApplication = Application("")
         mFeed = Feed(mFeed.id, newApplication :: mFeed.applications)
         updateListeners(ApplicationAdded(newApplication, mFeed))
      }
      case EditApplication(id, property, value) => {
         debug("Message received: FeedManager(%s)::EditApplicationName(%s, %s, %s)".format(mFeed.id, id, property, value))
         // TODO: Replace linear search through the applications list with
         // something better if needed.
         val optionApp = mFeed.applications find {app: Application => app.id == id}
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
         }
      }
   }

   // Not used, all updates to listeners are sent when updateListeners is called
   override def createUpdate = AnyRef
}
