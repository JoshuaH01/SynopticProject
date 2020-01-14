
package repositories

import javax.inject.Inject
import models._
import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.WriteConcern
import reactivemongo.api.commands.{FindAndModifyCommand, WriteResult}
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.{JSONCollection, _}

import scala.concurrent.{ExecutionContext, Future}


class BowsEmployeeRepository @Inject()(cc: ControllerComponents,
                                       config: Configuration,
                                       mongo: ReactiveMongoApi)
                                      (implicit ec: ExecutionContext) extends AbstractController(cc) {

  private val bowsEmployeeRepository: Future[JSONCollection] = {
    mongo.database.map(_.collection[JSONCollection]("employees"))
  }

  private def findAndUpdate(collection: JSONCollection, selection: JsObject,
                            modifier: JsObject): Future[FindAndModifyCommand.Result[collection.pack.type]] = {
    collection.findAndUpdate(
      selector = selection,
      update = modifier,
      fetchNewObject = true,
      upsert = false,
      sort = None,
      fields = None,
      bypassDocumentValidation = false,
      writeConcern = WriteConcern.Default,
      maxTime = None,
      collation = None,
      arrayFilters = Seq.empty
    )
  }

  //GET
  def getBowsEmployeeById(id: EmployeeId): Future[Option[BowsEmployee]] = {
    bowsEmployeeRepository.flatMap(_.find(
      Json.obj("id" -> id.id),
      None
    ).one[BowsEmployee])
  }

  //PUT
  def registerEmployee(newEmployee: BowsEmployee): Future[WriteResult] = {
    bowsEmployeeRepository.flatMap(
      _.insert.one(newEmployee)
    )
  }

  //DELETE
  def deleteEmployeeById(id: EmployeeId): Future[Option[JsObject]] = {
    bowsEmployeeRepository.flatMap(
      _.findAndRemove(Json.obj("id" -> id.id), None, None, WriteConcern.Default, None, None, Seq.empty).map(
        _.value
      )
    )
  }

  //UPDATE
  def updateName(id: EmployeeId, newData: String): Future[Option[BowsEmployee]] = {
    bowsEmployeeRepository.flatMap {
      result =>
        val selector: JsObject = Json.obj("id" -> id.id)
        val modifier: JsObject = Json.obj("$set" -> Json.obj("name" -> newData))
        findAndUpdate(result, selector, modifier).map(_.result[BowsEmployee])
    }
  }

  def updatePin(pin: EmployeePin, newData: String): Future[Option[BowsEmployee]] = {
    bowsEmployeeRepository.flatMap {
      result =>
        val selector: JsObject = Json.obj("pin" -> pin)
        val modifier: JsObject = Json.obj("$set" -> Json.obj("pin" -> newData))
        findAndUpdate(result, selector, modifier).map(_.result[BowsEmployee])
    }
  }

  def increaseBalance(id: EmployeeId, increase: Int): Future[Option[BowsEmployee]] = {
    bowsEmployeeRepository.flatMap {
      result =>
        val selector: JsObject = Json.obj("id" -> id.id)
        val modifier: JsObject = Json.obj("$inc" -> Json.obj("balance" -> increase))
        findAndUpdate(result, selector, modifier).map(_.result[BowsEmployee])

    }
  }

  def decreaseBalance(id: EmployeeId, decrease: Int): Future[Option[BowsEmployee]] = {
    bowsEmployeeRepository.flatMap {
      result =>
        val selector: JsObject = Json.obj("id" -> id.id)
        val modifier: JsObject = Json.obj("$inc" -> Json.obj("balance" -> -decrease))
        findAndUpdate(result, selector, modifier).map(_.result[BowsEmployee])
    }
  }
}

