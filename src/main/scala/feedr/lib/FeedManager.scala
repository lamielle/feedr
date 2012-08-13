package feedr.lib

import net.liftweb.actor.LiftActor
import net.liftweb.common.{Full, Logger}
import net.liftweb.http.ListenerManager

import feedr.lib.FeedManager.{ApplicationNameEdited, EditApplicationName, ApplicationAdded, RequestFeed, NewApplication}
import feedr.model.{Application, Feed}

object FeedManager {
   // Messages a FeedManager instance responds to
   case class RequestFeed()
   case class NewApplication()
   case class EditApplicationName(id: String, name: String)

   // Messages listeners of FeedManager instances need to respond to
   case class ApplicationAdded(application: Application, feed: Feed)
   case class ApplicationNameEdited(application: Application, feed: Feed)
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
         val newApplication = Application.createApplication
         mFeed = Feed(mFeed.id, newApplication :: mFeed.applications)
         updateListeners(ApplicationAdded(newApplication, mFeed))
      }
      case EditApplicationName(id, name) => {
         debug("Message received: FeedManager(%s)::EditApplicationName(%s, %s)".format(mFeed.id, id, name))
         // TODO: Replace linear search through the applications list with
         // something better if needed.
         val optionApp = mFeed.applications find {app: Application => app.id == id}
         optionApp match {
            case Some(app) => {
               // Build the modified application.
               val newApp = Application(app.id, name, app.version, app.description)
               // Replace the old application with the new one.
               val newAppsList = mFeed.applications.map(curApp => if (curApp == app) newApp else curApp)
               mFeed = Feed(mFeed.id, newAppsList)
               updateListeners(ApplicationNameEdited(newApp, mFeed))
            }
            case _ => debug("No application with id %s found!".format(id))
         }
      }
   }

   // Not used, all updates to listeners are sent when updateListeners is called
   override def createUpdate = AnyRef
}
