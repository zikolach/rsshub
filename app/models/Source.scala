package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import anorm.~
import scala.Some
import play.api.Play.current
import util.{CeptAPI, FeedReader}
import java.util.Date
import java.text.SimpleDateFormat
import org.jsoup.Jsoup

case class Source(id: Option[Long], name: String, url: String, fetchDate: Option[Date]) {
  def fetch(): Unit = {
    FeedReader.readEntries(url).foreach((entry) => {
      val title = entry._1
      val link = entry._2
      val description = entry._3
      val pubDate = entry._4
      Post.findPost(link) match {
        case Some(p: Post) => {}
        case None => {
          println(title)
          val text = "%s %s".format(Source.normalizeString(title), Source.normalizeString(description))
          val fp = CeptAPI.bitmap(text)
          val id = Post.create(title, link, description, pubDate, fp.get.positions)
          val sims = CeptAPI.findSimilar(text, 10, 0, 0, 1, "N", 0.95).get
          sims.foreach(sim => Post.addTag(id, sim.term))
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

  val source = long("id") ~ str("name") ~ str("url") ~ SqlParser.get[Option[Date]]("fetch_date") map { case id~name~url~fd => Source(Some(id), name, url, fd)}

  def normalizeString(s: String): String = Jsoup.parse(s).body().text().replaceAll("[^\\w\\d\\s]", "").replaceAll("\\s+", " ")

  def all(): List[Source] = DB.withConnection {
    implicit c =>
      SQL("select * from sources").as(source *)
  }

  def get(id: Long): Source = DB.withConnection {
    implicit c => SQL("select * from sources where id = {id}").on(
      'id -> id
    ).as(source single)
  }

  def create(name: String, url: String): Long = DB.withConnection {
    implicit c => {
      val id: Option[Long] =
        SQL("insert into sources(name, url) values ({name}, {url})").on(
          'name -> name,
          'url -> url
        ).executeInsert()
      id.get
    }
  }

  def update(id: Long, name: String, url: String) = DB.withConnection {
    implicit c => SQL("update sources set name = {name}, url = {url} where id = {id}").on(
      'id -> id,
      'name -> name,
      'url -> url
    ).executeUpdate()
  }

  def delete(id: Long) = DB.withConnection {
    implicit  c => SQL("delete sources where id = {id}").on(
      'id -> id
    ).executeUpdate()
  }

  def next: Option[Source] = DB.withConnection {
    implicit c => {
      SQL("select * from sources order by fetch_date nulls first limit 1").as(source singleOpt)
    }
  }

}
