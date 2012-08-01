package feedr.snippet

import net.liftweb.util.Helpers._
import net.liftweb.http.{S, SHtml}

import feedr.lib.FeedsManager
import feedr.lib.FeedsManager.NewFeed

class FeedrMain {
   def renderNewFeedButton =
      "#new-feed-button [onclick]" #> SHtml.ajaxInvoke(() => {
         val feedId = FeedsManager !! NewFeed()
         S.redirectTo("/" + feedId.get)
      })
}
