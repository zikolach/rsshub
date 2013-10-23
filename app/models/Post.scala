package models

import play.api.db.DB
import anorm.SqlParser._
import anorm._
import play.api.Play.current
import java.util.Date
import play.api.Logger
import java.sql.SQLException


case class Post(id: Option[Long],
                userId: Option[Long],
                sourceId: Option[Long],
                title: String,
                link: String,
                description: String,
                pubDate: Date,
                fingerprint: Option[Array[Int]],
                distance: Option[Double],
                tags: Option[List[Long]],
                comments: Option[List[Long]])

object Post {

  def arr_to_hex(arr: Array[Int]): String = arr.map(v => "%04X".format(v)).mkString

  def hex_to_arr(hex: String): Array[Int] = hex.sliding(4, 4).map(v => Integer.parseInt(v, 16)).toArray

  val post = long("id") ~ long("user_id") ~ SqlParser.get[Option[Long]]("source_id") ~
    str("title") ~ str("link") ~ str("description") ~ date("pub_date") ~ str("fingerprint") map {
    case id~userId~sourceId~title~link~description~pubDate~fingerprint => Post(Some(id), Some(userId), sourceId, title, link, description, pubDate, Some(hex_to_arr(fingerprint)), None, None, None)
  }

  def all(): List[Post] = DB.withConnection {
    implicit c => SQL("select * from posts").as(post *)
  }.map(
    p => Post(
      p.id, p.userId, p.sourceId, p.title, p.link, p.description, p.pubDate, p.fingerprint, p.distance,
      Some(Tag.find(p.id.get).map(_.id)),
      Some(Comment.find(p.id.get).map(_.id.get))
    ))

  def get(id: Long): Option[Post] = DB.withConnection {
    implicit c => SQL("select * from posts where id = {id}").on(
      'id -> id
    ).as(post singleOpt)
  } match {
    case Some(p) =>
      Some(Post(
        p.id, p.userId, p.sourceId, p.title, p.link, p.description, p.pubDate, p.fingerprint, p.distance,
        Some(Tag.find(p.id.get).map(_.id)),
        Some(Comment.find(p.id.get).map(_.id.get))
      ))
    case None => None
  }


  def get(ids: List[Long]): List[Post] = DB.withConnection {
    implicit c => SQL("select * from posts where id in (%s)" format ids.mkString(",")).as(post *)
  }.map(p =>
    Post(
      p.id, p.userId, p.sourceId, p.title, p.link, p.description, p.pubDate, p.fingerprint, p.distance,
      Some(Tag.find(p.id.get).map(_.id)),
      Some(Comment.find(p.id.get).map(_.id.get))
    ))

  def findByUserId(userId: Long): List[Post] = DB.withConnection {
    implicit c => SQL("select * from posts where user_id = {user_id}").on('user_id -> userId).as(post *)
  }.map(
    p =>
      Post(
        p.id, p.userId, p.sourceId, p.title, p.link, p.description, p.pubDate, p.fingerprint, p.distance,
        Some(Tag.find(p.id.get).map(_.id)),
        Some(Comment.find(p.id.get).map(_.id.get))
      )
  )

  def create(userId: Long, sourceId: Option[Long], title: String, link: String, description: String, pubDate: Date,
             fingerprint: Array[Int]): Long = DB.withConnection {
    require(pubDate != null)
    require(userId != null)
    require(sourceId != null)
    implicit c => try {
      SQL("""
            | insert into posts(user_id, source_id, title, link, description, pub_date, fingerprint)
            | values ({user_id}, {source_id}, {title}, {link}, {description}, {pub_date}, {fingerprint})
          """.stripMargin).on(
        'user_id      -> userId,
        'source_id    -> sourceId,
        'title        -> title,
        'link         -> link,
        'description  -> description,
        'pub_date     -> pubDate,
        'fingerprint  -> arr_to_hex(fingerprint)
      ).executeInsert() match {
        case Some(id: Long) => id
        case None => 0
      }
    } catch {
      case e: SQLException => {
        Logger.error(e.getMessage)
        0
      }
    }
  }

  def update(id: Long, sourceId: Option[Long],
             title: String, link: String, description: String, pubDate: Date,
             fingerprint: Array[Int]) = DB.withConnection {
    implicit c => SQL(
      """
        | update posts
        | set source_id = {source_id},
        |     title = {title},
        |     link = {link},
        |     description = {description},
        |     pub_date = {pub_date},
        |     fingerprint = {fingerprint}
        | where id = {id}
      """.stripMargin).on(
      'id -> id,
      'source_id -> sourceId,
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

  def findPost(link: String): Option[Post] = DB.withConnection {
    implicit c => SQL("select * from posts where link = {link}").on(
      'link -> link
    ).as(post singleOpt)
  }

  def findBySourceId(sourceId: Long): List[Post] = DB.withConnection {
    implicit c => SQL("select * from posts where source_id = {source_id}").on('source_id -> sourceId).as(post *)
  }.map(
    p =>
      Post(
        p.id, p.userId, p.sourceId, p.title, p.link, p.description, p.pubDate, p.fingerprint, p.distance,
        None,
        Some(Comment.find(p.id.get).map(_.id.get))
      )
  )
}
