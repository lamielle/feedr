package feedr.comet

import net.liftweb.common.{Logger, Box, Empty, Full}
import net.liftweb.http.{Templates, SHtml, CometActor}

import feedr.lib.{FeedManager, FeedsManager}
import net.liftweb.http.js.jquery.JqJsCmds.AppendHtml
import xml.NodeSeq
import feedr.model.Feed
import feedr.comet.FeedEditor.UseFeed
import feedr.lib.FeedManager.RequestFeed
import feedr.comet.FeedEditor.ProvideFeed
import feedr.lib.FeedsManager.RequestFeedManager
import feedr.model.Application
import feedr.comet.FeedEditor.ProvideFeedManager

object FeedEditor {
  case class UseFeed(feedId: String)
  case class ProvideFeedManager(feedManager: Box[FeedManager])
  case class ProvideFeed(feed: Box[Feed])

  // Constructs the name of a feed editor comet actor given a feed id
  def nameForEditor(feedId: String) = "feed-" + feedId
  def nameOfClass = "FeedEditor"
}

class FeedEditor extends CometActor with Logger {
  private var mFeedManager: Box[FeedManager] = Empty
  private var mFeed: Box[Feed] = Empty

  // Render the feed editor
  def render =
    // Add the name to the feed editor for debugging purposes
    "#feed-id" #> this.name &
    // Render the add-application button:
    // When it is clicked, embed a new application editor snippet
    "#add-application [onclick]" #> SHtml.ajaxInvoke(() =>
      AppendHtml("applications-list",
          <div class="lift:embed?what=application-editor"/>)) &
    "#applications-list-items" #> renderApplications(mFeed)

  override def lowPriority = {
    case UseFeed(feedId) => {
      debug("Message received: FeedEditor(%s)::UseFeed(%s)".format(name, feedId))
      mFeedManager match {
        case Full(_) => debug("Already using feed with ID %s".format(feedId))
        case _ => FeedsManager ! RequestFeedManager(feedId, this)
      }
    }
    // TODO: This weird typed request/response message design seems like it's trying to hard.
    // Need a way to have typed responses between actors, this was the only way I could find.
    case ProvideFeedManager(feedManager : Box[FeedManager]) => {
      debug("Message received: FeedEditor(%s)::ProvideFeedManager(%s)".format(name, feedManager))
      mFeedManager = feedManager
      mFeedManager.map(_ ! RequestFeed(this))
    }
    case ProvideFeed(feed: Box[Feed]) => {
      debug("Message received: FeedEditor(%s)::ProvideFeed(%s)".format(name, feed))
      mFeed = feed
      // XXX: Use a few hardcoded feeds/applications for now until editing is done
      mFeed = Full(Feed("iD!", Application("App1", "1.0", "blargh desc") ::
        Application("App1", "2.0", "boom") :: Nil))
      reRender()
    }
    case error => {
      debug("Message received: FeedEditor(%s): Unknown message type: %s".format(name, error))
      reply("Error: %s".format(error))
    }
  }

  // Return a function that can render the given application.
  private def renderApplication(app: Application) =
    "#application-name [value]" #> app.name &
    "#application-description [value]" #> app.description &
    "#application-version [value]" #> app.version

  // Render each application in the given feed.
  private def renderApplications(feed: Box[Feed]): Seq[NodeSeq] =
    feed match {
      // A feed has been provided for this editor.
      case Full(currFeed) =>
        currFeed.applications.map {
          currApp: Application =>
            renderApplication(currApp).apply(
              Templates("application-editor" :: Nil).getOrElse(<div/>))
        }
      // No feed available yet.
      case _ => <div/>
    }
}
