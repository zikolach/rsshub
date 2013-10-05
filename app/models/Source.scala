package models

import anorm.SqlParser._
import scala.Some
import anorm._
import play.api.db.DB
import anorm.~
import scala.Some
import play.api.Play.current
import com.sun.syndication.feed.synd.SyndFeed
import java.net.{HttpURLConnection, URL}
import scala.xml.{Node, XML}

case class Source(id: Option[Long], name: String, url: String)

object Source {
  val source = long("id") ~ str("name") ~ str("url") map { case id~name~url => Source(Some(id), name, url)}

  def all(): List[Source] = DB.withConnection {
    implicit c => SQL("select * from sources").as(source *)
  }

  def get(id: Long): Source = DB.withConnection {
    implicit c => SQL("select * from sources where id = {id}").on(
      'id -> id
    ).as(source single)
  }

  def create(name: String, url: String): Long = DB.withConnection {
    implicit c => SQL("insert into sources(name, url) values ({name}, {url})").on(
      'name -> name,
      'url -> url
    ).executeInsert()
  } match {
    case Some(long: Long) => {
      fetch(long)
      long
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

  private def fetch(id: Long) = DB.withConnection {
    implicit c => {
      val s = SQL("select * from sources where id = {id}").on( 'id -> id ).as(source single)
      val feed = XML.load((new URL(s.url)).openConnection.getInputStream)
      val entries: List[Node] = (feed \ "entry").toList
      entries.foreach((entry: Node) => Post.create(entry \ "title" text, entry \ "content" text))
      feed
    }
  }
}
