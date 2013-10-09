package models

import play.api.db.DB
import anorm.SqlParser._
import anorm._
import play.api.Play.current


case class Post(id: Option[Long],
                name: String,
                text: String,
                fingerprint: Option[Array[Int]],
                distance: Option[Double])

object Post {

//  implicit def columnToArray: Column[Array[Int]] = Column.nonNull[Array[Int]] { (value, meta) =>
//    val MetaDataItem(qualified, nullable, clazz) = meta
//    println(value.getClass)
//    value match {
//      case arr: Array[Int] => Right(arr)
//      case _ => Left(TypeDoesNotMatch("Cannot convert " + value + ":" + value.asInstanceOf[AnyRef].getClass + " to Array[Int] for column " + qualified))
//    }
//  }

//  def integers(columnName: String): RowParser[Array[Int]] = SqlParser.get[Array[Int]](columnName)(implicitly[Column[Array[Int]]])

  def arr_to_hex(arr: Array[Int]): String = arr.map(v => "%04X".format(v)).mkString

  def hex_to_arr(hex: String): Array[Int] = hex.sliding(4, 4).map(v => Integer.parseInt(v, 16)).toArray

  val post = long("id") ~ str("name") ~ str("text") ~ str("fingerprint") map { case id~name~text~fingerprint => Post(Some(id), name, text, Some(hex_to_arr(fingerprint)), None)}

  def all(): List[Post] = DB.withConnection {
    implicit c => SQL("select * from posts").as(post *)
  }

  def get(id: Long): Post = DB.withConnection {
    implicit c => SQL("select * from posts where id = {id}").on(
      'id -> id
    ).as(post single)
  }

  def create(name: String, text: String, fingerprint: Array[Int]): Long = DB.withConnection {
    implicit c => SQL("insert into posts(name, text, fingerprint) values ({name}, {text}, {fingerprint})").on(
      'name -> name,
      'text -> text,
      'fingerprint -> arr_to_hex(fingerprint)
    ).executeInsert()
  } match {
    case Some(long: Long) => long
  }

  def update(id: Long, name: String, text: String, fingerprint: Array[Int]) = DB.withConnection {
    implicit c => SQL("update posts set name = {name}, text = {text}, fingerprint = {fingerprint} where id = {id}").on(
      'id -> id,
      'name -> name,
      'text -> text,
      'fingerprint -> arr_to_hex(fingerprint)
    ).executeUpdate()
  }

  def delete(id: Long) = DB.withConnection {
    implicit  c => SQL("delete posts where id = {id}").on(
      'id -> id
    ).executeUpdate()
  }

}
