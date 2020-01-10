package controllers

import java.time.LocalDateTime

import javax.inject.Inject
import models._
import play.api.Configuration
import play.api.libs.json.{JsResultException, JsValue, Json}
import play.api.mvc._
import reactivemongo.core.errors.DatabaseException
import repositories.{PersonRepository, SessionRepository}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class CrudController @Inject()(cc: ControllerComponents,
                               config: Configuration,
                               personRepository: PersonRepository,
                               sessionRepository: SessionRepository)
                              (implicit ec: ExecutionContext) extends AbstractController(cc) {


  def present(_id: PersonId) = Action.async {
    implicit request =>
      personRepository.getPersonById(_id).flatMap {
        case Some(person) =>
          sessionRepository.getSession(_id).flatMap {
            case Some(_) =>
              sessionRepository.deleteSessionById(_id).map(_ => Ok(s"Goodbye ${person.name}"))
            case None =>
              sessionRepository.createNewSession(UserSession(_id._id, LocalDateTime.now))


                .map(_ => Ok(s"Hello ${person.name}"))

          }

        case None => Future.successful(BadRequest("Please register"))
      } recoverWith {
        case _: JsResultException =>
          Future.successful(BadRequest(s"Could not parse Json to Person model. Incorrect data!"))
        case e =>
          Future.successful(BadRequest(s"Something has gone wrong with the following exception: $e"))
      }
  }

  //GET
  def getId(_id: PersonId): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      personRepository.getPersonById(_id).map {
        case None => NotFound("Person not found")
        case Some(person) => Ok(Json.toJson(person))
      } recoverWith {
        case _: JsResultException =>
          Future.successful(BadRequest(s"Could not parse Json to Person model."))
        case exception =>
          Future.successful(BadRequest(s"Something has gone wrong with the following exception: $exception"))
      }
  }

  //GET
  def getName(_id: PersonId) = Action.async {
    implicit request: Request[AnyContent] =>
      personRepository.getPersonById(_id).map {
        case Some(person) => Ok(Json.toJson(person.name))
        case None => NotFound("Person not found!")
      } recoverWith {
        case _: JsResultException =>
          Future.successful(BadRequest(s"Could not parse Json to Persons model. Incorrect data!"))
        case e =>
          Future.successful(BadRequest(s"Something has gone wrong with the following exception: $e"))
      }
  }

  def addNewPerson: Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      (for {
        person <- Future.fromTry(Try {
          request.body.as[Person]
        })
        result <- personRepository.addNewPerson(person)
      } yield Ok("Success")).recoverWith {
        case e: JsResultException =>
          Future.successful(BadRequest(s"Could not parse Json to Person model. Incorrect data!"))
        case e: DatabaseException =>
          Future.successful(BadRequest(s"Could not parse Json to Person model. Duplicate key error!"))
        case e =>
          Future.successful(BadRequest(s"Something has gone wrong with the following exception: $e"))
      }
  }

  def deletePerson(_id: PersonId) = Action.async {
    implicit request =>
      personRepository.deletePersonById(_id).map {
        case Some(_) => Ok("Success")
        case _ => NotFound("Person not found")
      } recoverWith {
        case e =>
          Future.successful(BadRequest(s"Something has gone wrong with the following exception: $e"))
      }
  }

  //POST
  def updateName(_id: PersonId, newData: String): Action[AnyContent] = Action.async {
    implicit request =>
      personRepository.updateName(_id, newData).map {

        case Some(person) =>
          Ok(s"Success! updated Person with id ${person._id._id}'s name to $newData")
        case _ =>
          NotFound("No Person with that id exists in records")
      } recoverWith {
        case exception =>
          Future.successful(BadRequest(s"Something has gone wrong with the following exception: $exception"))
      }
  }
}