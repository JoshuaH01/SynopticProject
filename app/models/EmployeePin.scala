package models

import play.api.libs.json.{OWrites, Reads, __}
import play.api.mvc.PathBindable


case class EmployeePin(pin: String)

object EmployeePin {
  implicit val reads: Reads[EmployeePin] = (__ \ "pin").read[String].map(EmployeePin(_))
  implicit val writes: OWrites[EmployeePin] = (__ \ "pin").write[String].contramap(_.pin)

  implicit val pathBindable: PathBindable[EmployeePin] = {
    new PathBindable[EmployeePin] {
      override def bind(key: String, value: String): Either[String, EmployeePin] = {
        if (value.matches("^[0-9]{4}$")) {
          Right(EmployeePin(value))
        } else {
          Left("Invalid employee Pin")
        }
      }

      override def unbind(key: String, value: EmployeePin): String = {
        value.pin
      }
    }
  }
}

