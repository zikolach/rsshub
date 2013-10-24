package controllers

trait Pageable {
  case class PageInfo(start: Int, count: Int)
  def extractPageInfo(queryString: Map[String, Seq[String]]): PageInfo = {
    val start: Int = queryString.get("start").getOrElse(Seq()).headOption.getOrElse("0").toInt
    val count: Int = queryString.get("count").getOrElse(Seq()).headOption.getOrElse("10").toInt
    println(start.toString + ".." + count.toString)
    PageInfo(start, count)
  }
}
