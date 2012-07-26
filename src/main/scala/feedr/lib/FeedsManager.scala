package feedr.lib

import net.liftweb.actor.LiftActor
import net.liftweb.common.{Empty, Full, Logger}
import feedr.model.Feed

// Singleton actor responsible for creating new feeds and feed managers
object FeedsManager extends LiftActor with Logger {
  case class NewFeed()
  case class RequestFeedManager(feedId: String)

  private var feedManagers: Map[String, FeedManager] = Map.empty
  private var feedCounter: Long = 0

  override def messageHandler = {
    case NewFeed() => reply(newFeed())
    case RequestFeedManager(feedId) => {
      val feedManagerOption = feedManagers get feedId
      val feedManagerBox = feedManagerOption.map(Full(_)) openOr Empty
      reply(feedManagerBox)
    }
    case error => {
      debug("Message received: FeedsManager: Unknown message type: %s".format(error))
      reply("Error: %s".format(error))
    }
  }

  private def nextUniqueFeedId() = {
    feedCounter += 1
    feedCounter.toHexString
  }

  private def newFeed() = {
    val feedId = nextUniqueFeedId()
    feedManagers += feedId -> new FeedManager(Feed(feedId))
    debug("Added new feed: %s".format(feedId))
    feedId
  }
}
