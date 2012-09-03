package feedr.snippet

import xml.NodeSeq

import net.liftweb.util.Helpers._
import net.liftweb.http.S
import net.liftweb.common.{Failure, Full}

import feedr.comet.FeedEditor
import feedr.comet.FeedEditor.UseFeed

class FeedEditorInserter {
   def render = {
      // Grab the feedId session parameter that is based on the URL.
      // Boot.scala defines this parameter.
      S.param("feedId").map((feedIdString) => {
         // Try to convert the parameter to a Long, the URL is invalid if this fails.
         // TODO: Add a better error page for when the URL parameter is not valid.
         (try {
            Full(feedIdString.toLong)
         } catch {
            case ex : java.lang.NumberFormatException => Failure("Invalid feed id parameter: %s".format(feedIdString), Some(ex), None)
         }).map((feedId) => {
            S.session.map((session) => {
               // Send a message to the actor for this feed to use the feed.
               // This will result in the actor being created if it doesn't already exist.
               session.sendCometActorMessage(
                  FeedEditor.nameOfClass,
                  Full(FeedEditor.nameForEditor(feedId)),
                  UseFeed(feedId)
               )

               // Wrap the comet actor around the feed editor portion of the page.
               "#feed-editor" #> ((ns: NodeSeq) =>
                  <div id="feed-editor"
                       class={"lift:comet?type=" + FeedEditor.nameOfClass +
                          "&amp;name=" + FeedEditor.nameForEditor(feedId)}>
                     {ns}
                  </div>)
            }) openOr <span>Error: no lift session in the request?!?</span>
         }) openOr <span>Error: feedId parameter not a long.</span>
      }) openOr <span>Error: feedId parameter not provided.</span>
   }
}
