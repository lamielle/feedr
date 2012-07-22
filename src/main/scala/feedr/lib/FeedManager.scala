package feedr.lib

import net.liftweb.actor.LiftActor
import feedr.lib.FeedManager.{RequestFeed, NewApplication}
import feedr.model.Feed
import net.liftweb.common.{Full, Logger}
import feedr.comet.FeedEditor.ProvideFeed

object FeedManager {
  case class RequestFeed(actor: LiftActor)
  case class NewApplication()
}

// One FeedManager per feed: manages modifications of the feed it represents
class FeedManager(feed: Feed) extends LiftActor with Logger {
  override def messageHandler = {
    case RequestFeed(actor: LiftActor) => {
      debug("Message received: FeedManager(%s)::RequestFeed()".format(feed.id))
      actor ! ProvideFeed(Full(feed))
    }
    case NewApplication() =>
    case error => reply("Error: %s".format(error))
  }
}
