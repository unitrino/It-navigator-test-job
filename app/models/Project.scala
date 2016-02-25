package models

import play.api.Play
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.driver.PostgresDriver.api._
import slick.profile.SqlAction
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import play.libs
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json._

/**
  * Created by denis on 13.02.16.
  */


object Project {

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  case class UserFullData(first:String,middle:String,last:String,telType:String,numb:String,comments:String)
  case class NewTelephoneData(id:Int,newTelType:String,newTelNumb:String,newComments:String)

  class UsersTable(tag: Tag) extends Table[(Int, String, String, String, String, String, String)](tag, "it_nav_table") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def first_name = column[String]("first_name")
    def middle_name = column[String]("middle_name")
    def last_name = column[String]("last_name")
    def telephone_type = column[String]("telephone_type")
    def telephone_number = column[String]("telephone_number")
    def comments = column[String]("comments")
    def * = (id,first_name, middle_name, last_name, telephone_type, telephone_number, comments)
  }

  val userFormFullData = Form(
    mapping(
      "first" -> nonEmptyText,
      "middle" -> nonEmptyText,
      "last"-> email,
      "telType" -> nonEmptyText,
      "numb" -> nonEmptyText,
      "comments" -> nonEmptyText
    )(UserFullData.apply)(UserFullData.unapply)
  )

  val newTelephoneData = Form (
    mapping(
      "user_id" -> number,
      "new_telephone_type" -> text,
      "new_telephone_numb" -> text,
      "new_comments" -> text
    )(NewTelephoneData.apply)(NewTelephoneData.unapply)
  )

  val usersQuery = TableQuery[UsersTable]
  dbConfig.db.run(usersQuery.schema.create)

  def addNewValue() {
    val actions = DBIO.seq(
      usersQuery += (1,"Иван","Иванович","Иванов","mobile","123456","комментарий 1"),
      usersQuery += (2,"Петя","Вадимович","Сусликов","mobile","1334455","комментарий 2"),
      usersQuery += (3,"Денис","Денисов","Ден","no_info","79799797","комментарий 3"),
      usersQuery += (4,"Аня","Васильевна","Тест","mobile","455969","комментарий 4")
    )
    dbConfig.db.run(actions)
  }

  def getAllRows() = {
    val actions = usersQuery.sortBy(_.id.asc.nullsFirst)
    Await.result(dbConfig.db.run(actions.result),Duration.Inf)
  }

  def setRowsData(elem:JsObject) = {
    val parent = (elem \ "parent").validate[String]
    if(parent.isSuccess) println("Parent: " + parent.get)
    val userID = parent.get
    val number = (elem \ "numbers").validate[String]
    val numbType = (elem \ "numb_types").validate[String]
    val comments = (elem \ "comments").validate[String]

    import scala.collection.mutable.ArrayBuffer

    var stringSQL:ArrayBuffer[String] = ArrayBuffer[String]()

    if(number.isSuccess) {
      val newNumber = number.get
      stringSQL += s"telephone_number = '$newNumber' "
    }
    if(numbType.isSuccess) {
      val newNumbType = numbType.get
      stringSQL += s"telephone_type = '$newNumbType' "
    }
    if(comments.isSuccess) {
      val newComment = comments.get
      stringSQL += s"comments = '$newComment' "
    }
    val body = " SET " + stringSQL.mkString(",")
    val whereState = s" WHERE id = '$userID'"
    val ss = sqlu"UPDATE it_nav_table #$body #$whereState"
    println(ss.statements)
    Await.result(dbConfig.db.run(ss),Duration.Inf)
  }

}
