package controllers

import java.time.LocalDateTime

import repositories.{BowsEmployeeRepository, SessionRepository}
import models.{BowsEmployee, EmployeeId, EmployeePin, UserSession}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.OptionValues._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsResultException, JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import reactivemongo.api.commands.UpdateWriteResult
import reactivemongo.bson.BSONDocument
import reactivemongo.core.errors.DatabaseException

import scala.concurrent.Future

class CrudControllerSpec extends WordSpec with MustMatchers
  with MockitoSugar with ScalaFutures {

  val mockEmployeeRespository: BowsEmployeeRepository = mock[BowsEmployeeRepository]
  val mockSessionRespository: SessionRepository = mock[SessionRepository]

  private lazy val builder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder().overrides(
      bind[BowsEmployeeRepository].toInstance(mockEmployeeRespository),
      bind[SessionRepository].toInstance(mockSessionRespository)
    )

  private val employeeId = EmployeeId("testEmployeeId")
  private val employeePin = EmployeePin("1234")

  "present" must {

    "return ok and delete session if one already exists" in {
      when(mockEmployeeRespository.getBowsEmployeeById(any()))
        .thenReturn(Future.successful(Some(BowsEmployee(employeeId, "testName", "testEmail", "testMobile", employeePin, 0))))

      when(mockSessionRespository.getSession(any()))
        .thenReturn(Future.successful(Some(UserSession("testId", LocalDateTime.now))))

      when(mockSessionRespository.deleteSessionById(any()))
        .thenReturn(Future.successful(UpdateWriteResult.apply(ok = true, 1, 1, Seq.empty, Seq.empty, None, None, None)))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.CrudController.presentCard(employeeId).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe "Goodbye testName"

      app.stop
    }

    "return ok and create new session if none exist" in {
      when(mockEmployeeRespository.getBowsEmployeeById(any()))
        .thenReturn(Future.successful(Some(BowsEmployee(employeeId, "testName", "testEmail", "testMobile", employeePin, 0))))

      when(mockSessionRespository.getSession(any()))
        .thenReturn(Future.successful(None))

      when(mockSessionRespository.createNewSession(any()))
        .thenReturn(Future.successful(UpdateWriteResult.apply(ok = true, 1, 1, Seq.empty, Seq.empty, None, None, None)))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.CrudController.presentCard(employeeId).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe "Hello testName"

      app.stop
    }

    "return BadRequest if employee does not exist" in {
      when(mockEmployeeRespository.getBowsEmployeeById(any()))
        .thenReturn(Future.successful(None))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.CrudController.presentCard(employeeId).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Please register"

      app.stop
    }

    "return BadRequest if data in mongo is invalid" in {
      when(mockEmployeeRespository.getBowsEmployeeById(any()))
        .thenReturn(Future.failed(JsResultException(Seq.empty)))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.CrudController.presentCard(employeeId).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Could not parse Json to Employee model. Incorrect data!"

      app.stop
    }

    "return BadRequest if something else has failed" in {
      when(mockEmployeeRespository.getBowsEmployeeById(any()))
        .thenReturn(Future.failed(new Exception))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.CrudController.presentCard(employeeId).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Something has gone wrong with the following exception: java.lang.Exception"

      app.stop
    }

    "get employee by ID" must {

      "return ok and employee details" in {
        when(mockEmployeeRespository.getBowsEmployeeById(any()))
          .thenReturn(Future.successful(Some(BowsEmployee(employeeId, "testName", "testEmail", "testMobile", employeePin, 0))))
        val app: Application = builder.build()

        val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.CrudController.getId(employeeId).url)

        val result: Future[Result] = route(app, request).value

        status(result) mustBe OK
        contentAsString(result) must contain
        """{
            "_id":card,"name":testName,"email":"testEmail",
            "mobileNumber":"testMobile"}""".stripMargin


        app.stop
      }
      "return 'employee' not found' when id not present with status 404" in {
        when(mockEmployeeRespository.getBowsEmployeeById(any()))
          .thenReturn(Future.successful(None))
        val app: Application = builder.build()

        val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, routes.CrudController.getId(employeeId).url)

        val result: Future[Result] = route(app, request).value

        status(result) mustBe 404
        contentAsString(result) mustBe "Employee not found"

        app.stop
      }
    }

    "addNewEmployee" must {

      "return 'success if valid data is input" in {

        when(mockEmployeeRespository.registerEmployee(any()))
          .thenReturn(Future.successful(UpdateWriteResult.apply(ok = true, 1, 1, Seq.empty, Seq.empty, None, None, None)))

        val employeeJson: JsValue = Json.toJson(BowsEmployee(employeeId, "testName", "testEmail", "testMobile", employeePin, 0))

        val app: Application = builder.build()

        val request: FakeRequest[JsValue] =
          FakeRequest(POST, routes.CrudController.addNewEmployee().url).withBody(employeeJson)

        val result: Future[Result] = route(app, request).value

        status(result) mustBe OK
        contentAsString(result) mustBe "Success"

        app.stop

      }

      "Return BAD_REQUEST and correct error message when invalid data is input" in {

        val employeeJson: JsValue = Json.toJson("Invalid Json")

        val app: Application = builder.build()

        val request =
          FakeRequest(POST, routes.CrudController.addNewEmployee().url).withBody(employeeJson)

        val result: Future[Result] = route(app, request).value

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "Could not parse Json to Employee model. Incorrect data!"

        app.stop

      }

      "Return BAD_REQUEST and correct error message when duplicate data is input" in {

        when(mockEmployeeRespository.registerEmployee(any()))
          .thenReturn(Future.failed(new DatabaseException {
            override def originalDocument: Option[BSONDocument] = None

            override def code: Option[Int] = None

            override def message: String = "Duplicate key"
          }))

        val employeeJson: JsValue = Json.toJson(BowsEmployee(employeeId, "testName", "testEmail", "testMobile", employeePin, 0))

        val app: Application = builder.build()

        val request =
          FakeRequest(POST, routes.CrudController.addNewEmployee().url).withBody(employeeJson)

        val result: Future[Result] = route(app, request).value

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "Could not parse Json to Employee model. Duplicate key error!"

        app.stop

      }

      "Return BAD_REQUEST and correct error message for any other fault" in {

        when(mockEmployeeRespository.registerEmployee(any()))
          .thenReturn(Future.failed(new Exception))

        val employeeJson: JsValue = Json.toJson(BowsEmployee(employeeId, "testName", "testEmail", "testMobile", employeePin, 0))

        val app: Application = builder.build()

        val request =
          FakeRequest(POST, routes.CrudController.addNewEmployee().url).withBody(employeeJson)

        val result: Future[Result] = route(app, request).value

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "Something has gone wrong with the following exception: java.lang.Exception"

        app.stop

      }
    }

    "deleteEmployee" must {

    }
      "Return Ok and correct error message when valid data is input" in {
        when(mockEmployeeRespository.deleteEmployeeById(any()))
          .thenReturn(Future.successful(Some(Json.obj(
            "_id" -> employeeId,
            "name" -> "testName",
            "email" -> "testEmail",
            "mobileNumber" -> "testNumber"
          ))))

        val app: Application = builder.build()

        val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(POST, routes.CrudController.deleteEmployee(employeeId).url)
        val result: Future[Result] = route(app, request).value

        status(result) mustBe OK
        contentAsString(result) mustBe "Success"

        app.stop

      }
    }

    "Return Not found and correct error message when not found data" in {
      when(mockEmployeeRespository.deleteEmployeeById(any()))
        .thenReturn(Future.successful(None
        ))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(POST, routes.CrudController.deleteEmployee(employeeId).url)
      val result: Future[Result] = route(app, request).value

      status(result) mustBe NOT_FOUND
      contentAsString(result) mustBe "Employee not found"

      app.stop
    }

    "Return BAD_REQUEST  and throw exception" in {

      when(mockEmployeeRespository.deleteEmployeeById(any()))
        .thenReturn(Future.failed(new Exception))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(POST, routes.CrudController.deleteEmployee(employeeId).url)
      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Something has gone wrong with the following exception: java.lang.Exception"

      app.stop
    }

  "updateName" must {

    "return success and correct status" in {


      when(mockEmployeeRespository.getBowsEmployeeById(any))
        .thenReturn(Future.successful(Some(BowsEmployee(employeeId, "testName", "testEmail", "testMobile", employeePin, 0))))

      when(mockEmployeeRespository.updateName(any, any))
        .thenReturn(Future.successful(Some(BowsEmployee(employeeId, "testName", "testEmail", "testMobile", employeePin, 0))))

      val app: Application = builder.build()

      val request = FakeRequest(POST, routes.CrudController.updateName(employeeId, "fred").url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe 200
      contentAsString(result) mustBe "Success! updated Employee with id testId's name to fred"

      app.stop
    }

    "return correct error message if employee does not exist in data" in {

      when(mockEmployeeRespository.updateName(any, any))
        .thenReturn(Future.successful(None))

      val app: Application = builder.build()

      val request =
        FakeRequest(POST, routes.CrudController.updateName(employeeId, "fred").url)
      val result: Future[Result] = route(app, request).value

      contentAsString(result) mustBe "No Employee with that id exists in records"
      status(result) mustBe NOT_FOUND

      app.stop
    }

    "return correct error message exception thrown" in {

      when(mockEmployeeRespository.updateName(any, any))
        .thenReturn(Future.failed(new Exception))

      val app: Application = builder.build()

      val request =
        FakeRequest(POST, routes.CrudController.updateName(employeeId, "fred").url)
      val result: Future[Result] = route(app, request).value

      contentAsString(result) mustBe "Something has gone wrong with the following exception: java.lang.Exception"

      app.stop
    }
  }
}