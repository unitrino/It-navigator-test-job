package controllers

import java.io.{IOException, FileNotFoundException}

import models.Project
import play.api._
import play.api.libs.ws.WS
import play.api.mvc._
import play.twirl.api.Html
import play.api.data._
import play.api.data.Forms._
import models.Project._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext



object Application extends Controller {

  def index = Action {
    val all = getAllRows().toArray
    addNewValue()
    Ok(views.html.index(all))

  }

  def save = Action(parse.json) {
    implicit request =>
      val a = request.body.as[List[JsObject]]
      a.foreach(setRowsData(_))
      val all = getAllRows().toArray
      Ok(views.html.table(all))
  }

}
