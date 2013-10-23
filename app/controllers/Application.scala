package controllers

import play.api._
import play.api.mvc._
import play.libs.Akka
import akka.actor.{Actor, Props}
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import models.Source
import util.AsyncUtils

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def test = Action {
    AsyncUtils.start
    Ok("Ok")
  }

}