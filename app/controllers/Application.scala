package controllers

import play.api._
import play.api.mvc._
import util.CeptAPI

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def test = Action {
    CeptAPI.printBitmap("mood")
    Ok("Ok")
  }

}