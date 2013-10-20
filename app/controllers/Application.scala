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
//    val feedFetchActor = Akka.system.actorOf(Props(new Actor {
//      def receive = {
//        case "next" => {
//          Logger.info("Fetch feed started")
//          Source.next match {
//            case Some(source) => {
//              Logger.info("Feed %s".format(source.name))
//              source.fetch
//            }
//            case None => Logger.info("All done")
//          }
//
//
//          Logger.info("Fetch feed finished")
//          Akka.system.scheduler.scheduleOnce(10 seconds, self, "next")
//        }
//      }
//    }))
//    Akka.system.scheduler.scheduleOnce(1 minute, feedFetchActor, "next")
    AsyncUtils.start
    Ok("Ok")
  }

}