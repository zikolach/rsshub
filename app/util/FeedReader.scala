package util

import java.net.URL
import models.Post
import scala.xml.{Node, XML}

object FeedReader {

  def readEntries(url: String): List[(String, String, String, String)] = {
    val feed = XML.load((new URL(url)).openConnection.getInputStream)
    println(feed.toString())
    val entries: List[Node] = (feed \ "channel" \ "item").toList
    entries.map((entry: Node) => (
      entry \ "title" text,
      entry \ "link" text,
      entry \ "description" text,
      entry \ "pubDate" text
    ))
  }

}
