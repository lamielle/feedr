package feedr.lib

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import feedr.lib.FeedsManager.{NewFeed, RequestFeedManager}
import net.liftweb.common.{Full, Empty}
import feedr.lib.FeedManager.RequestFeedId

class FeedsManagerSpec extends FlatSpec with FeedrDBSetup with ShouldMatchers {
   "FeedsManager" should "initally return Empty for requested FeedManagers" in {
      FeedsManager !! RequestFeedManager(0) should equal (Empty)
      FeedsManager !! RequestFeedManager(1) should equal (Empty)
      FeedsManager !! RequestFeedManager(-1) should equal (Empty)
      FeedsManager !! RequestFeedManager(99) should equal (Empty)
   }

   it should "provide a new feed" in {
      FeedsManager !! NewFeed() should equal (Full(1))
   }

   it should "provide a FeedManager for the first new feed" in {
      FeedsManager !! RequestFeedManager(1) match {
         case Full(Full(feedManager: FeedManager)) => {
            feedManager !! RequestFeedId() should equal (Full(1))
         }
         case _ => fail()
      }
   }

   it should "provide more new feeds" in {
      FeedsManager !! NewFeed() should equal (Full(2))
      FeedsManager !! NewFeed() should equal (Full(3))
      FeedsManager !! NewFeed() should equal (Full(4))
      FeedsManager !! NewFeed() should equal (Full(5))
   }

   it should "provide FeedManagers for other feeds" in {
      for (feedId <- 2 until 6) {
         FeedsManager !! RequestFeedManager(feedId) match {
            case Full(Full(feedManager: FeedManager)) => {
               feedManager !! RequestFeedId() should equal (feedId)
            }
            case _ => fail()
         }
      }
   }
}
