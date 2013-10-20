package util

import java.net.{HttpURLConnection, URL}
import com.sun.syndication.io.{SyndFeedOutput, XmlReader, SyndFeedInput}
import com.sun.syndication.feed.synd._
import java.util.Date
import scala.collection.JavaConversions._
import models.{Comment, Source, Post}
import java.util
import scala.Some

object FeedReader {

  def readEntries(feedUrl: String): List[(String, String, String, Date)] = {
    val url = new URL(feedUrl)
    val conn: HttpURLConnection = url.openConnection match { case conn: HttpURLConnection => conn }
    val input = new SyndFeedInput()
    val feed = input.build(new XmlReader(conn))
    feed.getEntries.toList.map {
      case e: SyndEntry => (e.getTitle, e.getLink, e.getDescription.getValue, e.getPublishedDate)
    }
  }

  def makeCommentsFeed(sourceId: Long, addr: String) = {
    Source.get(sourceId) match {
      case Some(source) => {

        var feed: SyndFeed = new SyndFeedImpl()
        feed.setFeedType("rss_2.0")
        feed.setTitle("Comments feed for %s source".format(source.name))
        feed.setLink(addr)
        feed.setDescription("")



        val posts: List[Post] = Post.findBySourceId(sourceId)

//        println(posts.length)

        val entries = new util.ArrayList[SyndEntry]()
        posts.map(
          post => {
            post.comments match {
              case Some(commentIds) => {
                val comments = Comment.get(commentIds)
                comments.foreach(
                  comment => {
                    val entry: SyndEntry = new SyndEntryImpl()
                    entry.setTitle(comment.comment)
                    entry.setPublishedDate(comment.updateDate.get)
                    val description = new SyndContentImpl()
                    description.setType("text/html")
                    description.setValue(post.description)
                    entry.setLink(addr + "/#/posts/" + post.id.get + "/comments/" + comment.id.get)
                    entry.setDescription(description)
                    entry.setLinks(List(post.link))
                    entries.add(entry)
                  }
                )
              }
              case _ => {}
            }
          }
        )
        feed.setEntries(entries.sortWith(
          (a, b) => a.getPublishedDate.after(b.getPublishedDate)
        ))
        val output: SyndFeedOutput = new SyndFeedOutput()
        output.outputString(feed)
    }
      case _ => ""
    }

  }

}
