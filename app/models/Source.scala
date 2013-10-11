package models

import anorm.SqlParser._
import scala.Some
import anorm._
import play.api.db.DB
import anorm.~
import scala.Some
import play.api.Play.current
import java.net.{HttpURLConnection, URL}
import scala.xml.{Node, XML}
import util.{CeptAPI, FeedReader}
import java.text.{DateFormat, SimpleDateFormat}

case class Source(id: Option[Long], name: String, url: String)

object Source {
  val source = long("id") ~ str("name") ~ str("url") map { case id~name~url => Source(Some(id), name, url)}

  def norm_str(s: String): String = {
    val res = s.replaceAll("[^\\w\\d\\s]", "")
    res.substring(0, Math.min(255, res.length))
  }

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
    case None => -1
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

  private def fetch(id: Long): Unit = DB.withConnection {
    implicit c => {
      val s = SQL("select * from sources where id = {id}").on( 'id -> id ).as(source single)
      val df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z")
      FeedReader.readEntries(s.url).foreach((entry) => {
        println(entry._1)
        val title = norm_str(entry._1)
        val fp = CeptAPI.bitmap(title)
        // Fri, 11 Oct 2013 18:25:00 GMT
        // "EEE, d MMM yyyy HH:mm:ss Z"
        println(entry._4)
        val dt = df.parse(entry._4)
        val id = Post.create(title, entry._2, entry._3, dt, fp.get.positions)
        val sims = CeptAPI.findSimilar(title, 10, 0, 0, 1, "N", 0.95).get
        sims.foreach(sim => Post.addTag(id, sim.term))
      })
    }
  }
}
