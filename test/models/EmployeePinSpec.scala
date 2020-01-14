package models

import models.EmployeePin.pathBindable
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json

class EmployeePinSpec extends WordSpec with OptionValues with MustMatchers {

  "EmployeeId" must {

    val validEmployeePin = "1342"

    "Deserialise" in {
      val employeeId = EmployeePin(
        pin = validEmployeePin
      )
      val expectedJson = Json.obj(
        "pin" -> validEmployeePin
      )
      Json.toJson(employeeId) mustEqual expectedJson

    }

    "Serialise" in {
      val expectedEmployeePin = EmployeePin(
        pin = validEmployeePin
      )
      val json = Json.obj(
        "pin" -> validEmployeePin
      )
      json.as[EmployeePin] mustEqual expectedEmployeePin
    }

    "return 'Invalid employee id' if id does not match regex" in {

      val invalidEmployeePin = "£$%^&*()(*&^%$"
      val result = "Invalid employee Pin"

      pathBindable.bind("", invalidEmployeePin) mustBe Left(result)

    }

    "return a string" in {

      pathBindable.unbind("", EmployeePin("1234")) mustEqual "1234"
    }
  }
}
