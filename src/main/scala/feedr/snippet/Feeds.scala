package feedr.snippet


import _root_.net.liftweb.http._
import net.liftweb.util._
import Helpers._
import feedr.lib.FeedManager
import feedr.lib.FeedManager.NewFeed

class Feeds {
  def redirectTest =
    "#redirect [onclick]" #> SHtml.ajaxInvoke(() => {
      //val feedId: Box[String] = (FeedManager !! NewFeed())
      S.redirectTo("/")}) // + (FeedManager !! NewFeed()).flatMap(_).asA[util.UUID]))
}
