package feedr.lib

import net.liftweb.actor.LiftActor

// One FeedManager per feed: manages modifications of the feed it represents
class FeedManager(feedId: String) extends LiftActor {
  case class NewApplication()

  override def messageHandler = {
    case NewApplication() => Unit
    case error => reply("Error: %s".format(error))
  }
}
