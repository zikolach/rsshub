package models

import anorm.SqlParser._
import scala.Some
import anorm._
import play.api.db.DB
import anorm.~
import scala.Some
import play.api.Play.current

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
    case Some(long: Long) => long
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
}
