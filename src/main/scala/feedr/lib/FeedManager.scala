package feedr.lib

import net.liftweb._
import actor._
import common.Logger
import feedr.model.Feed

object FeedManager extends LiftActor with Logger {
  case class NewFeed()

  private var feeds: Map[String, Feed] = Map.empty
  private var feedCounter: Long = 0

  // Converts the string to a sequence of sets containing the chars of the string
  def strToSets(str: String) = str.map {Set(_)}

  // XXX: This algorithm is busted, need to figure something else out instead
  def shortestUniqueString(inStr: String, strings: Set[String]) = {
    // Build a sequence of sets of characters: the set at position i contains all characters
    // of the input strings at position i.
    // Use fold here instead of reduce to handle the case where strings is an empty set.
    val stringsAsSets = strings.map {strToSets(_)}
    val listOfSetsOfChars = stringsAsSets.fold(Seq[Set[Char]]())((result, charList) => {
      // Zip up the current character list (i.e. Seq(Set('b'),Set('o'),Set('b')))
      // with the result so far.
      val zippedLists = result.zipAll(charList, Set[Char](), Set[Char]())

      // Union the zipped character lists
      zippedLists.map {case (set1, set2) => set1 | set2}
    }
    )

    val inStrSets = strToSets(inStr)
    val zippedResSetList = inStrSets.zipAll(listOfSetsOfChars, Set[Char](), Set[Char]())
    val resSetList = zippedResSetList.map(charSets => charSets._1 -- charSets._2)

    debug("inStr: %s".format(inStr))
    debug("resSetList: %s".format(resSetList))
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
        case _ => Some(inStr.slice(0, resLen.get + 1))
      }
    }
  }

  //private def nextUniqueFeedId = shortestUniqueString(UUID.randomUUID().toString, feeds.keySet)
  private def nextUniqueFeedId = {
    feedCounter += 1
    feedCounter.formatted("%01x")
  }

  def newFeed = {
    val feedId = nextUniqueFeedId
    feeds += feedId -> Feed(feedId)
    debug("Added new feed: %s".format(feedId))
    feedId
  }

  override def messageHandler = {
    case NewFeed() => reply(newFeed)
    case error => reply("Error: %s".format(error))
  }
}
