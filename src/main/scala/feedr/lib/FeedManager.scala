package feedr.lib

import net.liftweb.actor.LiftActor
import feedr.lib.FeedManager.{ApplicationAdded, RequestFeed, NewApplication}
import feedr.model.{Application, Feed}
import net.liftweb.common.{Full, Logger}
import net.liftweb.http.ListenerManager

object FeedManager {
  case class RequestFeed()
  case class NewApplication()
  case class ApplicationAdded(application: Application, feed: Feed)
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
      val newApplication = Application("", "", "")
      mFeed = Feed(mFeed.id, newApplication :: mFeed.applications)
      updateListeners(ApplicationAdded(newApplication, mFeed))
    }
  }

  // Not used, all updates to listeners are sent when updateListeners is called
  override def createUpdate = AnyRef
}
