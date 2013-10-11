package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json.Json
import models.{Post, Tag, Source}

object Tags extends Controller {

  case class TagsWrapper(tags: List[Tag], posts: List[Post])
  case class TagWrapper(tag: Tag, posts: List[Post])

  implicit val tagFormat = Json.format[Tag]
  implicit val postFormat = Json.format[Post]
  implicit val tagsWrapperFormat = Json.format[TagsWrapper]
  implicit val tagWrapperFormat = Json.format[TagWrapper]

  def index = Action {
    request => {
      request.queryString.get("ids[]") match {
        case Some(ids) => {
          val tags = Tag.get(ids.toList.map(_.toLong))
          val postIds = tags.map(_.posts.get).flatten
          Ok(Json.toJson(new TagsWrapper(tags, Post.get(postIds))))
        }
        case None => {
          request.getQueryString("search") match {
            case Some(search) =>
              val reg = "(?iu)(.*)" + search + "(.*)"
              val tags = Tag.all().filter(_.name.matches(reg))
              Ok(Json.toJson(TagsWrapper(tags, Post.get(tags.map(_.posts.get).flatten))))
            case None => {
              val tags = Tag.all()
              val postIds = tags.map(_.posts.get).flatten
              Ok(Json.toJson(new TagsWrapper(tags, Post.get(postIds))))
            }
          }
        }
      }
    }
  }

  def get(id: Long) = Action {
    val tag = Tag.get(id)
    Ok(Json.toJson(new TagWrapper(tag, Post.get(tag.posts.get))))
  }

}
