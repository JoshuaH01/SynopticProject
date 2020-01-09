package controllers

import java.time.LocalDateTime

import models.{Person, PersonId, UserSession}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsResultException, JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, POST, contentAsString, route, status}
import reactivemongo.api.commands.UpdateWriteResult
import reactivemongo.bson.BSONDocument
import reactivemongo.core.errors.DatabaseException
import repositories.{PersonRepository, SessionRepository}

import scala.concurrent.Future

class CrudControllerSpec extends WordSpec with MustMatchers
  with MockitoSugar with ScalaFutures {

  val mockPersonRespository: PersonRepository = mock[PersonRepository]
  val mockSessionRespository: SessionRepository = mock[SessionRepository]

  private lazy val builder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder().overrides(
      bind[PersonRepository].toInstance(mockPersonRespository),
      bind[SessionRepository].toInstance(mockSessionRespository)
    )

  private val personId = PersonId("testPersonId")

  "present" must {

    "return ok and delete session if one already exists" in {
      when(mockPersonRespository.getPersonById(any()))
        .thenReturn(Future.successful(Some(Person(personId, "testName", "testEmail", "testMobile"))))

      when(mockSessionRespository.getSession(any()))
        .thenReturn(Future.successful(Some(UserSession("testId", LocalDateTime.now))))

      when(mockSessionRespository.deleteSessionById(any()))
        .thenReturn(Future.successful(UpdateWriteResult.apply(ok = true, 1, 1, Seq.empty, Seq.empty, None, None, None)))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, appRoutes.CrudController.present(PersonId("testId")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe "Goodbye testName"

      app.stop
    }

    "return ok and create new session if none exist" in {
      when(mockPersonRespository.getPersonById(any()))
        .thenReturn(Future.successful(Some(Person(personId, "testName", "testEmail", "testMobile"))))

      when(mockSessionRespository.getSession(any()))
        .thenReturn(Future.successful(None))

      when(mockSessionRespository.createNewSession(any()))
        .thenReturn(Future.successful(UpdateWriteResult.apply(ok = true, 1, 1, Seq.empty, Seq.empty, None, None, None)))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, appRoutes.CrudController.present(PersonId("testId")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe "Hello testName"

      app.stop
    }

    "return BadRequest if member does not exist" in {
      when(mockPersonRespository.getPersonById(any()))
        .thenReturn(Future.successful(None))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, appRoutes.CrudController.present(PersonId("testId")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Please register"

      app.stop
    }

    "return BadRequest if data in mongo is invalid" in {
      when(mockPersonRespository.getPersonById(any()))
        .thenReturn(Future.failed(JsResultException(Seq.empty)))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, appRoutes.CrudController.present(PersonId("testId")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Could not parse Json to Members model. Incorrect data!"

      app.stop
    }

    "return BadRequest if something else has failed" in {
      when(mockPersonRespository.getPersonById(any()))
        .thenReturn(Future.failed(new Exception))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, appRoutes.CrudController.present(PersonId("testId")).url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Something has gone wrong with the following exception: java.lang.Exception"

      app.stop
    }

    "getmemberById" must {
      "return ok and members details" in {
        when(mockPersonRespository.getPersonById(any()))
          .thenReturn(Future.successful(Some(Person(personId, "testName", "testEmail", "testMobile"))))
        val app: Application = builder.build()

        val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, appRoutes.CrudController.getMemberById(PersonId("testId")).url)

        val result: Future[Result] = route(app, request).value

        status(result) mustBe OK
        contentAsString(result) must contain
        """{
            "_id":card,"name":testName,"email":"testEmail",
            "mobileNumber":"testMobile","balance":123,"securityNumber":123}""".stripMargin


        app.stop
      }
      "return 'member' not found' when id not present with status 404" in {
        when(mockPersonRespository.getPersonById(any()))
          .thenReturn(Future.successful(None))
        val app: Application = builder.build()

        val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, appRoutes.CrudController.getMemberById(PersonId("wrongId")).url)

        val result: Future[Result] = route(app, request).value

        status(result) mustBe 404
        contentAsString(result) mustBe "Member not found"

        app.stop
      }
    }
    "getBalance" must {
      "return NOT_FOUND and correct error message when invalid request input" in {

        when(mockPersonRespository.getPersonById(any()))
          .thenReturn(Future.successful(None))

        val app: Application = builder.build()

        val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, appRoutes.CrudController.getBalance
        (PersonId("testId")).url)

        val result: Future[Result] = route(app, request).value

        status(result) mustBe NOT_FOUND
        contentAsString(result) mustBe "Member not found!"

        app.stop

      }
      "return correct balance and status ok when correct request input" in {

        when(mockPersonRespository.getPersonById(any()))
          .thenReturn(Future.successful(Some(Person(personId, "testName", "testEmail", "testMobile"))))

        val app: Application = builder.build()

        val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, appRoutes.CrudController
          .getBalance(PersonId("testId")).url)

        val result: Future[Result] = route(app, request).value

        status(result) mustBe OK
        contentAsString(result) mustBe "123"

        app.stop

      }
    }

    "addNewMember" must {

      "return 'success if valid data is input" in {

        when(mockPersonRespository.addNewPerson(any()))
          .thenReturn(Future.successful(UpdateWriteResult.apply(ok = true, 1, 1, Seq.empty, Seq.empty, None, None, None)))

        val membersJson: JsValue = Json.toJson(Person(personId, "test", "test", "test"))

        val app: Application = builder.build()

        val request: FakeRequest[JsValue] =
          FakeRequest(POST, appRoutes.CrudController.addNewMember().url).withBody(membersJson)

        val result: Future[Result] = route(app, request).value

        status(result) mustBe OK
        contentAsString(result) mustBe "Success"

        app.stop

      }

      "Return BAD_REQUEST and correct error message when invalid data is input" in {

        val membersJson: JsValue = Json.toJson("Invalid Json")

        val app: Application = builder.build()

        val request =
          FakeRequest(POST, appRoutes.CrudController.addNewMember().url).withBody(membersJson)

        val result: Future[Result] = route(app, request).value

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "Could not parse Json to Member model. Incorrect data!"

        app.stop

      }

      "Return BAD_REQUEST and correct error message when duplicate data is input" in {

        when(mockPersonRespository.addNewPerson(any()))
          .thenReturn(Future.failed(new DatabaseException {
            override def originalDocument: Option[BSONDocument] = None

            override def code: Option[Int] = None

            override def message: String = "Duplicate key"
          }))

        val membersJson: JsValue = Json.toJson(Person(personId, "test", "test", "test"))

        val app: Application = builder.build()

        val request =
          FakeRequest(POST, appRoutes.CrudController.addNewMember().url).withBody(membersJson)

        val result: Future[Result] = route(app, request).value

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "Could not parse Json to Member model. Duplicate key error!"

        app.stop

      }

      "Return BAD_REQUEST and correct error message for any other fault" in {

        when(mockPersonRespository.addNewPerson(any()))
          .thenReturn(Future.failed(new Exception))

        val membersJson: JsValue = Json.toJson(Person(personId, "test", "test", "test"))

        val app: Application = builder.build()

        val request =
          FakeRequest(POST, appRoutes.CrudController.addNewMember().url).withBody(membersJson)

        val result: Future[Result] = route(app, request).value

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "Something has gone wrong with the following exception: java.lang.Exception"

        app.stop

      }
    }

    "deleteMember" must {

    }
      "Return Ok and correct error message when valid data is input" in {
        when(mockPersonRespository.deletePersonById(any()))
          .thenReturn(Future.successful(Some(Json.obj(
            "_id" -> personId,
            "name" -> "testName",
            "email" -> "testEmail",
            "mobileNumber" -> "testNumber",
            "balance" -> 123,
            "securityNumber" -> 123
          ))))

        val app: Application = builder.build()

        val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(POST, appRoutes.CrudController.deleteMember(PersonId("testId")).url)
        val result: Future[Result] = route(app, request).value

        status(result) mustBe OK
        contentAsString(result) mustBe "Success"

        app.stop

      }
    }

    "Return Not found and correct error message when not found data" in {
      when(mockPersonRespository.deletePersonById(any()))
        .thenReturn(Future.successful(None
        ))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(POST, appRoutes.CrudController.deleteMember(PersonId("testId")).url)
      val result: Future[Result] = route(app, request).value

      status(result) mustBe NOT_FOUND
      contentAsString(result) mustBe "Member not found"

      app.stop
    }

    "Return BAD_REQUEST  and throw exception" in {
      when(mockPersonRespository.deletePersonById(any()))
        .thenReturn(Future.failed(new Exception))

      val app: Application = builder.build()

      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(POST, appRoutes.CrudController.deleteMember(PersonId("testId")).url)
      val result: Future[Result] = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Something has gone wrong with the following exception: java.lang.Exception"

      app.stop
    }

    "return correct error message and status if member not found" in {

      when(mockPersonRespository.getPersonById(PersonId("dftgyh")))
        .thenReturn(Future.successful(None))

      val app: Application = builder.build()

      val request =
        FakeRequest(POST, appRoutes.CrudController.increaseBalance(PersonId("dftgyh")).url)
      val result: Future[Result] = route(app, request).value

      contentAsString(result) mustBe "No Member with that id exists in records"

      app.stop
    }

    "return correct error message and status if member not found" in {
      when(mockPersonRespository.getPersonById(any))
        .thenReturn(Future.successful(Some(Person(personId, "test", "test", "test"))))

      when(mockPersonRespository.getPersonById(PersonId("dftgyh")))
        .thenReturn(Future.successful(None))

      val app: Application = builder.build()

      val request =
        FakeRequest(POST, appRoutes.CrudController.decreaseBalance(PersonId("dftgyh"), 234).url)
      val result: Future[Result] = route(app, request).value

      contentAsString(result) mustBe "No Member with that id exists in records"

      app.stop
    }

  "updateMemberName" must {

    "return success and correct status" in {

      when(mockPersonRespository.updateName(any, any))
        .thenReturn(Future.successful(Some(Person(personId, "testName", "test", "test"))))

      when(mockPersonRespository.getPersonById(any))
        .thenReturn(Future.successful(Some(Person(personId, "testName", "test", "test"))))


      val app: Application = builder.build()

      val request = FakeRequest(POST, appRoutes.CrudController.updateMemberName(PersonId("testName"), "fred").url)

      val result: Future[Result] = route(app, request).value

      status(result) mustBe 200
      contentAsString(result) mustBe "Success! updated Member with id testId's name to fred"

      app.stop
    }

    "return correct error message if member does not exist in data" in {

      when(mockPersonRespository.updateName(any, any))
        .thenReturn(Future.successful(None))

      val app: Application = builder.build()

      val request =
        FakeRequest(POST, appRoutes.CrudController.updateMemberName(PersonId("dftgyh"), "fred").url)
      val result: Future[Result] = route(app, request).value

      contentAsString(result) mustBe "No Member with that id exists in records"
      status(result) mustBe NOT_FOUND


      app.stop
    }

    "return correct error message exception thrown" in {

      when(mockPersonRespository.updateName(any, any))
        .thenReturn(Future.failed(new Exception))

      val app: Application = builder.build()

      val request =
        FakeRequest(POST, appRoutes.CrudController.updateMemberName(PersonId("dftgyh"), "fred").url)
      val result: Future[Result] = route(app, request).value

      contentAsString(result) mustBe "Something has gone wrong with the following exception: java.lang.Exception"

      app.stop
    }
  }
}