package controllers

import play.api.mvc.{Action, Controller}
import models.Post
import play.api.libs.json.{JsError, Json}

object Posts extends Controller {

  case class PostsWrapper(posts: List[Post])
  case class PostWrapper(post: Post)
  implicit val postFormat = Json.format[Post]
  implicit val postsWrapperFormat = Json.format[PostsWrapper]
  implicit val postWrapperFormat = Json.format[PostWrapper]

  def index = Action {
    Ok(Json.toJson(new PostsWrapper(Post.all())))
  }

  def get(id: Long) = Action {
    Ok(Json.toJson(new PostWrapper(Post.get(id))))
  }

  def create = Action(parse.json) {
    implicit request =>
      request.body.validate[PostWrapper].map {
        case PostWrapper(Post(id, name, text)) => {
          val id: Long = Post.create(name, text)
          Ok(Json.toJson(new PostWrapper(new Post(Some(id), name, text))))
        }
      }.recoverTotal{
        e => BadRequest("Error: " + JsError.toFlatJson(e))
      }
  }

  def update(id: Long) = Action(parse.json) {
    implicit request =>
      request.body.validate[PostWrapper].map {
        case PostWrapper(Post(None, name, text)) => {
          Post.update(id, name, text)
          Ok(Json.toJson(new PostWrapper(new Post(Some(id), name, text))))
        }
      }.recoverTotal{
        e => BadRequest("Error: " + JsError.toFlatJson(e))
      }
  }

  def delete(id: Long) = Action {
    Post.delete(id)
    Ok("")
  }
}
