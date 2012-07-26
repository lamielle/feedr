package feedr.lib

import net.liftweb.actor.LiftActor
import feedr.lib.FeedManager.{RequestFeed, NewApplication}
import feedr.model.{Application, Feed}
import net.liftweb.common.{Full, Logger}

object FeedManager {
  case class RequestFeed()
  case class NewApplication()
}

// One FeedManager per feed: manages modifications of the feed it represents
class FeedManager(private var mFeed: Feed) extends LiftActor with Logger {
  override def messageHandler = {
    case RequestFeed() => {
      debug("Message received: FeedManager(%s)::RequestFeed()".format(mFeed.id))
      reply(Full(mFeed))
    }
    case NewApplication() => {
      debug("Message received: FeedManager(%s)::NewApplication()".format(mFeed.id))
      mFeed = Feed(mFeed.id, Application("", "", "") :: mFeed.applications)
    }
    case error => reply("Error: %s".format(error))
  }
}
