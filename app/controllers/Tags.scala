package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json.Json
import models.{Tag, Source}

object Tags extends Controller {

  case class TagsWrapper(tags: List[Tag])

  implicit val tagFormat = Json.format[Tag]
  implicit val tagsWrapperFormat = Json.format[TagsWrapper]

  def index = Action {
    Ok(Json.toJson(new TagsWrapper(Tag.all())))
  }

}
