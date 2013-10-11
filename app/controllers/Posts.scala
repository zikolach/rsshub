package controllers

import play.api.mvc.{Action, Controller}
import models._
import play.api.libs.json.{JsError, Json}
import util.CeptAPI

object Posts extends Controller {

  case class PostsWrapper(posts: List[Post], tags: List[Tag])
  case class PostWrapper(post: Option[Post], tags: Option[List[Tag]])
  implicit val postFormat = Json.format[Post]
  implicit val tagFormat = Json.format[Tag]
  implicit val postsWrapperFormat = Json.format[PostsWrapper]
  implicit val postWrapperFormat = Json.format[PostWrapper]

  def index = Action {
    implicit request => {
      request.queryString.get("ids[]") match {
        case Some(ids) => {
          val posts = Post.get(ids.toList.map(_.toLong))
          Ok(Json.toJson(PostsWrapper(posts, Tag.get(posts.map(_.tags.get).flatten))))
        }
        case None =>
          request.getQueryString("search") match {
            case Some(search) => {
              val searchBitmap: Array[Int] = CeptAPI.bitmap(search).get.positions
              val posts = Post.all()
                .map(post => Post(post.id, post.name, post.text, None, Some(CeptAPI.compareSimilarity(post.fingerprint.get, searchBitmap).distance), post.tags))
                .filter(_.distance.get < 0.9)
                .sortWith((a, b) => a.distance.get < b.distance.get)
              val tagIds = posts.map(p => p.tags.get).flatten
              Ok(Json.toJson(PostsWrapper(posts, Tag.get(tagIds))))
            }
            case None => Ok(Json.toJson(PostsWrapper(Nil, Nil)))
          }
      }
    }
  }

  def get(id: Long) = Action {
    val post = Post.get(id)
    Ok(Json.toJson(new PostWrapper(Some(post), Some(Tag.get(post.tags.get)))))
  }

  def create = Action(parse.json) {
    implicit request =>
      request.body.validate[PostWrapper].map {
        case PostWrapper(Some(Post(None, name, text, None, None, None)), None) => {
          val fp = CeptAPI.bitmap(name)
          val id: Long = Post.create(name, text, fp.get.positions)
          Ok(Json.toJson(PostWrapper(Some(Post(Some(id), name, text, None, None, None)), None)))
        }
      }.recoverTotal{
        e => BadRequest("Error: " + JsError.toFlatJson(e))
      }
  }

  def update(id: Long) = Action(parse.json) {
    implicit request =>
      request.body.validate[PostWrapper].map {
        case PostWrapper(Some(Post(None, name, text, None, None, None)), None) => {
          val fp = CeptAPI.bitmap(text)
          Post.update(id, name, text, fp.get.positions)
          Ok(Json.toJson(PostWrapper(Some(Post(Some(id), name, text, None, None, None)), None)))
        }
      }.recoverTotal{
        e => BadRequest("Error: " + JsError.toFlatJson(e))
      }
  }

  def delete(id: Long) = Action {
    Post.delete(id)
    Ok(Json.toJson(PostWrapper(None, None)))
  }
}
