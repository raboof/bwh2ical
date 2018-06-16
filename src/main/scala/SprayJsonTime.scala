import java.time.{LocalDate, LocalTime, ZonedDateTime}

import spray.json.DefaultJsonProtocol.lift
import spray.json.{JsString, JsValue, JsonReader}

object SprayJsonTime {
  implicit val zonedDateTimeFormat = lift(new JsonReader[ZonedDateTime] {
    override def read(json: JsValue): ZonedDateTime = json match {
      case JsString(str) => ZonedDateTime.parse(str)
    }
  })
  implicit val localDateFormat = lift(new JsonReader[LocalDate] {
    override def read(json: JsValue): LocalDate = json match {
      case JsString(str) => LocalDate.parse(str)
    }
  })
  implicit val localTimeFormat = lift(new JsonReader[LocalTime] {
    override def read(json: JsValue): LocalTime = json match {
      case JsString(str) => LocalTime.parse(str)
    }
  })
}
