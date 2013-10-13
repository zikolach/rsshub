package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import anorm.~
import scala.Some
import play.api.Play.current
import util.{CeptAPI, FeedReader}
import java.util.Date

case class Source(id: Option[Long], name: String, url: String, fetchDate: Option[Date]) {
  def fetch(): Unit = {
    FeedReader.readEntries(url).foreach((entry) => {
      println(entry._1)
      val title = Source.norm_str(entry._1)
      val link = entry._2
      Post.findPost(link) match {
        case Some(p: Post) => {}
        case None => {
          val fp = CeptAPI.bitmap(title)
          val id = Post.create(title, link, entry._3, entry._4, fp.get.positions)
          val sims = CeptAPI.findSimilar(title, 10, 0, 0, 1, "N", 0.95).get
          sims.foreach(sim => Post.addTag(id, sim.term))
        }
      }
    })
    DB.withConnection {
      implicit c => {
        SQL("update sources set fetch_date = {fetch_date} where id = {id}").on(
          'fetch_date -> new Date(),
          'id -> id
        )
      }
    }
  }
}

object Source {

  val source = long("id") ~ str("name") ~ str("url") ~ SqlParser.get[Option[Date]]("fetch_date") map { case id~name~url~fd => Source(Some(id), name, url, fd)}

  def norm_str(s: String): String = {
    val res = s.replaceAll("[^\\w\\d\\s]", "")
    res.substring(0, Math.min(255, res.length))
  }

  def all(): List[Source] = DB.withConnection {
    implicit c => SQL("select * from sources").as(source *)
  }.map(s => Source(s.id, s.name, s.url, None))

  def get(id: Long): Source = DB.withConnection {
    implicit c => SQL("select * from sources where id = {id}").on(
      'id -> id
    ).as(source single)
  } match {
    case s: Source => Source(s.id, s.name, s.url, None)
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
