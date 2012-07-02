package feedr.snippet

import _root_.net.liftweb.http._
import net.liftweb.util._
import Helpers._
import feedr.lib.FeedManager
import feedr.lib.FeedManager.NewFeed

class FeedrMain {
  def renderNewFeedButton =
    "#new-feed-button [onclick]" #> SHtml.ajaxInvoke(() => {
      val feedId = FeedManager !! NewFeed()
      S.redirectTo("/" + feedId.get)})
}
