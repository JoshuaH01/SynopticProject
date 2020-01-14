package models


import java.time.LocalDateTime

import play.api.libs.json._
import requests.MongoDateTimeFormats

case class UserSession(id: String, lastUpdated: LocalDateTime)

object UserSession extends MongoDateTimeFormats {
  implicit lazy val format: OFormat[UserSession] = Json.format
}