package models

import org.scalatest._
import play.api.libs.json.Json

class PersonSpec extends WordSpec with OptionValues with MustMatchers {

  val id: PersonId = PersonId("id")

  "Person" must {
    "Deserialize correctly" in {

      val json = Json.obj(
        "_id" -> "id",
        "name" -> "Fred",
        "email" -> "a@b.com",
        "mobileNumber" -> "07444345"
      )

      val expectedPerson = Person(
        _id = card,
        name = "Fred",
        email = "a@b.com",
        mobileNumber = "07444345"
      )

      json.as[Person] mustEqual expectedPerson

    }
    "Serialize correctly" in {
      val person = Person(
        _id = card,
        name = "Fred",
        email = "a@b.com",
        mobileNumber = "07444345"
      )

      val expectedJson = Json.obj(
        "_id" -> "id",
        "name" -> "Fred",
        "email" -> "a@b.com",
        "mobileNumber" -> "07444345",
      )

      Json.toJson(person) mustBe expectedJson
    }
  }
}
