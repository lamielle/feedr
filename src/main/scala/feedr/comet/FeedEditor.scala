package feedr.comet

import net.liftweb.http._
import js.jquery.JqJsCmds.AppendHtml
import net.liftweb.common.{Logger, Empty, Box}

import feedr.model.Feed
import feedr.lib.FeedsManager
import feedr.lib.FeedsManager.RequestFeed
import feedr.comet.FeedEditor.{ProvideFeed, UseFeed}

object FeedEditor {
  case class UseFeed(feedId: String)
  case class ProvideFeed(feedReply: Box[Feed])

  // Constructs the name of a feed editor comet actor given a feed id
  def nameForEditor(feedId: String) = "feed-" + feedId
  def nameOfClass = "FeedEditor"
}

class FeedEditor extends CometActor with Logger {
  private var feed: Box[Feed] = Empty

  // Render the feed editor
  def render =
    // Add the name to the feed editor for debugging purposes
    "#feed-name" #> name &
    // Render the add-application button:
    // When it is clicked, embed a new application editor snippet
    "#add-application [onclick]" #> SHtml.ajaxInvoke(() =>
      AppendHtml("applications-list",
          <div class="lift:embed?what=application-editor"/>))

  override def lowPriority = {
    case UseFeed(feedId) => {
      FeedsManager ! RequestFeed(feedId, this)
    }
    case ProvideFeed(feedReply) => {
      val blargh = "boom"
      feed = feedReply
    }
    case error => reply("Error: %s".format(error))
  }
}
