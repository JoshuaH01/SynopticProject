package models

import models.EmployeeId.pathBindable
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json


class EmployeeIdSpec extends WordSpec with OptionValues with MustMatchers {

  "EmployeeId" must {

    val validEmployeeId = "dssfd123"

    "Deserialise" in {
      val employeeId = EmployeeId(
        id = validEmployeeId
      )
      val expectedJson = Json.obj(
        "id" -> validEmployeeId
      )
      Json.toJson(employeeId) mustEqual expectedJson

    }
    "Serialise" in {
      val expectedEmployeeId = EmployeeId(
        id = validEmployeeId
      )
      val json = Json.obj(
        "id" -> validEmployeeId
      )
      json.as[EmployeeId] mustEqual expectedEmployeeId
    }
    "return 'Invalid employee id' if id does not match regex" in {

      val invalidEmployeeId = "!dssuhciehf7833"
      val result = "Invalid employee Id"

      pathBindable.bind("", invalidEmployeeId) mustBe Left(result)

    }
    "return a string" in {

      pathBindable.unbind("", EmployeeId("testpersonId")) mustEqual "testpersonId"
    }
  }
}
