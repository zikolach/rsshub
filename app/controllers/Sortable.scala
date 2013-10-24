package controllers

trait Sortable {
  case class SortInfo(sortBy: Seq[String])
  def extractSortInfo(queryString: Map[String, Seq[String]]): SortInfo = {
    val attributes: Seq[String] = queryString.get("sort[]").getOrElse(Seq("1")).filterNot(_.matches("[^\\w]"))
    println("Sorting by " + attributes.mkString(", "))
    SortInfo(attributes)
  }
}
