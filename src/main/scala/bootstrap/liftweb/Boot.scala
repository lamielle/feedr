package bootstrap.liftweb

import java.sql.DriverManager

import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.provider._
import net.liftweb.squerylrecord.SquerylRecord

import org.squeryl.Session
import org.squeryl.adapters.PostgreSqlAdapter

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment.
 */
class Boot {
   def boot() {
      // Load the DB config properties
      val dbDriver = Props.get("db.driver") openOr "No DB driver available!"
      val dbUrl = Props.get("db.url") openOr "No DB url available!"
      val dbUser = Props.get("db.user") openOr "No DB user available!"
      val dbPassword = Props.get("db.password") openOr "No DB password available!"

      // Set up the Squeryl session factory so a new Postgres connection is made for every connection.
      // Use a connection pool like c3p0 if this becomes a performance issue.
      // TODO: Look at using Lift's session management again.  Couldn't get it working, but it isn't the
      // recommended method in the docs, so maybe it's supported?
      Class.forName(dbDriver)
      SquerylRecord.initWithSquerylSession(Session.create(
            DriverManager.getConnection(dbUrl, dbUser, dbPassword),
            new PostgreSqlAdapter))

      // Where to search for snippets.
      LiftRules.addToPackages("feedr")

      // Show the spinny image when an Ajax call starts
      LiftRules.ajaxStart =
         Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

      // Make the spinny image go away when it ends
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
   }

   // Force the request to be UTF-8
   private def makeUtf8(req: HTTPRequest) {
      req.setCharacterEncoding("UTF-8")
   }
}
