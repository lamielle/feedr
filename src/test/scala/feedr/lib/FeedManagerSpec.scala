package feedr.lib

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import feedr.lib.FeedsManager.{RequestFeedManager, NewFeed}
import feedr.lib.FeedManager.{RequestFeed, RequestFeedId}
import net.liftweb.common.Full
import feedr.model.Feed

class FeedManagerSpec extends FlatSpec with FeedrDBSetup with ShouldMatchers {
   // Create a new feed and return its ID and manager
   def newFeedAndManager = {
      FeedsManager !! NewFeed() match {
         case Full(feedId: Long) =>
            FeedsManager !! RequestFeedManager(feedId) match {
               case Full(Full(feedManager: FeedManager)) =>
                  (feedId, feedManager)
               case other => fail("Unexpected response from feed manager request: %s".format(other))
            }
         case other => fail("Unexpected response from new feed request: %s".format(other))
      }
   }

   "FeedManager" should "provide the ID of the feed it manages" in {
      // TODO: look up syntax for tuple unpacking
      val feedIdAndManager = newFeedAndManager
      feedIdAndManager._2 !! RequestFeedId() should equal (feedIdAndManager._1)
   }

   it should "provide the feed that it manages" in {
      val feedIdAndManager = newFeedAndManager
      feedIdAndManager._2 !! RequestFeed() match {
         case Full(Full(feed: Feed)) => feed.id should equal (feedIdAndManager._1)
         case other => fail("No feed provided: %s".format(other))
      }
   }
}
