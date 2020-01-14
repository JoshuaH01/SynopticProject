
package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class BowsEmployee(
                   employeeId: EmployeeId,
                    name: String,
                    email: String,
                    mobileNumber: String,
                    pin: EmployeePin,
                    balance: Int
                  )

object BowsEmployee {

  implicit val reads: Reads[BowsEmployee] = (
    __.read[EmployeeId] and
      (__ \ "name").read[String] and
      (__ \ "email").read[String] and
      (__ \ "mobileNumber").read[String] and
      (__ \ "pin").read[EmployeePin]and
      (__ \ "balance").read[Int]
    ) (BowsEmployee.apply _)

  implicit val writes: OWrites[BowsEmployee] = (
    __.write[EmployeeId] and
      (__ \ "name").write[String] and
      (__ \ "email").write[String] and
      (__ \ "mobileNumber").write[String]and
      (__ \ "pin").write[EmployeePin]and
      (__ \ "balance").write[Int]
    ) (unlift(BowsEmployee.unapply))
}