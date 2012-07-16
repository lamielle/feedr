package feedr.snippet

import _root_.net.liftweb.http._
import net.liftweb.util._
import Helpers._
import feedr.lib.FeedsManager
import feedr.lib.FeedsManager.NewFeed

class FeedrMain {
  def renderNewFeedButton =
    "#new-feed-button [onclick]" #> SHtml.ajaxInvoke(() => {
      val feedId = FeedsManager !! NewFeed()
      S.redirectTo("/" + feedId.get)})
}
