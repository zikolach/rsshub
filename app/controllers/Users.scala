package controllers

import models.User
import play.api.mvc.{Controller, Action}
import play.api.libs.json.Json

object Users extends Controller {

  case class UsersWrapper(users: List[User])
  case class UserWrapper(user: User)

  implicit val userFormat = Json.format[User]
  implicit val usersWrapperFormat = Json.format[UsersWrapper]
  implicit val userWrapperFormat = Json.format[UserWrapper]

  def index = Action {
    implicit request => {
      request.cookies.get("token") match {
        case Some(token) => {
          User.getByToken(token.value) match {
            case Some(user) => Ok(Json.toJson(UsersWrapper(List(user))))
            case None => BadRequest("")
          }
        }
        case None => Unauthorized("")
      }
    }
  }

  def get(id: Long) = Action {
    implicit request => {
      request.headers.get("token") match {
        case Some(token) => {
          User.getByToken(token) match {
            case Some(user) => {
              if (user.id == id)
                Ok(Json.toJson(UserWrapper(User(user.id, user.name, None, user.lastLoginDate))))
              else
                BadRequest("")
            }
            case None => BadRequest("")
          }
        }
        case None => Unauthorized("")
      }
    }
  }
}
