package models

import java.util.Date
import play.api.db.DB
import anorm._
import SqlParser._
import play.api.Play.current

case class Comment(id: Option[Long],
                   post: String,
                   updateDate: Option[Date],
                   comment: String,
                   userId: Option[Long],
                   userName: Option[String])

object Comment {


  val comment = long("id") ~ long("post_id") ~long("user_id") ~ date("update_date") ~ str("comment") map { case id~post~userId~updateDate~comment => Comment(Some(id), post.toString, Some(updateDate), comment, Some(userId), None) }

  def find(postId: Long): List[Comment] = DB.withConnection {
    implicit c => SQL("select * from comments where post_id = {post_id}").on(
      'post_id -> postId
    ).as(comment *)
  }

  def get(ids: List[Long]): List[Comment] = DB.withConnection {
    implicit c =>
      if (ids.length > 0) SQL("select * from comments where id in (%s)" format ids.mkString(",")).as(comment *)
      else Nil
  }.map(
    c => c.copy(
      userName = User.get(c.userId.get) match {
        case Some(user) => Some(user.name)
        case None => None
      }
    )
  )

  def findByPostId(postId: Long): List[Comment] = DB.withConnection {
    implicit c => SQL("select * from comments where post_id = {post_id}").on(
      'post_id -> postId
    ).as(comment *)
  }


  def create(userId: Long, post: String, comment: String, updateDate: Date): Option[Long] = DB.withConnection {
    implicit  c => SQL("insert into comments(user_id, post_id, update_date, comment) values ({user_id}, {post_id}, {update_date}, {comment})").on(
      'user_id -> userId,
      'post_id ->  post.toLong,
      'update_date -> updateDate,
      'comment -> comment
    ).executeInsert()
  }

  def delete(id: Long) = ???

}