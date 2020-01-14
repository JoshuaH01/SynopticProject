package models

import org.scalatest._
import play.api.libs.json.Json

class BowsEmployeeSpec extends WordSpec with OptionValues with MustMatchers {

  val employeeId: EmployeeId = EmployeeId("id")
  val employeePin: EmployeePin = EmployeePin("1234")

  "BowsEmployee" must {
    "Deserialize correctly" in {

      val json = Json.obj(
        "id" -> "id",
        "name" -> "Fred",
        "email" -> "a@b.com",
        "mobileNumber" -> "07444345",
        "pin" -> employeePin,
        "balance" -> 0
      )

      val expectedEmployee = BowsEmployee(
        employeeId = employeeId,
        name = "Fred",
        email = "a@b.com",
        mobileNumber = "07444345",
        pin = employeePin,
        balance = 0
      )

      json.as[BowsEmployee] mustEqual expectedEmployee

    }
    "Serialize correctly" in {
      val employee = BowsEmployee(
        employeeId = employeeId,
        name = "Fred",
        email = "a@b.com",
        mobileNumber = "07444345",
        pin = employeePin,
        balance = 0
      )

      val expectedJson = Json.obj(
        "id" -> "id",
        "name" -> "Fred",
        "email" -> "a@b.com",
        "mobileNumber" -> "07444345",
        "pin" -> employeePin,
        "balance" -> 0
      )

      Json.toJson(employee) mustBe expectedJson
    }
  }
}
