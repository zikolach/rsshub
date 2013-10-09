package controllers

import play.api.mvc.{Action, Controller}
import models.Post
import play.api.libs.json.{JsError, Json}
import util.CeptAPI

object Posts extends Controller {

  case class PostsWrapper(posts: List[Post])
  case class PostWrapper(post: Option[Post])
  implicit val postFormat = Json.format[Post]
  implicit val postsWrapperFormat = Json.format[PostsWrapper]
  implicit val postWrapperFormat = Json.format[PostWrapper]

  def index = Action {
    implicit request => {
      request.getQueryString("search") match {
        case Some(search) => {
//          val reg = "(?iu)(.*)" + search + "(.*)"
//          Ok(Json.toJson(new PostsWrapper(Post.all().filter( post => post.name.matches(reg)))))
          val searchBitmap: Array[Int] = CeptAPI.bitmap(search).get.positions
          val posts = Post.all()
            .map(post => Post(post.id, post.name, post.text, None, Some(CeptAPI.compareSimilarity(post.fingerprint.get, searchBitmap).distance)))
            .filter(p => p.distance.get < 1.0)
            .sortWith((a, b) => a.distance.get < b.distance.get)

          Ok(Json.toJson(PostsWrapper(posts)))
        }
        case None => Ok(Json.toJson(PostsWrapper(Post.all())))
      }
    }
  }

  def get(id: Long) = Action {
    Ok(Json.toJson(new PostWrapper(Some(Post.get(id)))))
  }

  def create = Action(parse.json) {
    implicit request =>
      request.body.validate[PostWrapper].map {
        case PostWrapper(Some(Post(None, name, text, None, None))) => {
          val fp = CeptAPI.bitmap(name)
          val id: Long = Post.create(name, text, fp.get.positions)
          Ok(Json.toJson(PostWrapper(Some(Post(Some(id), name, text, None, None)))))
        }
      }.recoverTotal{
        e => BadRequest("Error: " + JsError.toFlatJson(e))
      }
  }

  def update(id: Long) = Action(parse.json) {
    implicit request =>
      request.body.validate[PostWrapper].map {
        case PostWrapper(Some(Post(None, name, text, None, None))) => {
          val fp = CeptAPI.bitmap(text)
          Post.update(id, name, text, fp.get.positions)
          Ok(Json.toJson(PostWrapper(Some(Post(Some(id), name, text, None, None)))))
        }
      }.recoverTotal{
        e => BadRequest("Error: " + JsError.toFlatJson(e))
      }
  }

  def delete(id: Long) = Action {
    Post.delete(id)
    Ok(Json.toJson(PostWrapper(None)))
  }
}
