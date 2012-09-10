package feedr.lib

import java.sql.DriverManager

import net.liftweb.squerylrecord.SquerylRecord
import net.liftweb.squerylrecord.RecordTypeMode._

import org.scalatest.{Suite, BeforeAndAfterAll}
import org.squeryl.Session
import org.squeryl.adapters.H2Adapter

import feedr.model.FeedrSchema
import net.liftweb.common.Logger
import feedr.lib.FeedsManager.{NewFeed, ClearFeedManagers}

trait FeedrDBSetup extends BeforeAndAfterAll with Logger {
   this: BeforeAndAfterAll with Suite =>

   override def beforeAll() {
      // Set up an in-memory DB connection with a DB name unique to this test suite.
      SquerylRecord.initWithSquerylSession {
         val session = Session.create(DriverManager.getConnection("jdbc:h2:mem:Feedr;DB_CLOSE_DELAY=-1"), new H2Adapter)
         //session.setLogger(statement => println(statement))
         session
      }

      // Create the Feedr DB schema.
      inTransaction {
         try {
            FeedrSchema.create
         } catch {
            case e: Throwable => e.printStackTrace()
            throw e
         }
      }
   }

   override def afterAll() {
      // Clear the state of the FeedsManager so it matches the DB
      FeedsManager !! ClearFeedManagers()

      // Drop the Feedr DB schema.
      inTransaction {
         try {
            FeedrSchema.drop
         } catch {
            case e: Throwable => e.printStackTrace()
            throw e
         }
      }
   }
}
