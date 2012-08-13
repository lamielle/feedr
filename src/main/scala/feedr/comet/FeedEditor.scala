package feedr.comet

import xml.NodeSeq

import net.liftweb.common.{Logger, Box, Empty, Full}
import net.liftweb.http.{RemoveAListener, Templates, SHtml, CometActor, AddAListener}
import net.liftweb.http.js.jquery.JqJsCmds.PrependHtml
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds.SetValById

import feedr.lib.{FeedManager, FeedsManager}
import feedr.model.Feed
import feedr.comet.FeedEditor.UseFeed
import feedr.lib.FeedManager.{ApplicationNameEdited, EditApplicationName, ApplicationAdded, NewApplication, RequestFeed}
import feedr.lib.FeedsManager.RequestFeedManager
import feedr.model.Application

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
      // Render the newly added application and update our copy of the feed state
      case ApplicationAdded(application: Application, feed: Feed) => {
         debug("Message received: FeedEditor(%s)::ApplicationAdded(%s, %s)".format(name, application, feed))
         mFeed = Full(feed)
         partialUpdate(PrependHtml("applications-list",
            RenderApplication(application).apply(
               Templates("application-editor" :: Nil).getOrElse(<div/>)))
         )
      }
      case ApplicationNameEdited(application: Application, feed: Feed) => {
         debug("Message received: FeedEditor(%s)::ApplicationNameEdited(%s, %s)".format(name, application, feed))
         mFeed = Full(feed)
         partialUpdate(SetValById("application-name-%s".format(application.id),
            application.name
         ))
      }
      case error => {
         debug("Message received: FeedEditor(%s): Unknown message type: %s".format(name, error))
         reply("Error: %s".format(error))
      }
   }

   // Handler for when add application is clicked.
   private def OnClickAddApplication() =
      mFeedManager match {
         case Full(feedManager: FeedManager) => {
            feedManager ! NewApplication()
            JsCmds.Noop
         }
         case _ => JsCmds.Noop
      }

   // Handler when an application name field loses focus.
   private def OnBlurApplicationName(appId: String, value: String) =
      mFeedManager match {
         case Full(feedManager: FeedManager) => {
            feedManager ! EditApplicationName(appId, value)
            JsCmds.Noop
         }
         case _ => JsCmds.Noop
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
            for {feedManager <- mFeedManager} {
               feedManager ! AddAListener(this)
               RequestFeedFromManager(feedManager)
            }
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
            reRender()
         }
         case m => debug("Unknown reply from FeedManager(%s) for feed: %s".format(name, m))
      }
   }

   // Return a function that can render the given application.
   private def RenderApplication(app: Application) =
      "#application-name [id]" #> "application-name-%s".format(app.id) &
         "#application-name [value]" #> app.name &
         "#application-description [value]" #> app.description &
         "#application-version [value]" #> app.version &
         "#application-name [onblur]" #> SHtml.onEvent(s => OnBlurApplicationName(app.id, s))

   // Render each application in the given feed.
   private def RenderApplications(feed: Box[Feed]): Seq[NodeSeq] =
      feed match {
         // A feed has been provided for this editor.
         case Full(currFeed) =>
            currFeed.applications.map {
               currApp: Application =>
                  RenderApplication(currApp)(
                     Templates("application-editor" :: Nil).getOrElse(<div/>))
            }
         // No feed available yet.
         case _ => <div/>
      }

   override def localShutdown() {
      for (feedManager <- mFeedManager) feedManager ! RemoveAListener(this)
   }
}
