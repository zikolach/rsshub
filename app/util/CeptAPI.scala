package util
import dispatch._, Defaults._
import play.api.libs.json.{JsError, Json}
import play.api.Logger
import scalaz.{Success, Validation, Failure}


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

  case class SimilarTerm(rank: Int,
                         term: String,
                         distance: Double,
                         relDf: Double)
  case class SimilarResponse(time: Int,
                             queryStatus:
                             QueryStatus,
                             term: String,
                             totalSimilarterms: Int,
                             similarterms: List[SimilarTerm])


  implicit val formatBitmap = Json.format[Bitmap]
  implicit val formatQueryStatus = Json.format[QueryStatus]
  implicit val formatBitmapResponse = Json.format[BitmapResponse]
  implicit val formatSimilarTerm = Json.format[SimilarTerm]
  implicit val formatSimilarResponse = Json.format[SimilarResponse]


  private def bitmapSvc(term: String) =
    url("http://api.cept.at/v1/term2bitmap")
      .GET
      .addQueryParameter("term", term)
      .addQueryParameter("app_id", appId)
      .addQueryParameter("app_key", appKey)

  def bitmap(term: String): Option[Bitmap] = {
    Logger.info("Get bitmap for: %s".format(term))
    val res = Http(bitmapSvc(term) OK as.String).option
    res() match {
      case Some(json) => {
        Json.parse(json).validate[BitmapResponse].map(
          r => Some(r.bitmap)
        ).recoverTotal(
          e => {
            Logger.error("Error occurred while parsing json")
            None
          }
        )
      }
      case None => {
        Logger.error("Error occurred while getting bitmap")
        None
      }
    }
  }

  def compareSimilarity(a: Array[Int], b: Array[Int]): CompareResult = {
    val and = a.filter(b.contains).length.toDouble
    val or = (a ++ b).distinct.length.toDouble
    new CompareResult(and / or, (points - (or - and)) / points, and / a.length, and / b.length, (or - and) / or)
  }

  def compareSimilarity(a: Bitmap, b: Bitmap): CompareResult = compareSimilarity(a.positions, b.positions)

  private def similarSvc(term: String,
                         similartermsLimit: Int, similartermsOffset: Int,
                         minRelDf: Double, maxRelDf: Double,
                         posFilter: String, posConfidence: Double) =
    url("http://api.cept.at/v1/similarterms-xl")
      .GET
      .addQueryParameter("term", term)
      .addQueryParameter("similarterms_limit", similartermsLimit.toString)
      .addQueryParameter("similarterms_offset", similartermsOffset.toString)
      .addQueryParameter("min_rel_df", minRelDf.toString)
      .addQueryParameter("max_rel_df", maxRelDf.toString)
      .addQueryParameter("pos_filter", posFilter)
      .addQueryParameter("pos_confidence", posConfidence.toString)
      .addQueryParameter("app_id", appId)
      .addQueryParameter("app_key", appKey)

  def findSimilar(term: String,
                  similarTermsLimit: Int, similartermsOffset: Int,
                  minRelDf: Double, maxRelDf: Double,
                  posFilter: String, posConfidence: Double): Validation[String, List[SimilarTerm]] = {
    require(term.length <= 10000)
    require(term.length > 0)
    Logger.info("Get similar for: %s".format(term))
    val svc = similarSvc(term.toLowerCase, similarTermsLimit, similartermsOffset, minRelDf, maxRelDf, posFilter, posConfidence)
    val res = Http(svc OK as.String).either
    val json = res.apply()
    json match {
      case Left(err) => {
        Failure(err.getMessage)
      }
      case Right(str) => {
        Json.parse(str).validate[SimilarResponse].map(
          r => Success(r.similarterms)
        ).recoverTotal(
          e => Failure(e.toString)
        )
      }
    }

  }

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
