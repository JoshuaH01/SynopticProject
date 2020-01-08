package models

import play.api.libs.json._
import play.api.mvc.PathBindable


case class PersonId(_id: String)

object PersonId {
  implicit val reads: Reads[PersonId] = (__ \ "_id").read[String].map(PersonId(_))
  implicit val writes: OWrites[PersonId] = (__ \ "_id").write[String].contramap(_._id)

  implicit val pathBindable: PathBindable[PersonId] = {
    new PathBindable[Card] {
      override def bind(key: String, value: String): Either[String, PersonId] = {
        if (value.matches("^[a-zA-Z0-9]+$")) {
          Right(Card(value))
        } else {
          Left("Invalid Person Id")
        }
      }

      override def unbind(key: String, value: Card): String = {
        value._id
      }
    }
  }
}


