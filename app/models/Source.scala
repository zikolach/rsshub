package models

import anorm.SqlParser._
import play.api.db.DB
import anorm.~
import anorm.SQL
import anorm.SqlParser
import scala.Some
import play.api.Play.current
import util.{CeptAPI, FeedReader}
import java.util.Date
import java.text.SimpleDateFormat
import org.jsoup.Jsoup
import play.api.Logger
import scalaz.{Failure, Success}

case class Source(id: Option[Long], userId: Option[Long], name: String, url: String, fetchDate: Option[Date]) {
  def fetch(): Unit = {
    FeedReader.readEntries(url).foreach((entry) => {
      val title = entry._1
      val link = entry._2
      val description = entry._3
      val pubDate = entry._4 match {
        case d: Date => d
        case _ => new Date()
      }
      Post.findPost(link) match {
        case Some(p: Post) => {}
        case None => {
          println(title)
          val text = "%s %s".format(Source.normalizeString(title), Source.normalizeString(description))
          CeptAPI.bitmap(text) match {
            case Some(fp) => {
              Post.create(userId.get, id, title, link, description, Option(pubDate), fp.positions).fold(
                e => Logger.error(e),
                post => {
                  CeptAPI.findSimilar(text, 10, 0, 0, 1, "N", 0.95) match {
                    case Success(sims) => sims.foreach(sim => Post.addTag(post.id.get, sim.term))
                    case Failure(err) => println(err)
                  }
                }
              )
            }
            case None => Post.create(userId.get, id, title, link, description, Option(pubDate), Array())
          }
        }
      }
    })
    DB.withConnection {
      implicit c => {
        SQL("update sources set fetch_date = {fetch_date} where id = {id}").on(
          'fetch_date -> new Date(),
          'id -> id
        ).executeUpdate()
      }
    }
  }
  override def toString = "Source[id: %s, name: %s, url: %s, fetchDate: %s]".format(id, name, url, fetchDate match {
    case Some(dt) => new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z").format(dt)
    case None => "[empty]"
  })
}

object Source {

  val source = long("id") ~ long("user_id") ~ str("name") ~ str("url") ~ SqlParser.get[Option[Date]]("fetch_date") map { case id~userId~name~url~fd => Source(Some(id), Some(userId), name, url, fd)}

  def normalizeString(s: String): String = Jsoup.parse(s).body().text().replaceAll("[^\\p{L}\\p{Nd}\\s]", "").replaceAll("\\s+", " ")

  def all(): List[Source] = DB.withConnection {
    implicit c =>
      SQL("select * from sources").as(source *)
  }

  def findByUserId(userId: Long): List[Source] = DB.withConnection {
    implicit c =>
      SQL("select * from sources where user_id = {user_id}").on(
        'user_id -> userId
      ).as(source *)
  }

  def get(id: Long): Option[Source] = DB.withConnection {
    implicit c => SQL("select * from sources where id = {id}").on(
      'id -> id
    ).as(source singleOpt)
  }

  def create(userId: Long, name: String, url: String): Long = DB.withConnection {
    implicit c => {
      val id: Option[Long] =
        SQL("insert into sources(user_id, name, url) values ({user_id}, {name}, {url})").on(
          'user_id -> userId, 'name -> name, 'url -> url
        ).executeInsert()
      id.get
    }
  }

  def update(id: Long, userId: Long, name: String, url: String) = DB.withConnection {
    implicit c => SQL("update sources set iser_id = {user_id}, name = {name}, url = {url} where id = {id}").on(
      'id -> id, 'user_id -> userId, 'name -> name, 'url -> url
    ).executeUpdate()
  }

  def delete(id: Long) = DB.withConnection {
    implicit  c => SQL("delete from sources where id = {id}").on(
      'id -> id
    ).executeUpdate()
  }

  def next: Option[Source] = DB.withConnection {
    implicit c => {
      SQL("select * from sources order by fetch_date nulls first limit 1").as(source singleOpt)
    }
  }

  def getFeedSources(feedId: Long): List[Source] = DB.withConnection {
    implicit c => SQL(
      """
        | select s.*
        | from sources s
        | inner join feed_sources fs on fs.source_id = s.id
        | where fs.feed_id = {feed_id}
      """.stripMargin).on('feed_id -> feedId).as(source *)
  }

}
