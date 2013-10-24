package controllers

import play.api.mvc.{Action, Controller}
import models._
import play.api.libs.json.{JsError, Json}
import util.CeptAPI

object Posts extends Controller with Auth with Pageable with Sortable {

  case class PostsWrapper(posts: Option[List[Post]], tags: Option[List[Tag]], comments: Option[List[Comment]])
  case class PostWrapper(post: Option[Post], tags: Option[List[Tag]], comments: Option[List[Comment]])
  implicit val tagFormat = Json.format[Tag]
  implicit val commentFormat = Json.format[Comment]
  implicit val postFormat = Json.format[Post]
  implicit val postsWrapperFormat = Json.format[PostsWrapper]
  implicit val postWrapperFormat = Json.format[PostWrapper]

  private def wrapPosts(posts: List[Post]): PostsWrapper = posts match {
    case Nil => PostsWrapper(Some(Nil), None, None)
    case posts: List[Post] => PostsWrapper(
      Some(posts.map(_.copy(fingerprint = None))),
      Some(Tag.get(posts.map(p => p.tags.get).flatten)),
      Some(Comment.get(posts.map(p => p.comments.get).flatten))
    )
  }

  private def wrapPost(postOpt: Option[Post]): PostWrapper = postOpt match {
    case None => PostWrapper(None, None, None)
    case Some(post) => PostWrapper(
      Some(post.copy(fingerprint = None)),
      Some(Tag.get(post.tags.get)),
      Some(Comment.get(post.comments.get))
    )
  }



  def index = Action {
    implicit request => {
      val pageInfo = extractPageInfo(request.queryString)
      val sortInfo = extractSortInfo(request.queryString)
      val posts = getAuthor(request.headers) match {
        case Some(author) => request.queryString.get("ids[]") match {
          case Some(ids) => Post.get(ids.toList.map(_.toString.toLong))
          case None =>
            request.getQueryString("search") match {
              case Some(search) => {
                CeptAPI.bitmap(search) match {
                  case Some(bitmap) => {
                    val searchBitmap: Array[Int] = bitmap.positions
                    Post.all.map(
                      post => post.copy(fingerprint = None, distance = Some(CeptAPI.compareSimilarity(post.fingerprint.get, searchBitmap).distance))
                    ).filter(_.distance.get < 0.9).sortWith((a, b) => a.distance.get < b.distance.get)
                  }
                  case None => Nil
                }
              }
              case None => request.getQueryString("owner") match {
                case Some("me") => Post.findByUserIdOrderedWithLimit(author.id.get, pageInfo.start, pageInfo.count, sortInfo.sortBy)
                case _ => Nil
              }
            }
        }
        case _ => Nil
      }
      Ok(Json.toJson(wrapPosts(posts)))
    }
  }


  def get(id: Long) = Action {
    Post.get(id).fold(
      e => BadRequest(e),
      post => Ok(Json.toJson(wrapPost(Some(post)))))
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
                post => Ok(Json.toJson(wrapPost(Some(post))))
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
              post => Ok(Json.toJson(wrapPost(Some(post))))
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
    Ok(Json.toJson(wrapPost(None)))
  }
}
