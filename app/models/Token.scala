package models

import play.api.db.DB
import anorm._
import anorm.SqlParser._
import play.api.Play.current
import akka.util.HashCode

case class Token(token: String, userId: Long, ip: String)

object Token {

  val token = str("token") ~ long("user_id") ~ str("ip") map {
    case token~userId~ip => Token(token, userId, ip)
  }

  def create(userId: Long, ip: String): Token = DB.withConnection {
    implicit c => {
      val token: String = java.util.UUID.randomUUID.toString
      SQL("insert into tokens(token, user_id, ip) values({token}, {user_id}, {ip})").on(
        'token -> token,
        'user_id -> userId,
        'ip -> ip
      ).executeUpdate()
      Token(token, userId, ip)
    }
  }

  def delete(token: String) = DB.withConnection {
    implicit c => SQL("delete from tokens where token = {token}").on(
      'token -> "aaa"
    ).executeUpdate()
  }

}
