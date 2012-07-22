package feedr.snippet

import net.liftweb.util.Helpers._
import feedr.lib.FeedsManager
import feedr.lib.FeedsManager.NewFeed
import net.liftweb.http.{S, SHtml}

class FeedrMain {
  def renderNewFeedButton =
    "#new-feed-button [onclick]" #> SHtml.ajaxInvoke(() => {
      val feedId = FeedsManager !! NewFeed()
      S.redirectTo("/" + feedId.get)})
}
