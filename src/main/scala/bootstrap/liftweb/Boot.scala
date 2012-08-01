package bootstrap.liftweb

import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.provider._
import _root_.net.liftweb.mapper.{DB, DefaultConnectionIdentifier, StandardDBVendor}

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment.
 */
class Boot {
   def boot() {
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
            val stopRewriting = true
            val relativePath = true
            val noTrailingSlash = true
            feedId match {
               // Must explicitly tell Lift to stop rewriting URLs so we don't recurse
               // infinitely.  Do this by passing true for the last parameter of
               // RewriteResponse.

               // index and favicon are not consider feeds
               case "index" => RewriteResponse("index" :: Nil, stopRewriting)
               case "favicon" => RewriteResponse("favicon" :: Nil, stopRewriting)

               // All other URLs are considered feed IDs and redirect to the feed editor
               case _ => RewriteResponse(
                  ParsePath("feed-editor" :: Nil, "", relativePath, noTrailingSlash),
                  Map("feedId" -> feedId), stopRewriting
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
