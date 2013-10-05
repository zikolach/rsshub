package models

import play.api.db.DB
import anorm.SqlParser._
import anorm._
import play.api.Play.current


case class Post(id: Option[Long], name: String, text: String)

object Post {

  val post = long("id") ~ str("name") ~ str("text") map { case id~name~text => Post(Some(id), name, text)}

  def all(): List[Post] = DB.withConnection {
    implicit c => SQL("select * from posts").as(post *)
  }

  def get(id: Long): Post = DB.withConnection {
    implicit c => SQL("select * from posts where id = {id}").on(
      'id -> id
    ).as(post single)
  }

  def create(name: String, text: String): Long = DB.withConnection {
    implicit c => SQL("insert into posts(name, text) values ({name}, {text})").on(
      'name -> name,
      'text -> text
    ).executeInsert()
  } match {
    case Some(long: Long) => long
  }

  def update(id: Long, name: String, text: String) = DB.withConnection {
    implicit c => SQL("update posts set name = {name}, text = {text} where id = {id}").on(
      'id -> id,
      'name -> name,
      'text -> text
    ).executeUpdate()
  }

  def delete(id: Long) = DB.withConnection {
    implicit  c => SQL("delete posts where id = {id}").on(
      'id -> id
    ).executeUpdate()
  }

}
