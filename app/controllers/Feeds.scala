package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json.{JsError, Json}
import models.Feed
import java.util.Date
import scalaz.{Failure, Success}
import util.FeedReader

object Feeds extends Controller with Auth {

  case class FeedsWrapper(feeds: List[Feed])
  case class FeedWrapper(feed: Option[Feed])

  implicit val feedFormat = Json.format[Feed]
  implicit val feedsWrapperFormat = Json.format[FeedsWrapper]
  implicit val feedWrapperFormat = Json.format[FeedWrapper]

  private def wrapFeeds(feeds: List[Feed]) =
    FeedsWrapper(feeds)

  def index = Action {
    implicit request =>
      getAuthor(request.headers) match {
        case Some(author) => {
          val feeds = Feed.getByUserId(author.id.get)
          Ok(Json.toJson(wrapFeeds(feeds)))
        }
        case None => Unauthorized("")
      }
  }

  def create = Action(parse.json) {
    implicit request =>
      getAuthor(request.headers) match {
        case Some(author) => {
          request.body.validate[FeedWrapper].map {
            case FeedWrapper(Some(f)) => {
              val sources = f.sources match {
                case Some(list) => list.map(_.toString.toLong)
                case None => Nil
              }
              Feed.create(author.id.get, f.name, f.description, new Date(), sources) match {
                case Success(feed) => Ok(Json.toJson(FeedWrapper(Some(feed))))
                case Failure(err) => BadRequest(err)
              }
            }
            case _ => BadRequest("Invalid format")
          }.recoverTotal(
            e => BadRequest(JsError.toFlatJson(e))
          )
        }
        case None => Unauthorized("")
      }
  }

  def update(id: Long) = Action(parse.json) {
    implicit request =>
      getAuthor(request.headers) match {
        case Some(author) => {
          request.body.validate[FeedWrapper].map {
            case FeedWrapper(Some(f)) => {
              val sources = f.sources match {
                case Some(list) => list.map(_.toString.toLong)
                case None => Nil
              }
              Feed.update(id, author.id.get, f.name, f.description, new Date(), sources) match {
                case Success(feed) => Ok(Json.toJson(FeedWrapper(Some(feed))))
                case Failure(err) => BadRequest(err)
              }
            }
            case _ => BadRequest("Invalid format")
          }.recoverTotal(
            e => BadRequest(JsError.toFlatJson(e))
          )
        }
        case None => Unauthorized("")
      }
  }

  def delete(id: Long) = Action {
    implicit request =>
      getAuthor(request.headers) match {
        case Some(author) => {
          Feed.get(id) match {
            case Some(feed) =>
              feed.userId match {
                case Some(userId) if userId == author.id.get => {
                  Feed.delete(id)
                  Ok(Json.toJson(FeedWrapper(None)))
                }
                case _ => Unauthorized("")
              }
            case None => BadRequest("No feed found")
          }
        }
        case None => Unauthorized("")
      }
  }

  def getAggFeed(id: Long) = Action {
    implicit request => {
      val address = "http://" + request.host;
      Ok(FeedReader.makeAggFeed(id, address)).as(XML)
    }
  }
}
