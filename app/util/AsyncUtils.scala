package util

import play.libs.Akka
import akka.actor.{Actor, Props}
import play.api.Logger
import models.Source
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._


object AsyncUtils {
  def start = {
    val feedFetchActor = Akka.system.actorOf(Props(new Actor {
      def receive = {
        case "next" => {
          Logger.info("Fetch feed started")
          Source.next match {
            case Some(source) => {
              Logger.info("Feed %s".format(source.name))
              source.fetch
            }
            case None => Logger.info("All done")
          }


          Logger.info("Fetch feed finished")
          Akka.system.scheduler.scheduleOnce(15 seconds, self, "next")
        }
      }
    }))
    Akka.system.scheduler.scheduleOnce(1 second, feedFetchActor, "next")
  }
}
