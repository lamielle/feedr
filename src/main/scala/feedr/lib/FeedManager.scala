package feedr.lib

import net.liftweb._
import actor._
import http._

object FeedManager extends LiftActor with ListenerManager {
  private var msgs = Vector("Welcome")

  /**
   * Notify listeners with the list of messages
   */
  def createUpdate = msgs

  /**
   * Process messages sent to this actor.
   */
  override def lowPriority = {
    case s: String => msgs :+= s; updateListeners()
  }
}
