package util
import dispatch._, Defaults._
import play.api.libs.json.Json


object CeptAPI {
  val appId = "5b782a1b"
  val appKey = "c4e2825c72f9356d8e8ab2de96e2b4d6"
  val points: Double = 128 * 128

  case class QueryStatus(fullQuery: Boolean, errorCode: Int, errorString: Option[String])
  case class Bitmap(width: Int, height: Int, positions: Array[Int])
  case class BitmapResponse(time: Int, queryStatus: QueryStatus, bitmap: Bitmap)

  case class CompareResult(overlap: Double, overallOverlap: Double, oneInTwo: Double, twoInOne: Double, distance: Double) {
    override def toString = "{overlap: "+overlap.toString+", overallOverlap: "+overallOverlap.toString+", oneInTwo: "+oneInTwo.toString+", twoInOne: "+twoInOne.toString+", distance: "+distance.toString+"}"
  }

  implicit val formatBitmap = Json.format[Bitmap]
  implicit val formatQueryStatus = Json.format[QueryStatus]
  implicit val formatBitmapResponse = Json.format[BitmapResponse]


  private def bitmapSvc(term: String) =
    url("http://api.cept.at/v1/term2bitmap").GET.addQueryParameter("term", term).addQueryParameter("app_id", appId).addQueryParameter("app_key", appKey)

  def bitmap(term: String): Option[Bitmap] = {
    val res = Http(bitmapSvc(term) OK as.String)
    val json = res()
    Json.parse(json).validate[BitmapResponse].map(
      r => Some(r.bitmap)
    ).recoverTotal(
      e => None
    )
  }

  def compareSimilarity(a: Array[Int], b: Array[Int]): CompareResult = {
    val and = a.filter(b.contains).length.toDouble
    val or = (a ++ b).distinct.length.toDouble
    new CompareResult(and / or, (points - (or - and)) / points, and / a.length, and / b.length, (or - and) / or)
  }

  def compareSimilarity(a: Bitmap, b: Bitmap): CompareResult = compareSimilarity(a.positions, b.positions)

  def printBitmap(term: String) = {
    print("Bitmap: ")
    bitmap(term) match {
      case Some(Bitmap(w, h, pos)) => pos.foreach(v => print(v.toString + " "))
      case None => println("Error")
    }
    println()
    println(compareSimilarity(bitmap("apple").get, bitmap("juice").get))
  }

}
