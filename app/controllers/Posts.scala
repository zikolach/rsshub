package controllers

import play.api.mvc.{Action, Controller}
import models._
import play.api.libs.json.{JsError, Json}
import util.CeptAPI

object Posts extends Controller with Auth {

  case class PostsWrapper(posts: List[Post], tags: List[Tag])
  case class PostWrapper(post: Option[Post], tags: Option[List[Tag]])
  implicit val postFormat = Json.format[Post]
  implicit val tagFormat = Json.format[Tag]
  implicit val postsWrapperFormat = Json.format[PostsWrapper]
  implicit val postWrapperFormat = Json.format[PostWrapper]

  def index = Action {
    implicit request => {
      getAuthor(request.headers) match {
        case Some(author) => request.queryString.get("ids[]") match {
          case Some(ids) => {
            val posts = Post.get(ids.toList.map(_.toLong))
            Ok(Json.toJson(PostsWrapper(posts, Tag.get(posts.map(_.tags.get).flatten))))
          }
          case None =>
            request.getQueryString("search") match {
              case Some(search) => {
                CeptAPI.bitmap(search) match {
                  case Some(bitmap) => {
                    val searchBitmap: Array[Int] = bitmap.positions
                    val posts = Post.all().map(
                      post => Post(post.id, None, post.sourceId, post.title, post.link, post.description, post.pubDate, None, Some(CeptAPI.compareSimilarity(post.fingerprint.get, searchBitmap).distance), post.tags)
                    ).filter(_.distance.get < 0.9).sortWith((a, b) => a.distance.get < b.distance.get)
                    val tagIds = posts.map(p => p.tags.get).flatten
                    Ok(Json.toJson(PostsWrapper(posts, Tag.get(tagIds))))
                  }
                  case None => Ok(Json.toJson(PostsWrapper(Nil, Nil)))
                }
              }
              case None => request.getQueryString("owner") match {
                case Some("me") => {
                  val posts = Post.findByUserId(author.id.get)
                  val tagIds = posts.map(p => p.tags.get).flatten
                  Ok(Json.toJson(PostsWrapper(posts,  Tag.get(tagIds))))
                }
                case _ => Ok(Json.toJson(PostsWrapper(Nil, Nil)))
              }
            }
        }
        case _ => Ok(Json.toJson(PostsWrapper(Nil, Nil)))
      }
    }
  }

  def get(id: Long) = Action {
    Post.get(id) match {
      case Some(post) => Ok(Json.toJson(new PostWrapper(Some(post), Some(Tag.get(post.tags.get)))))
      case None => BadRequest("Post not found")
    }

  }

  def create = Action(parse.json) {
    implicit request =>
      getAuthor(request.headers) match {
        case Some(author) => {
          request.body.validate[PostWrapper].map {
            case PostWrapper(Some(Post(None, None, None, title, link, description, pubDate, _, _, _)), None) => {
              val fp = CeptAPI.bitmap(title)
              val id: Long = Post.create(author.id.get, None, title, link, description, pubDate, fp.get.positions)
              if (id == 0) BadRequest("Cannot create post")
              else Ok(Json.toJson(PostWrapper(Some(Post(Some(id), None, None, title, link, description, pubDate, None, None, None)), None)))
            }
          }.recoverTotal{
            e => BadRequest(JsError.toFlatJson(e))
          }
        }
        case None => Unauthorized(UR)
      }

  }

  def update(id: Long) = Action(parse.json) {
    implicit request =>
      // TODO: check author
      request.body.validate[PostWrapper].map {
        case PostWrapper(Some(Post(None, None, sourceId, title, link, description, pubDate, _, _, _)), None) => {
          val fp = CeptAPI.bitmap(Source.normalizeString(title) + " " + Source.normalizeString(description))
          // TODO: maybe check whether sourceId is the same
          Post.update(id, sourceId, title, link, description, pubDate, fp.get.positions)
          Ok(Json.toJson(PostWrapper(Some(Post(Some(id), None, sourceId, title, link, description, pubDate, None, None, None)), None)))
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
