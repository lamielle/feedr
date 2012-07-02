package feedr.snippet

import net.liftweb._
import http._
import js.jquery.JqJsCmds.AppendHtml
import js.JsCmds.{SetHtml, Script}

import _root_.net.liftweb.http._
import js._
import net.liftweb.util._
import Helpers._
import xml.NodeSeq

class FeedEditor {
  // Include JS code to serialize the form to JSON
  // Note that head tags are merged into the single head tag at the top of the
  // rendered page
  def head = Script(processFeed.jsCmd)

  def redirectTest =
    "#redirect [onclick]" #> SHtml.ajaxInvoke(() =>
      S.redirectTo("/static/index"))

  // Render the feed editor
  def render =
    "#feed-editor" #> ((ns: NodeSeq) => SHtml.jsonForm(processFeed, ns))

  def renderFeedName =
    "#feed-name" #> <span>{S.param("feedId").getOrElse("Invalid Feed")}</span>

  // Render the add-application button:
  // When it is clicked, embed a new application editor snippet
  def renderAddApplication =
    "#add-application [onclick]" #> SHtml.ajaxInvoke(() =>
      AppendHtml("applications-list",
          <div class="lift:embed?what=application-editor"/>))

  // Process feed editor submission
  object processFeed extends JsonHandler {
    def apply(in: Any): JsCmd =
      SetHtml("json_result", in match {
        case JsonCmd("processForm", _, params: Map[String, Any], _) =>
          <div>Processed!</div>
        case x =>
          <span class="error">Unknown issue handling JSON: {x}</span>
      })
  }
}
