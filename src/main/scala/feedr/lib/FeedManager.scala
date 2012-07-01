package feedr.lib

import net.liftweb._
import actor._
import feedr.model.Feed
import collection.immutable.HashMap
import java.util.UUID

object FeedManager extends LiftActor {
  case class NewFeed()

  private val feeds: HashMap[String, Feed] = HashMap.empty

  def shortestUniqueString(inStr: String, strings: Set[String]) = {
    // Build a sequence of sets of characters: the set at position i contains all characters
    // of the input strings at position i.
    val listOfSetsOfChars = strings.map(_.map(Set(_))).fold(Seq[Set[Char]]())((result, charList) =>
      result.zipAll(charList, Set[Char](), Set[Char]()).map(charSets => charSets._1 | charSets._2)
    )

    val inStrSets = inStr.map(Set(_))
    val zippedResSetList = inStrSets.zipAll(listOfSetsOfChars, Set[Char](), Set[Char]())
    val resSetList = zippedResSetList.map(charSets => charSets._1 -- charSets._2)

    // Determine the position of the first non-empty set (None if all sets are empty)
    val resLen = resSetList.zipWithIndex.foldLeft(None: Option[Int])((pos, itemIndexTuple) => pos match {
      case None => itemIndexTuple._1.toSeq match {
        case Seq() => None
        case other => Some(itemIndexTuple._2)
      }
      case other => pos
    })

    inStr match {
      case "" => None
      case _ => resLen match {
        case None => None
        case _ => inStr.slice(0, resLen.get + 1)
      }
    }
  }

  private def nextUniqueFeedId = shortestUniqueString(UUID.randomUUID().toString, feeds.keySet)

  private def newFeed = {
    //val feed = Feed()
    //feeds[feed.id] = feed
    //feed
    ""
  }

  override def messageHandler = {
    case NewFeed => reply(newFeed)
    case _ => "Error"
  }
}
