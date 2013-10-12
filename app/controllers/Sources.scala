package controllers

import play.api.mvc.{Action, Controller}
import models.Source
import play.api.libs.json.{JsError, Json}
import controllers.Posts.PostWrapper

object Sources extends Controller {

  case class SourcesWrapper(sources: List[Source])
  case class SourceWrapper(source: Option[Source])
  implicit val sourceFormat = Json.format[Source]
  implicit val sourcesWrapperFormat = Json.format[SourcesWrapper]
  implicit val sourceWrapperFormat = Json.format[SourceWrapper]

  def index = Action {
    Ok(Json.toJson(new SourcesWrapper(Source.all())))
  }

  def get(id: Long) = Action {
    Ok(Json.toJson(new SourceWrapper(Some(Source.get(id)))))
  }

  def create = Action(parse.json) {
    implicit request =>
      request.body.validate[SourceWrapper].map {
        case SourceWrapper(Some(Source(id, name, text, None))) => {
          val id: Long = Source.create(name, text)
          Ok(Json.toJson(new SourceWrapper(Some(new Source(Some(id), name, text, None)))))
        }
      }.recoverTotal{
        e => BadRequest("Error: " + JsError.toFlatJson(e))
      }
  }

  def update(id: Long) = Action(parse.json) {
    implicit request =>
      request.body.validate[SourceWrapper].map {
        case SourceWrapper(Some(Source(None, name, text, None))) => {
          Source.update(id, name, text)
          Ok(Json.toJson(new SourceWrapper(Some(new Source(Some(id), name, text, None)))))
        }
      }.recoverTotal{
        e => BadRequest("Error: " + JsError.toFlatJson(e))
      }
  }

  def delete(id: Long) = Action {
    Source.delete(id)
    Ok(Json.toJson(SourceWrapper(None)))
  }

}
