package controllers

import models.{Comment, Post, Tag}
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{Action, Controller}
import java.util.Date
import scala.collection.mutable


object Comments extends Controller with Auth {

  case class CommentsWrapper(comments: Option[List[Comment]])
  case class CommentWrapper(comment: Option[Comment])

  implicit val commentFormat = Json.format[Comment]
  implicit val commentsWrapperFormat = Json.format[CommentsWrapper]
  implicit val commentWrapperFormat = Json.format[CommentWrapper]

  def index = Action {
    implicit request => {
      request.queryString.get("ids[]") match {
        case Some(ids) => {
          val comments = Comment.get(ids.toList.map(_.toString.toLong))
          Ok(Json.toJson(CommentsWrapper(Some(comments))))
        }
        case _ =>
          Ok(Json.toJson(CommentsWrapper(None)))
      }
    }
  }


  def create = Action(parse.json) {
    implicit request => {
      getAuthor(request.headers) match {
        case Some(user) => {
          request.body.validate[CommentWrapper].map {
            case CommentWrapper(Some(Comment(_, post, _, comment))) => {
              val updateDate = new Date()
              Comment.create(user.id.get, post, comment, updateDate) match {
                case Some(id) => {
                  Ok(Json.toJson(CommentWrapper(Some(Comment(Some(id), post, Some(updateDate), comment)))))
                }
                case None =>
                  BadRequest(Json.toJson(CommentWrapper(None)))
              }
            }
          }.recoverTotal(
            e => BadRequest(JsError.toFlatJson((e)))
          )
        }
        case None => Unauthorized("")
      }
    }
  }

}
