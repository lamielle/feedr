package bootstrap.liftweb

import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.provider._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import _root_.net.liftweb.mapper.{DB, DefaultConnectionIdentifier, StandardDBVendor}

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor =
   new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
              Props.get("db.url") openOr
              "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
              Props.get("db.user"), Props.get("db.password"))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }

    // where to search snippet
    LiftRules.addToPackages("feedr")

    // Build SiteMap
    def sitemap() = SiteMap(
      // Menu with special Link
      Menu(Loc("Static", Link(List("static"), true, "/static/index"),
          "Static Content")))

    /*
     * Show the spinny image when an Ajax call starts
     */
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    /*
     * Make the spinny image go away when it ends
     */
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    LiftRules.early.append(makeUtf8)

    LiftRules.statelessRewrite.prepend(NamedPF("FeedEditorRewrite") {
      case RewriteRequest(ParsePath(feedId :: Nil, _, _, _), _, _) =>
        feedId match {
          // Must explicitly tell Lift to stop rewriting URLs so we don't recurse
          // infinitely.  Do this by passing true for the last parameter of
          // RewriteResponse.

          // index and favicon are not consider feeds
          case "index" => RewriteResponse("index" :: Nil, true)
          case "favicon" => RewriteResponse("favicon" :: Nil, true)

          // All other URLs are considered feed IDs and redirect to the feed editor
          case _ => RewriteResponse(
            ParsePath("feed-editor" :: Nil, "", false, false), Map("feedId" -> feedId), true // Use webapp/feed-editor.html
        )
      }
    })

    S.addAround(DB.buildLoanWrapper())
  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }
}
