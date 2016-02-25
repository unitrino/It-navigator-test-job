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

  //SITEMAP

  def measureTime (input: Map[String,Long]):Future[Map[String,Long]] = {
    val futureArray:Future[Map[String,Long]] = scala.concurrent.Future {
      input.map {
        case (k,v) => {
          val startTime = System.currentTimeMillis()
          try {
            val _ = scala.io.Source.fromURL(k)
            k -> (System.currentTimeMillis() - startTime)
          }
          catch {
            case _:FileNotFoundException => println("No file")
              k -> 0L
          }
        }
      }
    }
    futureArray
  }

  def send_req = Action.async {
    import scala.xml.{XML => xxmmll}
    val xmlData = xxmmll.load("http://afisha.zp.ua/sitemap.xml")//Array("http://afisha.zp.ua/", "http://afisha.zp.ua/cluby/", "http://afisha.zp.ua/cluby/dj-greenev_2346.html")
    val arr:Map[String,Long] = (xmlData \ "url" \ "loc").map{
      elem => (elem.text,0L)
    }.toMap
    val answer:Future[Map[String,Long]] = measureTime(arr.slice(0,100))//arr.zipWithIndex.map(t => (t._1, t._2.toLong)).toMap)
    answer.map(i => Ok(views.html.graph(Json.toJson(i).toString())))

//    //    import play.api.libs.ws.ning._
//    //    import play.api.libs.ws._
//    //    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
//    //    import scala.concurrent.{Future, Await}
//    //    import scala.concurrent.duration._
//    //
//          import play.api.libs.ws.ning._
//          import play.api.libs.ws._
//          implicit val sslClient = NingWSClient()
//          val ss = System.currentTimeMillis()
//          WS.clientUrl("http://www.google.com.ua").withRequestTimeout(5000).get().map{
//            s => val r =  System.currentTimeMillis() - ss
//              Ok("Measure  " + r)
//          }
  }

  case class DataSet(url:String,status:Int,start:Long,end:Long)

  def measure (input: List[DataSet]):Future[List[DataSet]] = {
    import play.api.libs.ws.ning._
    val startTime = System.currentTimeMillis()
    val futureArray:Future[List[DataSet]] = scala.concurrent.Future {
      input.map {
        case elem => {
          val start = System.currentTimeMillis()
          //try {
            implicit val sslClient = NingWSClient()
            val futClient = Await.result(WS.clientUrl(elem.url).withRequestTimeout(5000).get(),Duration.Inf)//scala.io.Source.fromURL(elem.url,"utf-8")
            DataSet(elem.url,futClient.status, start - startTime,start - startTime + (System.currentTimeMillis() - start))
          //}
//          catch {
//            case _:FileNotFoundException => println("No file")
//              println(elem.url)
//              DataSet(elem.url,0L,0L)
//            case _:IOException => println("No file2")
//              println(elem.url)
//              DataSet(elem.url,0L,0L)
//          }
        }
      }
    }
    futureArray
  }

  def client_send = Action.async {
      import play.api.libs.ws.ning._
      import play.api.libs.ws._
      import scala.util.matching.Regex
      implicit val sslClient = NingWSClient()
      val pattern = new Regex("(http|https)://(([a-zA-Z0-9_-]+)(\\.)*(\\/)*)+")
      val allHtml = WS.clientUrl("http://rabota.ua/").withRequestTimeout(5000).get().map{
        elem => pattern.findAllMatchIn(elem.body).map {
          all =>
            DataSet(all.group(0),0,0L,0L)
        }.toList
      }

    import play.api.libs.json._
    import play.api.libs.functional.syntax._

    implicit val locationWrites: Writes[DataSet] = (
      (JsPath \ "url").write[String] and
      (JsPath \ "status").write[Int] and
      (JsPath \ "start").write[Long] and
      (JsPath \ "end").write[Long]
      )(unlift(DataSet.unapply))

    val answer:Future[List[DataSet]] = allHtml.flatMap(measure)
    answer.map{i =>
      println(Json.toJson(i).toString());
      Ok(views.html.graph(Json.toJson(i).toString()))
    }
  }

}