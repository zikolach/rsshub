package controllers

import play.api.mvc.{Action, Controller}
import models.Source
import play.api.libs.json.{JsError, Json}
import util.FeedReader

object Sources extends Controller with Auth {

  case class SourcesWrapper(sources: List[Source])
  case class SourceWrapper(source: Option[Source])
  implicit val sourceFormat = Json.format[Source]
  implicit val sourcesWrapperFormat = Json.format[SourcesWrapper]
  implicit val sourceWrapperFormat = Json.format[SourceWrapper]

  def index = Action {
    implicit request => getAuthor(request.headers) match {
      case Some(author) => Ok(Json.toJson(new SourcesWrapper(Source.findByUserId(author.id.get))))
      case None => Unauthorized("Not authorized request")
    }
  }

  def get(id: Long) = Action {
    implicit request => getAuthor(request.headers) match {
      case Some(author) => {
        Source.get(id) match {
          case Some(Source(_, author.id, name, link, fd)) =>
            Ok(Json.toJson(new SourceWrapper(Some(Source(Some(id), None, name, link, fd)))))
          case Some(_) => Unauthorized("Not authorized request")
          case None => BadRequest("Source does not exists")
        }
      }
      case None => Unauthorized("Not authorized request")
    }

  }

  def create = Action(parse.json) {
    implicit request => {
      getAuthor(request.headers) match {
        case Some(author) => {
          request.body.validate[SourceWrapper].map {
            case SourceWrapper(Some(Source(id, None, name, text, None))) => {
              val id: Long = Source.create(author.id.get, name, text)
              Ok(Json.toJson(new SourceWrapper(Some(new Source(Some(id), None, name, text, None)))))
            }
          }.recoverTotal{
            e => BadRequest("Not valid request format")
          }
        }
        case None => Unauthorized("Not authorized request")
      }
    }
  }

  def update(id: Long) = Action(parse.json) {
    implicit request => {
      getAuthor(request.headers) match {
        case Some(author) => {
          request.body.validate[SourceWrapper].map {
            case SourceWrapper(Some(Source(None, None, name, text, None))) => {
              Source.get(id) match {
                case Some(Source(_, Some(userId), _, _, _)) => {
                  if (userId == author.id.get) {
                    Source.update(id, author.id.get, name, text)
                    Ok(Json.toJson(new SourceWrapper(Some(new Source(Some(id), None, name, text, None)))))
                  } else Unauthorized("Not authorized request")
                }
                case Some(source) => BadRequest("Source has no author")
                case None => BadRequest("Source does not exist")
              }
            }
          }.recoverTotal{
            e => BadRequest("Error: " + JsError.toFlatJson(e))
          }
        }
        case None => Unauthorized("Not authorized request")
      }
    }
  }

  def delete(id: Long) = Action {
    Source.delete(id)
    Ok(Json.toJson(SourceWrapper(None)))
  }


  def getCommentsFeed(id: Long) = Action {
    implicit request => {
      val addr = "http://" + request.host;
      Ok(FeedReader.makeCommentsFeed(id, addr)).as(XML)
    }
  }

}
