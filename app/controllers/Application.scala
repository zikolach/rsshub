package controllers

import play.api._
import play.api.mvc._
import util.CeptAPI

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def test = Action {
    val sims = CeptAPI.findSimilar("test", 10, 0, 0, 1, "N", 0.95).get
    sims.foreach(println)
    Ok("Ok")
  }

}