package feedr.comet

import net.liftweb.common.{Logger, Box, Empty, Full}
import net.liftweb.http.{Templates, SHtml, CometActor}

import feedr.lib.{FeedManager, FeedsManager}
import xml.NodeSeq
import feedr.model.Feed
import feedr.comet.FeedEditor.UseFeed
import feedr.lib.FeedManager.{NewApplication, RequestFeed}
import feedr.lib.FeedsManager.RequestFeedManager
import feedr.model.Application
import net.liftweb.http.js.JsCmds

object FeedEditor {
  case class UseFeed(feedId: String)

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
    "#add-application [onclick]" #> SHtml.ajaxInvoke(() => OnClickAddApplication()) &
    // Render the list of applications
    "#applications-list-items" #> RenderApplications(mFeed)

  override def lowPriority = {
    case UseFeed(feedId) => {
      debug("Message received: FeedEditor(%s)::UseFeed(%s)".format(name, feedId))
      mFeedManager match {
        case Full(_) => debug("Already using feed with ID %s".format(feedId))
        case _ => InitForFeed(feedId)
      }
    }
    case error => {
      debug("Message received: FeedEditor(%s): Unknown message type: %s".format(name, error))
      reply("Error: %s".format(error))
    }
  }

  private def OnClickAddApplication() {
    mFeedManager match {
      case Full(feedManager) => {
        feedManager ! NewApplication()
        JsCmds.Noop
      }
      case _ => JsCmds.Noop
    }
  }

  // Initialize this feed editor comet actor to use the feed with the given id.
  private def InitForFeed(feedId: String) {
    // Request the feed manager for the given feed id by sending the FeedsManager
    // singleton a request message and waiting for a response.
    FeedsManager !! RequestFeedManager(feedId) match {
      // Match based on case classes instead of type parameters, otherwise
      // the matching fails due to type erasure.
      case Full(feedManagerBox: Box[FeedManager]) => {
        mFeedManager = feedManagerBox
        for {feedManager <- mFeedManager} RequestFeedFromManager(feedManager)
      }
      case m => debug("Unknown reply from FeedsManager for feed manager: %s".format(m))
    }
  }

  // Request the feed that the given manager manages
  private def RequestFeedFromManager(feedManager: FeedManager) {
    feedManager !! RequestFeed() match {
      // Match based on case classes instead of type parameters, otherwise
      // the matching fails due to type erasure.
      case Full(Full(feed: Feed)) => {
        mFeed = Full(feed)
        // XXX: Use a few hardcoded feeds/applications for now until editing is done
        mFeed = Full(Feed("iD!", Application("App1", "1.0", "blargh desc") ::
          Application("App1", "2.0", "boom") :: Nil))
        reRender()
      }
      case m => debug("Unknown reply from FeedManager(%s) for feed: %s".format(name, m))
    }
  }

  /*
  // Do something similar with partial update when a new app is added
  AppendHtml("applications-list",
      <div class="lift:embed?what=application-editor"/>)
  */

  // Return a function that can render the given application.
  private def RenderApplication(app: Application) =
    "#application-name [value]" #> app.name &
    "#application-description [value]" #> app.description &
    "#application-version [value]" #> app.version

  // Render each application in the given feed.
  private def RenderApplications(feed: Box[Feed]): Seq[NodeSeq] =
    feed match {
      // A feed has been provided for this editor.
      case Full(currFeed) =>
        currFeed.applications.map {
          currApp: Application =>
            RenderApplication(currApp).apply(
              Templates("application-editor" :: Nil).getOrElse(<div/>))
        }
      // No feed available yet.
      case _ => <div/>
    }
}
