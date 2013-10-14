package models

import java.util.Date
import play.api.db.DB
import anorm._
import SqlParser._
import play.api.Play.current

case class Comment(id: Long, postId: Long, updateDate: Date, comment: String)

object Comment {

  val comment = long("id") ~ long("post_id") ~ date("update_date") ~ str("comment") map { case id~postId~updateDate~comment => Comment(id, postId, updateDate, comment) }

  def findByPostId(postId: Long): List[Comment] = DB.withConnection {
    implicit c => SQL("select * from comments where post_id = {post_id}").on(
      'post_id -> postId
    ).as(comment *)
  }

  def create(postId: Long, comment: String): Option[Long] = DB.withConnection {
    implicit  c => SQL("insert into comments(post_id, update_date, comment) values ({post_id}, {update_date}, {comment})").on(
      'post_id -> postId,
      'update_date -> new Date(),
      'comment -> comment
    ).executeInsert()
  }

  def delete(id: Long) = ???

}