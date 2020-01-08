
package controllers.repositories

import javax.inject.Inject
import models.{Card, Members}
import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.WriteConcern
import reactivemongo.api.commands.{FindAndModifyCommand, WriteResult}
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.{JSONCollection, _}

import scala.concurrent.{ExecutionContext, Future}


class MemberRepository @Inject()(cc: ControllerComponents,
                                 config: Configuration,
                                 mongo: ReactiveMongoApi)
                                (implicit ec: ExecutionContext) extends AbstractController(cc) {

  private val memberCollection: Future[JSONCollection] = {
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
  def getMemberById(_id: Card): Future[Option[Members]] = {
    memberCollection.flatMap(_.find(
      Json.obj("_id" -> _id._id),
      None
    ).one[Members])
  }

  //PUT
  def addNewMember(newMember: Members): Future[WriteResult] = {
    memberCollection.flatMap(
      _.insert.one(newMember)
    )
  }

  //DELETE
  def deleteMemberById(_id: Card): Future[Option[JsObject]] = {
    memberCollection.flatMap(
      _.findAndRemove(Json.obj("_id" -> _id._id), None, None, WriteConcern.Default, None, None, Seq.empty).map(
        _.value
      )
    )
  }

  //UPDATE
  def updateName(_id: Card, newData: String): Future[Option[Members]] = {
    memberCollection.flatMap {
      result =>
        val selector: JsObject = Json.obj("_id" -> _id._id)
        val modifier: JsObject = Json.obj("$set" -> Json.obj("name" -> newData))
        findAndUpdate(result, selector, modifier).map(_.result[Members])
    }
  }
}

