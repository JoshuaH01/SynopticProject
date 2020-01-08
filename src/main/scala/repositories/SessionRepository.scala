package controllers.repositories

import javax.inject.Inject
import models.{Card, UserSession}
import play.api.Configuration
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.{JSONCollection, _}

import scala.concurrent.{ExecutionContext, Future}

class SessionRepository @Inject()(mongo: ReactiveMongoApi, config: Configuration,
                                  memberRepository: MemberRepository)(implicit ec: ExecutionContext) {

  private val sessionCollection: Future[JSONCollection] =
    mongo.database.map(_.collection[JSONCollection]("session"))

  val ttl: Int = config.get[Int]("session.ttl")

  private val index: Index = Index(
    key = Seq("lastUpdated" -> IndexType.Ascending),
    name = Some("session-index"),
    options = BSONDocument("expireAfterSeconds" -> ttl)
  )

  sessionCollection.map(_.indexesManager.ensure(index))


  def createNewSession(session: UserSession): Future[WriteResult] = {

    sessionCollection.flatMap(
      _.insert.one(session))

  }

  def getSession(_id: Card): Future[Option[UserSession]] = {
    sessionCollection.flatMap(_.find(
      Json.obj("_id" -> _id._id),
      None
    ).one[UserSession])
  }

  def deleteSessionById(_id: Card): Future[WriteResult] = {
    sessionCollection.flatMap(
      _.delete.one(Json.obj("_id" -> _id._id))
    )
  }


}





