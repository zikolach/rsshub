package models

import play.api.Play.current
import play.api.db.DB
import anorm._
import SqlParser._

case class Tag(id: Long, name: String)

object Tag {

  val tag = long("id") ~ str("name") map { case id~name => Tag(id, name) }

  def find(name: String): Option[Tag] = DB.withConnection {
    implicit c => SQL("select * from tags where name = {name}").on(
      'name -> name
    ).as(tag singleOpt)
  }

  def get(id: Long): Tag = DB.withConnection {
    implicit c => SQL("select * from tags where id = {id}").on(
      'id -> id
    ).as(tag single)
  }

  def create(name: String): Long = DB.withConnection {
    implicit c => SQL("insert into tags(name) values ({name})").on(
      'name -> name
    ).executeInsert()
  } match {
    case Some(long: Long) => long
  }

  def delete(id: Long): Unit = DB.withConnection {
    implicit c => SQL("delete tags where id = {id}").on(
      'id -> id
    ).executeUpdate()
  }

  def find(postId: Long): List[Tag] = DB.withConnection {
    implicit c => SQL(
      """
        |select t.*
        |from tags t
        |inner join post_tags pt on pt.tag_id = t.id
        |where pt.post_id = {post_id}
      """.stripMargin).on(
      'post_id -> postId
    ).as(tag *)
  }

}
