package controllers

import java.time.LocalDateTime

import javax.inject.Inject
import models._
import play.api.Configuration
import play.api.libs.json.{JsResultException, JsValue, Json}
import play.api.mvc._
import reactivemongo.core.errors.DatabaseException
import repositories.{BowsEmployeeRepository, SessionRepository}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class BowsController @Inject()(cc: ControllerComponents,
                               config: Configuration,
                               bowsEmployeeRepository: BowsEmployeeRepository,
                               sessionRepository: SessionRepository)
                              (implicit ec: ExecutionContext) extends AbstractController(cc) {


  def presentCard(id: EmployeeId) = Action.async {
    implicit request =>
      bowsEmployeeRepository.getBowsEmployeeById(id).flatMap {
        case Some(bowsEmployee) =>
          sessionRepository.getSession(id).flatMap {
            case Some(_) =>
              sessionRepository.deleteSessionById(id).map(_ => Ok(s"Goodbye ${bowsEmployee.name}"))
            case None =>
              sessionRepository.createNewSession(UserSession(id.id, LocalDateTime.now))


                .map(_ => Ok(s"Hello ${bowsEmployee.name}"))

          }

        case None => Future.successful(BadRequest("Please register"))
      } recoverWith {
        case _: JsResultException =>
          Future.successful(BadRequest(s"Could not parse Json to Employee model. Incorrect data!"))
        case e =>
          Future.successful(BadRequest(s"Something has gone wrong with the following exception: $e"))
      }
  }


  def getId(id: EmployeeId): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      bowsEmployeeRepository.getBowsEmployeeById(id).map {
        case None => NotFound("Employee not found")
        case Some(employee) => Ok(Json.toJson(employee))
      } recoverWith {
        case _: JsResultException =>
          Future.successful(BadRequest(s"Could not parse Json to Employee model."))
        case exception =>
          Future.successful(BadRequest(s"Something has gone wrong with the following exception: $exception"))
      }
  }

  def getBalance(id: EmployeeId) = Action.async {
    implicit request: Request[AnyContent] =>
      bowsEmployeeRepository.getBowsEmployeeById(id).map {
        case Some(member) => Ok(Json.toJson(member.balance))
        case None => NotFound("employee not found!")
      } recoverWith {
        case _: JsResultException =>
          Future.successful(BadRequest(s"Could not parse Json to employee model. Incorrect data!"))
        case e =>
          Future.successful(BadRequest(s"Something has gone wrong with the following exception: $e"))
      }
  }


  def getEmployeeName(id: EmployeeId) = Action.async {
    implicit request: Request[AnyContent] =>
      bowsEmployeeRepository.getBowsEmployeeById(id).map {
        case Some(employee) => Ok(Json.toJson(employee.name))
        case None => NotFound("Employee not found!")
      } recoverWith {
        case _: JsResultException =>
          Future.successful(BadRequest(s"Could not parse Json to Employee model. Incorrect data!"))
        case e =>
          Future.successful(BadRequest(s"Something has gone wrong with the following exception: $e"))
      }
  }

  def addNewEmployee: Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      (for {
        employee <- Future.fromTry(Try {
          request.body.as[BowsEmployee]
        })
        result <- bowsEmployeeRepository.registerEmployee(employee)
      } yield Ok("Success")).recoverWith {
        case e: JsResultException =>
          Future.successful(BadRequest(s"Could not parse Json to Employee model. Incorrect data!"))
        case e: DatabaseException =>
          Future.successful(BadRequest(s"Could not parse Json to Employee model. Duplicate key error!"))
        case e =>
          Future.successful(BadRequest(s"Something has gone wrong with the following exception: $e"))
      }
  }

  def deleteEmployee(id: EmployeeId) = Action.async {
    implicit request =>
      bowsEmployeeRepository.deleteEmployeeById(id).map {
        case Some(_) => Ok("Success")
        case _ => NotFound("Employee not found")
      } recoverWith {
        case e =>
          Future.successful(BadRequest(s"Something has gone wrong with the following exception: $e"))
      }
  }

  def updateName(id: EmployeeId, newData: String): Action[AnyContent] = Action.async {
    implicit request =>
      bowsEmployeeRepository.updateName(id, newData).map {

        case Some(employee) =>
          Ok(s"Success! updated Employee with id ${employee.employeeId.id}'s name to $newData")
        case _ =>
          NotFound("No Employee with that id exists in records")
      } recoverWith {
        case exception =>
          Future.successful(BadRequest(s"Something has gone wrong with the following exception: $exception"))
      }
  }

  def updatePin(pin: EmployeePin, newData: String): Action[AnyContent] = Action.async {
    implicit request =>
      bowsEmployeeRepository.updatePin(pin, newData).map {

        case Some(employee) =>
          Ok(s"Success! updated ${employee.name}'s pin")
        case _ =>
          NotFound("No Employee with that id exists in records")
      } recoverWith {
        case exception =>
          Future.successful(BadRequest(s"Something has gone wrong with the following exception: $exception"))
      }
  }

  def increaseBalance(id: EmployeeId, increase: Int): Action[AnyContent] = Action.async {

    bowsEmployeeRepository.getBowsEmployeeById(id).flatMap {
      case Some(_) =>
        increase match {
          case x if x <= 0 => Future.successful(BadRequest("Balance increase must be greater than zero"))
          case _ =>
            bowsEmployeeRepository.getBowsEmployeeById(id).flatMap {
              case Some(_) => bowsEmployeeRepository.increaseBalance(id, increase)
                .map { _ => Ok(s"Balance updated!") }
            }
        }
      case None => Future.successful(NotFound("No employee with that id exists in records"))
    } recoverWith {
      case _ => Future.successful(BadRequest(s"Could not parse Json to Employee model. Incorrect data!"))
      case e => Future.successful(BadRequest(s"Something has gone wrong with the following exception: $e"))

    }
  }

  def decreaseBalance(id: EmployeeId, decrease: Int): Action[AnyContent] = Action.async {
    bowsEmployeeRepository.getBowsEmployeeById(id).flatMap {
      case Some(employee) => {
        decrease match {
          case x if x <= 0 => Future.successful(BadRequest("Balance increase must be greater than zero"))
          case x if x > employee.balance => Future.successful(BadRequest("Decrease cannot be greater than current balance"))
          case _ =>
            bowsEmployeeRepository.getBowsEmployeeById(id).flatMap {
              case Some(employee) =>
                bowsEmployeeRepository.decreaseBalance(id, decrease).map {
                  case Some(_) => Ok("Balance updated!")
                  case None => NotFound("Employee not found")
                }
            }
        }

      }
      case None => Future.successful(NotFound("No employee with that id exists in records"))

    }.recoverWith {
      case _ => Future.successful(BadRequest(s"Could not parse Json to Employee model. Incorrect data!"))
      case e => Future.successful(BadRequest(s"Something has gone wrong with the following exception: $e"))
    }
  }

}