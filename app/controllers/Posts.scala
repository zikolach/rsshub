package controllers

import play.api.mvc.{Action, Controller}
import models._
import play.api.libs.json.{JsError, Json}
import util.CeptAPI

object Posts extends Controller with Auth {

  case class PostsWrapper(posts: Option[List[Post]], tags: Option[List[Tag]], comments: Option[List[Comment]])
  case class PostWrapper(post: Option[Post], tags: Option[List[Tag]], comments: Option[List[Comment]])
  implicit val tagFormat = Json.format[Tag]
  implicit val commentFormat = Json.format[Comment]
  implicit val postFormat = Json.format[Post]
  implicit val postsWrapperFormat = Json.format[PostsWrapper]
  implicit val postWrapperFormat = Json.format[PostWrapper]


  def index = Action {
    implicit request => {
      getAuthor(request.headers) match {
        case Some(author) => request.queryString.get("ids[]") match {
          case Some(ids) => {
            val posts = Post.get(ids.toList.map(_.toString.toLong))
            Ok(Json.toJson(PostsWrapper(
              Some(posts),
              Some(Tag.get(posts.map(_.tags.get).flatten)),
              None
            )))
          }
          case None =>
            request.getQueryString("search") match {
              case Some(search) => {
                CeptAPI.bitmap(search) match {
                  case Some(bitmap) => {
                    val searchBitmap: Array[Int] = bitmap.positions
                    val posts = Post.all.map(
                      post => post.copy(fingerprint = None, distance = Some(CeptAPI.compareSimilarity(post.fingerprint.get, searchBitmap).distance))
                    ).filter(_.distance.get < 0.9).sortWith((a, b) => a.distance.get < b.distance.get)
                    val tagIds = posts.map(p => p.tags.get).flatten
                    Ok(Json.toJson(PostsWrapper(
                      Some(posts),
                      Some(Tag.get(tagIds)),
                      None
                    )))
                  }
                  case None => Ok(Json.toJson(PostsWrapper(None, None, None)))
                }
              }
              case None => request.getQueryString("owner") match {
                case Some("me") => {
                  val posts = Post.findByUserId(author.id.get)
                  val tagIds = posts.map(p => p.tags.get).flatten

                  Ok(Json.toJson(PostsWrapper(
                    Some(posts),
                    Some(Tag.get(tagIds)),
                    None
                  )))
                }
                case _ => Ok(Json.toJson(PostsWrapper(None, None, None)))
              }
            }
        }
        case _ => Ok(Json.toJson(PostsWrapper(None, None, None)))
      }
    }
  }


  def get(id: Long) = Action {
    Post.get(id).fold(
      e => BadRequest(e),
      post => Ok(Json.toJson(new PostWrapper(Some(post),
        Some(Tag.get(post.tags.get)),
        Some(Comment.get(post.comments.get)))))
    )
  }

  def create = Action(parse.json) {
    implicit request =>
      getAuthor(request.headers) match {
        case Some(author) => {
          request.body.validate[PostWrapper].map {
            case PostWrapper(Some(Post(_, _, _, title, link, description, pubDate, _, _, _, _)), None, None) => {
              val fp = CeptAPI.bitmap(title)
              Post.create(author.id.get, None, title, link, description, Option(pubDate), fp.get.positions).fold(
                e => BadRequest(e),
                post => Ok(Json.toJson(PostWrapper(Some(post), None, None)))
              )
            }
          }.recoverTotal{
            e => BadRequest(JsError.toFlatJson(e))
          }
        }
        case None => Unauthorized(UR)
      }

  }

  def update(id: Long) = Action(parse.json) {
    implicit request => getAuthor(request.headers) match {
      case Some(author) => {
        request.body.validate[PostWrapper].map {
          case PostWrapper(Some(Post(_, _, sourceId, title, link, description, pubDate, _, _, _, _)), None, None) => {
            val fp = CeptAPI.bitmap(Source.normalizeString(title) + " " + Source.normalizeString(description))
            Post.update(id, sourceId, author.id.get, title, link, description, pubDate, fp.get.positions).fold(
              e => BadRequest(e),
              post => Ok(Json.toJson(PostWrapper(Some(post), None, None)))
            )
          }
        }.recoverTotal{
          e => BadRequest("Error: " + JsError.toFlatJson(e))
        }
      }
      case None => Unauthorized(UR)
    }
  }

  def delete(id: Long) = Action {
    Post.delete(id)
    Ok(Json.toJson(PostWrapper(None, None, None)))
  }
}
