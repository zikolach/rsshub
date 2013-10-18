package controllers

import play.api.mvc._
import models.{User, Token}
import play.api.libs.json.Json
import scala.Some

trait Auth {
  case class ResponseWrapper(status: Option[String])
  val UR = "Not authorized request"
  def checkToken(headers: Headers): Boolean = {
    headers.get("token") match {
      case Some(token) => User.getByToken(token) match {
        case Some(user) => true
        case None => false
      }
      case None => false
    }
  }
  def getAuthor(headers: Headers): Option[User] = headers.get("token") match {
    case Some(token) => User.getByToken(token)
    case None => None
  }
}

object Auth extends  Controller {

  case class AuthResponse(token: Option[String], userId: Option[Long], message: Option[String])

  case class AuthRequest(name: Option[String], password: Option[String], token: Option[String])

  implicit val userFormat = Json.format[User]
  implicit val tokenFormat = Json.format[Token]
  implicit val authResponseFormat = Json.format[AuthResponse]
  implicit val authRequestFormat = Json.format[AuthRequest]

  def register = Action(parse.json) {
    request => {
      request.body.validate[AuthRequest].map({
        case AuthRequest(Some(name), Some(password), None) => {
          User.register(name, password) match {
            case Some(user) => Ok(Json.toJson(AuthResponse(
              None,
              None,
              Some("Successfully registered")
            )))
            case None => BadRequest(Json.toJson(AuthResponse(None, None, Some("Can't create user"))))
          }
        }
      }).recoverTotal(
        e => BadRequest(Json.toJson(AuthResponse(None, None, Some("Invalid request"))))
      )
    }
  }

  def login = Action(parse.json) {
    request => {
      request.body.validate[AuthRequest].map({
        case AuthRequest(Some(name), Some(password), None) => {
          User.login(name, password, request.remoteAddress) match {
            case Some(token) => Ok(Json.toJson(AuthResponse(
              Some(token.token),
              Some(User.getByToken(token.token).get.id.get),
              Some("Successfully logged in")
            )))
            case None => BadRequest(Json.toJson(AuthResponse(None, None, Some("Can't login"))))
          }
        }
        case AuthRequest(None, None, Some(token)) => {
          User.getByToken(token) match {
            case Some(user) => Ok(Json.toJson(AuthResponse(
              Some(token),
              Some(user.id.get),
              Some("Successfully logged in")
            )))
            case None => BadRequest(Json.toJson(AuthResponse(None, None, Some("Can't login"))))
          }
        }
      }).recoverTotal(
        e => BadRequest(Json.toJson(AuthResponse(None, None, Some("Invalid request"))))
      )
    }
  }

  def logout = Action {
    request => {
      request.headers.get("token") match {
        case Some(token) => {
          User.logout(token)
          Ok(Json.toJson(AuthResponse(None, None, Some("Successfully logged out"))))
        }
        case None => BadRequest(Json.toJson(AuthResponse(None, None, Some("Invalid token"))))
      }
    }
  }

}
