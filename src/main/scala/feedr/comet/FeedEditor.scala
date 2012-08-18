package feedr.comet

import xml.NodeSeq

import net.liftweb.common.{Logger, Box, Empty}
import net.liftweb.common.Full
import net.liftweb.http.{Templates, SHtml, CometActor, AddAListener}
import net.liftweb.http.js.jquery.JqJsCmds.PrependHtml
import net.liftweb.http.js.JsCmds
import net.liftweb.http.RemoveAListener
import net.liftweb.http.js.JsCmds.SetValById
import net.liftweb.util.CssSel

import org.scalastuff.scalabeans.Preamble._
import org.scalastuff.scalabeans.PropertyDescriptor

import feedr.lib.{FeedManager, FeedsManager}
import feedr.model.{Editable, Application, Feed}
import feedr.lib.FeedManager.{ApplicationEdited, EditApplication, NewApplication, ApplicationAdded, RequestFeed}
import feedr.lib.FeedsManager.RequestFeedManager
import feedr.comet.FeedEditor.UseFeed

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
      case ApplicationEdited(application: Application, property: PropertyDescriptor, feed: Feed) => {
         debug("Message received: FeedEditor(%s)::ApplicationNameEdited(%s, %s)".format(name, application, feed))
         mFeed = Full(feed)
         val updatedValueEditable = descriptorOf[Application].get(application, property.name).asInstanceOf[Editable[String]]
         partialUpdate(SetValById("application-%s-%s".format(property.name, application.id),
            updatedValueEditable.value.toString
         ))
      }
      case error => {
         debug("Message received: FeedEditor(%s): Unknown message type: %s".format(name, error))
         reply("Error: %s".format(error))
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

   // Handler for when add application is clicked.
   private def OnClickAddApplication() =
      mFeedManager match {
         case Full(feedManager: FeedManager) => {
            feedManager ! NewApplication()
            JsCmds.Noop
         }
         case _ => JsCmds.Noop
      }

   // Handler when an application field loses focus.
   private def OnBlurApplicationField(appId: String, property: PropertyDescriptor, value: String) =
      mFeedManager match {
         case Full(feedManager: FeedManager) => {
            feedManager ! EditApplication(appId, property, value)
            JsCmds.Noop
         }
         case _ => JsCmds.Noop
      }

   // Generates a css selector that defines the id property for each editable
   // field of the given application.
   private def IDSelectors(app: Application): CssSel =
      descriptorOf[Application].properties.filter {
         property: PropertyDescriptor =>
            property.scalaType.equals(
            descriptorOf[Editable[String]].beanType)
      } map {
         property: PropertyDescriptor =>
            "#application-%s [id]".format(property.name) #> "application-%s-%s".format(property.name, app.id)
      } reduce {
         (sel1: CssSel, sel2: CssSel) => sel1 & sel2
      }

   // Generate a css selector that inserts the values of each editable field of
   // the given application in the proper field in the DOM.
   private def ValueSelectors(app: Application): CssSel =
      descriptorOf[Application].properties.filter {
         property: PropertyDescriptor =>
            property.scalaType.equals(
               descriptorOf[Editable[String]].beanType)
      } map {
         property: PropertyDescriptor =>
            "#application-%s [value]".format(property.name) #>
               descriptorOf[Application].get(app, property.name).asInstanceOf[Editable[String]].value
      } reduce {
         (sel1: CssSel, sel2: CssSel) => sel1 & sel2
      }

   // Generates selectors for editable fields of Application that send a
   // message to the FeedManager that the field has been edited.
   private def OnBlurSelectors(app: Application): CssSel =
      descriptorOf[Application].properties.filter {
         property: PropertyDescriptor =>
            property.scalaType.equals(
               descriptorOf[Editable[String]].beanType)
      } map {
         property: PropertyDescriptor =>
            "#application-%s [onblur]".format(property.name) #>
               SHtml.onEvent(s => OnBlurApplicationField(app.id, property, s))
      } reduce {
         (sel1: CssSel, sel2: CssSel) => sel1 & sel2
      }

   // Return a function that can render the given application.
   private def RenderApplication(app: Application): CssSel =
      IDSelectors(app) & ValueSelectors(app) & OnBlurSelectors(app)

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
