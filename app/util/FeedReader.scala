package util

import java.net.{HttpURLConnection, URL}
import scala.xml.{Node, XML}
import com.sun.syndication.io.{XmlReader, SyndFeedInput}
import com.sun.syndication.feed.synd.SyndEntry
import java.util.Date
import java.util.ArrayList
import scala.collection.JavaConversions._

object FeedReader {

  def readEntries(feedUrl: String): List[(String, String, String, Date)] = {
    val url = new URL(feedUrl)
    val conn: HttpURLConnection = url.openConnection match { case conn: HttpURLConnection => conn }
    val input = new SyndFeedInput()
    val feed = input.build(new XmlReader(conn))
    feed.getEntries match {
      case entries: ArrayList[SyndEntry] => entries.toList.map(e => (e.getTitle, e.getLink, e.getDescription.getValue, e.getPublishedDate))
    }
  }

}
