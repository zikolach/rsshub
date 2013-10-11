package models

import play.api.db.DB
import anorm.SqlParser._
import anorm._
import play.api.Play.current
import java.util.Date


case class Post(id: Option[Long],
                title: String,
                link: String,
                description: String,
                pubDate: Date,
                fingerprint: Option[Array[Int]],
                distance: Option[Double],
                tags: Option[List[Long]])

object Post {

  def arr_to_hex(arr: Array[Int]): String = arr.map(v => "%04X".format(v)).mkString

  def hex_to_arr(hex: String): Array[Int] = hex.sliding(4, 4).map(v => Integer.parseInt(v, 16)).toArray

  val post = long("id") ~ str("title") ~ str("link") ~ str("description") ~ date("pub_date") ~ str("fingerprint") map { case id~title~link~description~pubDate~fingerprint => Post(Some(id), title, link, description, pubDate, Some(hex_to_arr(fingerprint)), None, None)}

  def all(): List[Post] = DB.withConnection {
    implicit c => SQL("select * from posts").as(post *)
  }.map(p => Post(p.id, p.title, p.link, p.description, p.pubDate, p.fingerprint, p.distance, Some(Tag.find(p.id.get).map(_.id))))

  def get(id: Long): Post = DB.withConnection {
    implicit c => SQL("select * from posts where id = {id}").on(
      'id -> id
    ).as(post single)
  } match {
    case Post(id, title, link, description, pubDate, fingerprint, distance, tags) => Post(id, title, link, description, pubDate, fingerprint, distance, Some(Tag.find(id.get).map(_.id)))
  }

  def get(ids: List[Long]): List[Post] = DB.withConnection {
    implicit c => SQL("select * from posts where id in (%s)" format ids.mkString(",")).as(post *)
  }.map(p => Post(p.id, p.title, p.link, p.description, p.pubDate, p.fingerprint, p.distance, Some(Tag.find(p.id.get).map(_.id))))

  def create(title: String, link: String, description: String, pubDate: Date,
             fingerprint: Array[Int]): Long = DB.withConnection {
    implicit c => SQL("insert into posts(title, link, description, pub_date, fingerprint) values ({title}, {link}, {description}, {pub_date}, {fingerprint})").on(
      'title -> title,
      'link -> link,
      'description -> description,
      'pub_date -> pubDate,
      'fingerprint -> arr_to_hex(fingerprint)
    ).executeInsert()
  } match {
    case Some(long: Long) => long
    case None => -1
  }

  def update(id: Long,
             title: String, link: String, description: String, pubDate: Date,
             fingerprint: Array[Int]) = DB.withConnection {
    implicit c => SQL("update posts set name = {name}, text = {text}, fingerprint = {fingerprint} where id = {id}").on(
      'id -> id,
      'title -> title,
      'link -> link,
      'description -> description,
      'pub_date -> pubDate,
      'fingerprint -> arr_to_hex(fingerprint)
    ).executeUpdate()
  }

  def delete(id: Long) = DB.withConnection {
    implicit  c => SQL("delete posts where id = {id}").on(
      'id -> id
    ).executeUpdate()
  }

  def addTag(postId: Long, name: String): Unit = Tag.find(name) match {
    case Some(tag) => addTag(postId, tag)
    case None => addTag(postId, Tag.get(Tag.create(name)))
  }

  private def addTag(postId: Long, tag: Tag): Unit = DB.withConnection {
    implicit c => SQL("insert into post_tags(post_id, tag_id) values({post_id}, {tag_id})").on(
      'post_id -> postId,
      'tag_id -> tag.id
    ).executeInsert()
  }

  private def removeTag(postId: Long, tag: Tag): Unit = DB.withConnection {
    implicit c => SQL("delete post_tags where post_id = {post_id} and tag_id = {tag_id}").on(
      'post_id -> postId,
      'tag_id -> tag.id
    ).executeInsert()
  }

  def findPosts(tagId: Long): List[Post] = DB.withConnection {
    implicit c => SQL(
      """
        |select p.*
        |from posts p
        |inner join post_tags pt on pt.post_id = p.id
        |where pt.tag_id = {tag_id}
      """.stripMargin).on(
      'tag_id -> tagId
    ).as(post *)
  }
}
