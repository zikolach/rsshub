package models

import java.util.Date
import play.api.db.DB
import anorm._
import anorm.SqlParser._
import play.api.Play.current
import org.h2.jdbc.JdbcSQLException
import play.api.Logger

case class User(id: Option[Long], name: String, password: Option[String], lastLoginDate: Date)

object User {

  val user = long("id") ~ str("name") ~ str("password") ~ date("last_login_date") map {
    case id~name~password~lastLoginDate => User(Some(id), name, Some(password), lastLoginDate)
  }

  def register(name: String, password: String): Option[User] = DB.withConnection {
    implicit c => try {
      val lastLoginDate = new Date()
      SQL("insert into users(name, password, last_login_date) values({name}, {password}, {last_login_date})").on(
        'name -> name, 'password -> password, 'last_login_date -> lastLoginDate
      ).executeInsert() match {
        case Some(id: Long) => Some(User(Some(id), name, Some(password), lastLoginDate))
        case None => None
      }
    } catch {
      case e: JdbcSQLException => {
        Logger.warn("User.register %s".format(e.getMessage))
        None
      }
    }
  }

  def login(name: String, password: String, ip: String): Option[Token] = DB.withConnection {
    implicit c => {
      SQL("select * from users where name = {name} and password = {password}").on(
        'name -> name,
        'password -> password
      ).as(user singleOpt) match {
        case Some(user: User) => {
          SQL("update users set last_login_date = {last_login_date} where id = {id}").on(
            'id -> user.id.get,
            'last_login_date -> new Date()
          ).executeUpdate()
          Some(Token.create(user.id.get, ip))
        }
        case None => None
      }
    }
  }

  def logout(token: String) = DB.withConnection {
    implicit c => SQL("delete from tokens where token = {token}").on('token -> token).executeUpdate()
  }

  def getByToken(token: String): Option[User] = DB.withConnection {
    implicit c => SQL(
      """
        |select *
        |from users
        |where id = (
        |   select user_id
        |   from tokens
        |   where token = {token})
      """.stripMargin).on(
      'token -> token
    ).as(user singleOpt)
  }

  def get(id: Long): Option[User] = DB.withConnection {
    implicit c => SQL("select * from users where id = {id}").on('id -> id).as(user singleOpt)
  }

  def update(id: Long, name: String, password: String) = DB.withConnection {
    implicit c => {
      SQL("update users set name = {name}, password = {password} where id = {id}").on(
        'id -> id,
        'name -> name,
        'password -> password
      ).executeUpdate()
    }
  }
}
