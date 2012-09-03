package feedr.lib

import net.liftweb.actor.LiftActor
import net.liftweb.common.{Full, Empty, Logger}
import net.liftweb.squerylrecord.RecordTypeMode._

import feedr.model.{FeedModel, FeedrSchema}

// Singleton actor responsible for creating new feeds and feed managers
object FeedsManager extends LiftActor with Logger {
   case class NewFeed()
   case class RequestFeedManager(feedId: Long)

   private var feedManagers: Map[Long, FeedManager] = Map.empty

   override def messageHandler = {
      case NewFeed() => {
         debug("Message received: FeedsManager::NewFeed()")
         reply(newFeed())
      }
      case RequestFeedManager(feedId) => {
         debug("Message received: FeedsManager::RequestFeedManager(%s)".format(feedId))
         reply(feedManagers get feedId map(Full(_)) openOr Empty)
      }
      case error => {
         debug("Message received: FeedsManager: Unknown message type: %s".format(error))
         reply("Error: %s".format(error))
      }
   }

   // Create a new feed and a manager for that feed.  Returns the ID of the feed
   // what was created.
   private def newFeed() = {
      val feedId = createFeed()
      feedManagers += feedId -> new FeedManager(feedId)
      debug("Added new feed: %s".format(feedId))
      feedId
   }

   // Create a new feed record and return its ID.
   private def createFeed(): Long = {
      transaction {
         FeedrSchema.feeds.insert(FeedModel.createRecord).id
      }
   }
}
