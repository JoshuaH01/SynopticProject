package mongoDateTimeFormats

import java.time.{LocalDate, LocalDateTime}

import requests.MongoDateTimeFormats
import org.scalatest.{FreeSpec, MustMatchers, OptionValues}
import play.api.libs.json.Json

class MongoDateTimeFormatsSpec extends FreeSpec with MustMatchers with OptionValues with MongoDateTimeFormats {

  "a LocalDateTime" - {

    val date = LocalDate.of(2020, 1, 1).atStartOfDay

    val dateMillis = 1577836800000L

    val json = Json.obj(
      "$date" -> dateMillis
    )

    "must serialise to json" in {
      val result = Json.toJson(date)
      result mustEqual Json.obj(
        "$date" -> (dateMillis - 0)
      )
    }

    "must deserialise from json" in {
      val result = json.as[LocalDateTime]
      result mustEqual date
    }

    "must serialise/deserialise to the same value" in {
      val result = Json.toJson(date).as[LocalDateTime]
      result mustEqual date.minusHours(0)
    }
  }
}