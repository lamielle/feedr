package feedr.lib

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class FeedManagerSpec extends FlatSpec with ShouldMatchers {
  "shortestUniqueString" should "handle both empty inputs" in {
    FeedManager.shortestUniqueString("", Set()) should equal (None)
  }

  "shortestUniqueString" should "handle an empty input string" in {
    FeedManager.shortestUniqueString("", Set("a")) should equal (None)
    FeedManager.shortestUniqueString("", Set("a", "b")) should equal (None)
  }

  "shortestUniqueString" should "handle an empty input set" in {
    FeedManager.shortestUniqueString("a", Set()) should equal ("a")
    FeedManager.shortestUniqueString("abcd", Set()) should equal ("a")
  }

  "shortestUniqueString" should "work properly" in {
    FeedManager.shortestUniqueString("a", Set("a")) should equal (None)
    FeedManager.shortestUniqueString("ab", Set("a", "b", "c")) should equal ("ab")
    FeedManager.shortestUniqueString("a", Set("c", "b", "c")) should equal ("a")
    FeedManager.shortestUniqueString("abcdef", Set("d", "b", "c")) should equal ("a")
    FeedManager.shortestUniqueString("abcdef", Set("abcd", "basdf", "csdf")) should equal ("abcde")
    FeedManager.shortestUniqueString("abc", Set("abc", "bd", "c")) should equal (None)
  }
}
