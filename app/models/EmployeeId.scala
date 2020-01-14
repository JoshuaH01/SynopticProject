package models

import play.api.libs.json._
import play.api.mvc.PathBindable


case class EmployeeId(id: String)

object EmployeeId {
  implicit val reads: Reads[EmployeeId] = (__ \ "id").read[String].map(EmployeeId(_))
  implicit val writes: OWrites[EmployeeId] = (__ \ "id").write[String].contramap(_.id)

  implicit val pathBindable: PathBindable[EmployeeId] = {
    new PathBindable[EmployeeId] {
      override def bind(key: String, value: String): Either[String, EmployeeId] = {
        if (value.matches("^[A-Z]{1,16}$")) {
          Right(EmployeeId(value))
        } else {
          Left("Invalid Employee Id")
        }
      }

      override def unbind(key: String, value: EmployeeId): String = {
        value.id
      }
    }
  }
}


