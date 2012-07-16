package feedr.snippet

import net.liftweb.util.Helpers._
import net.liftweb.http.S
import net.liftweb.common.Full
import feedr.comet.FeedEditor.UseFeed
import xml.NodeSeq
import feedr.comet.FeedEditor

class FeedEditorInserter {
  def render = {
    // Grab the feedId session parameter.
    S.param("feedId").map((feedId) => {
      S.session.map((session) => {
        // Send a message to the actor for this feed to use the feed.
        // This will result in the actor being created if it doesn't already exist.
        session.sendCometActorMessage(
          FeedEditor.nameOfClass,
          Full(FeedEditor.nameForEditor(feedId)),
          UseFeed(feedId)
        )

        // Wrap the comet actor around the feed editor.
        "#feed-editor" #> ((ns: NodeSeq) =>
          <div id="feed-editor"
               class={"lift:comet?type="+ FeedEditor.nameOfClass +
                 "&amp;name=" + FeedEditor.nameForEditor(feedId)}>{ns}
          </div>)
      }) openOr <span>Error: no lift session in the request?!?</span>
    }) openOr <span>Error: feedId parameter not provided</span>
  }
}
