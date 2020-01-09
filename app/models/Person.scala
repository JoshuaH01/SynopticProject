
package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Person(
                    _id: PersonId,
                    name: String,
                    email: String,
                    mobileNumber: String,
                  )

object Person {

  implicit val reads: Reads[Person] = (
    __.read[PersonId] and
      (__ \ "name").read[String] and
      (__ \ "email").read[String] and
      (__ \ "mobileNumber").read[String]
    ) (Person.apply _)

  implicit val writes: OWrites[Person] = (
    __.write[PersonId] and
      (__ \ "name").write[String] and
      (__ \ "email").write[String] and
      (__ \ "mobileNumber").write[String]
    ) (unlift(Person.unapply))
}