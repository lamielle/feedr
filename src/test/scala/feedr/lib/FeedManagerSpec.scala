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
    FeedManager.shortestUniqueString("a", Set()) should equal (Some("a"))
    FeedManager.shortestUniqueString("abcd", Set()) should equal (Some("a"))
  }

  "shortestUniqueString" should "work properly" in {
    FeedManager.shortestUniqueString("a", Set("a")) should equal (None)
    FeedManager.shortestUniqueString("ab", Set("a", "b", "c")) should equal (Some("ab"))
    FeedManager.shortestUniqueString("a", Set("c", "b", "c")) should equal (Some("a"))
    FeedManager.shortestUniqueString("abcdef", Set("d", "b", "c")) should equal (Some("a"))
    FeedManager.shortestUniqueString("abcdef", Set("abcd", "basdf", "csdf")) should equal (Some("abcde"))
    FeedManager.shortestUniqueString("abc", Set("abc", "bd", "c")) should equal (None)
    //FeedManager.shortestUniqueString("7f2a4745-5d02-4ec3-98cc-c8b964c97c3f", Set("e", "34", "4", "9", "4f", "d6", "2", "7")) should equal (Some("7f"))
    //FeedManager.shortestUniqueString("7f2a4745-5d02-4ec3-98cc-c8b964c97c3f", Set("e", "34", "4", "9", "4f", "d6", "c1", "6", "1", "39", "b", "0", "2", "0a", "c", "30", "7", "3", "d")) should equal (Some("7f"))
  }
}
