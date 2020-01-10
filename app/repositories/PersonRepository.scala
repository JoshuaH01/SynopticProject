
package repositories

import javax.inject.Inject
import models.{Person, PersonId}
import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.WriteConcern
import reactivemongo.api.commands.{FindAndModifyCommand, WriteResult}
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.{JSONCollection, _}

import scala.concurrent.{ExecutionContext, Future}


class PersonRepository @Inject()(cc: ControllerComponents,
                                 config: Configuration,
                                 mongo: ReactiveMongoApi)
                                (implicit ec: ExecutionContext) extends AbstractController(cc) {

  private val personCollection: Future[JSONCollection] = {
    mongo.database.map(_.collection[JSONCollection]("people"))
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
  def getPersonById(_id: PersonId): Future[Option[Person]] = {
    personCollection.flatMap(_.find(
      Json.obj("_id" -> _id._id),
      None
    ).one[Person])
  }

  //PUT
  def addNewPerson(newPerson: Person): Future[WriteResult] = {
    personCollection.flatMap(
      _.insert.one(newPerson)
    )
  }

  //DELETE
  def deletePersonById(_id: PersonId): Future[Option[JsObject]] = {
    personCollection.flatMap(
      _.findAndRemove(Json.obj("_id" -> _id._id), None, None, WriteConcern.Default, None, None, Seq.empty).map(
        _.value
      )
    )
  }

  //UPDATE
  def updateName(_id: PersonId, newData: String): Future[Option[Person]] = {
    personCollection.flatMap {
      result =>
        val selector: JsObject = Json.obj("_id" -> _id._id)
        val modifier: JsObject = Json.obj("$set" -> Json.obj("name" -> newData))
        findAndUpdate(result, selector, modifier).map(_.result[Person])
    }
  }
}

