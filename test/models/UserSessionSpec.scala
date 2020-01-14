package models


import java.time.LocalDateTime

import org.scalatest.{FreeSpec, MustMatchers}
import play.api.libs.json.Json
import requests.MongoDateTimeFormats

class UserSessionSpec extends FreeSpec with MustMatchers with MongoDateTimeFormats {

  "UserSession model" - {

    val id = "345dcfvb"
    val time = LocalDateTime.now

    "must serialise into JSON" in {

      val userSession = UserSession(
        id = id,
        lastUpdated = time
      )

      val expectedJson = Json.obj(
        "id" -> id,
        "lastUpdated" -> time
      )

      Json.toJson(userSession) mustEqual expectedJson
    }

    "must deserialise from JSON" in {

      val json = Json.obj(
        "id" -> id,
        "lastUpdated" -> time
      )

      val expectedUser = UserSession(
        id = id,
        lastUpdated = time.minusHours(0)
      )

      json.as[UserSession] mustEqual expectedUser
    }
  }

}
