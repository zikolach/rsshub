package models

import play.api.db.DB
import anorm.SqlParser._
import anorm.{SQL, ~}
import play.api.Play.current
import java.util.Date
import scalaz.{Success, Failure, Validation}

case class Feed(id: Option[Long], userId: Option[Long], name: String, description: String, updatedAt: Option[Date],
                sources: Option[List[String]])

object Feed {

  val feed = long("id") ~ long("user_id") ~ str("name") ~ str("description") ~ date("updated_at") map {
    case id~userId~name~description~updatedAt => Feed(Some(id), Some(userId), name, description, Some(updatedAt), None)
  }

  def all = DB.withConnection {
    implicit c => SQL("select * from feeds").as(feed *)
  }

  def get(id: Long): Option[Feed] = DB.withConnection {
    implicit c => SQL("select * from feeds where id = {id}").on('id -> id).as(feed singleOpt)
  }

  def getByUserId(userId: Long): List[Feed] = DB.withConnection {
    implicit c => SQL("select * from feeds where user_id = {user_id}").on('user_id -> userId).as(feed *).map(
      feed => feed.copy(sources = Some(Source.getFeedSources(feed.id.get).map(_.id.get.toString)))
    )
  }

  /**
   * Create new aggregated feed
   * @param name feed name
   * @param description feed description
   * @param updatedAt last update date
   * @return feed or error
   */
  def create(userId: Long, name: String, description: String, updatedAt: Date, sourceIds: List[Long]): Validation[String, Feed] = DB.withConnection {
    implicit c => SQL(
      """
        | insert into feeds(user_id, name, description, updated_at)
        | values ({user_id}, {name}, {description}, {updated_at})
      """.stripMargin).on(
      'user_id -> userId,
      'name -> name,
      'description -> description,
      'updated_at -> updatedAt
    ).executeInsert() match {
      case Some(id: Long) => {
        updateFeedSources(sourceIds, id)
        Success(Feed(Some(id), Some(userId), name, description, Some(updatedAt), None))
      }
      case _ => Failure("Feed cannot be created")
    }
  }

  def update(id: Long, userId: Long, name: String, description: String, updatedAt: Date, sourceIds: List[Long]): Validation[String, Feed] = DB.withConnection {
    implicit c => {
      SQL(
        """
          | update feeds
          | set name = {name},
          |     description = {description},
          |     updated_at = {updated_at}
          | where id = {id}
        """.stripMargin).on(
        'id -> id,
        'name -> name,
        'description -> description,
        'updated_at -> updatedAt
      ).executeUpdate()
      val newSourceIds = updateFeedSources(sourceIds, id).map(_.toString)
      Success(Feed(Some(id), Some(userId), name, description, Some(updatedAt), Some(newSourceIds)))
    }
  }


  private def updateFeedSources(sourceIds: List[Long], feedId: Long): List[Long] = DB.withConnection {
    implicit c => {
      val verifiedIds = sourceIds.filter(Source.get(_).isDefined)
      verifiedIds.foreach(
        sourceId => SQL(
          """
            | select count(*) as c
            | from feed_sources
            | where feed_id = {feed_id}
            | and source_id = {source_id}
          """.stripMargin).on(
          'feed_id -> feedId,
          'source_id -> sourceId
        ).apply().head[Long]("c") match {
          case n: Long if n == 0 => SQL("insert into feed_sources(feed_id, source_id) values({feed_id}, {source_id})").on(
            'feed_id -> feedId,
            'source_id -> sourceId
          ).executeUpdate()
          case _ => {}
        }
      )
      verifiedIds match {
        case Nil => SQL(
          """
            | delete from feed_sources
            | where feed_id = {feed_id}
          """.stripMargin).on(
          'feed_id -> feedId
        ).executeUpdate()
        case _ => SQL(
          """
            | delete from feed_sources
            | where feed_id = {feed_id}
            | and source_id not in (%s)
          """.format(verifiedIds.mkString(",")).stripMargin).on(
          'feed_id -> feedId
        ).executeUpdate()
      }
      verifiedIds
    }
  }

  def delete(id: Long) = DB.withConnection {
    implicit  c => {
      SQL("delete from feed_sources where feed_id = {id}").on(
        'id -> id
      ).executeUpdate()
      SQL("delete from feeds where id = {id}").on(
        'id -> id
      ).executeUpdate()
    }
  }

}
