package models

import models.PersonId.pathBindable
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json

class PersonIdSpec extends WordSpec with OptionValues with MustMatchers {

  "PersonId" must {

    val validPersonId = "dssfd123"

    "Deserialise" in {
      val personId = PersonId(
        _id = validPersonId
      )
      val expectedJson = Json.obj(
        "_id" -> validPersonId
      )
      Json.toJson(personId) mustEqual expectedJson

    }
    "Serialise" in {
      val expectedPersonId = PersonId(
        _id = validPersonId
      )
      val json = Json.obj(
        "_id" -> validPersonId
      )
      json.as[PersonId] mustEqual expectedPersonId
    }
    "return 'Invalid person id' if _id does not match regex" in {

      val invalidPersonId = "!dssuhciehf7833"
      val result = "Invalid person Id"

      pathBindable.bind("", invalidPersonId) mustBe Left(result)

    }
    "return a string" in {

      pathBindable.unbind("", PersonId("testpersonId")) mustEqual "testpersonId"
    }
  }
}
