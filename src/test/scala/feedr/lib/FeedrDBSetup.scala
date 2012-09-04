package feedr.lib

import java.sql.DriverManager

import net.liftweb.squerylrecord.SquerylRecord
import net.liftweb.squerylrecord.RecordTypeMode._

import org.scalatest.{Suite, BeforeAndAfterAll}
import org.squeryl.Session
import org.squeryl.adapters.H2Adapter

import feedr.model.FeedrSchema

trait FeedrDBSetup extends BeforeAndAfterAll {
   this: BeforeAndAfterAll with Suite =>

   override def beforeAll() {
      // Set up an in-memory DB connection
      SquerylRecord.initWithSquerylSession {
         val session = Session.create(DriverManager.getConnection("jdbc:h2:mem:FeedrTest;DB_CLOSE_DELAY=-1", "sa", ""), new H2Adapter)
         //session.setLogger(statement => println(statement))
         session
      }

      // Drop and create the Feedr DB schema
      inTransaction {
         try {
            FeedrSchema.printDdl
            FeedrSchema.drop
            FeedrSchema.create
         } catch {
            case e: Throwable => e.printStackTrace()
            throw e
         }
      }
   }
}
