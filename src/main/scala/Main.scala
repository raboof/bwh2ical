import java.time._
import java.io.{InputStream, OutputStream}

import scala.language.{implicitConversions, postfixOps}

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import icalendar._
import icalendar.Properties._
import icalendar.CalendarProperties._
import icalendar.ical.Writer._
import icalendar.PropertyParameters.Language

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer
import akka.stream.alpakka.json.scaladsl.JsonReader
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString

import spray.json._
import spray.json.DefaultJsonProtocol._

trait Main {
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  import SprayJsonTime._

  def get() = {
    Source.fromFutureSource(
      Http()
        .singleRequest(HttpRequest(uri = "https://burgerweeshuis.nl/api/v2/events.json"))
        .map(_.entity.dataBytes)
    )
  }

  implicit def liftOption[T](value: T): Option[T] = Some(value)

  case class BwhEvent(
                     id: Long,
                     created_at: ZonedDateTime,
                     date: LocalDate,
                     starts_at: LocalTime,
                     title_sanitized: String,
                     description_sanitized: String,
                     venue: String,
                     event_url: String,
                     )

  implicit val bwhEventFormat = jsonFormat8(BwhEvent)

  val parse: Flow[ByteString, Event, _] =
      JsonReader.select("$.events[*]")
        .map(bytes => bytes.utf8String.parseJson.convertTo[BwhEvent])
        .map(json => Event(
          Some(Dtstamp(json.created_at)),
          Uid(s"bwh2ical-${json.id}"),
          dtstart = json.date.atTime(json.starts_at).atZone(ZoneId.of("Europe/Amsterdam")),
          description = Description(json.description_sanitized),
          location = Location(json.venue, Language("nl")),
          summary = Summary(json.title_sanitized),
          url = Url(json.event_url)
        ))

  def fetchEvents(): Future[Seq[Event]] = get().via(parse).runWith(Sink.seq[Event])

  def calendar(events: Seq[Event]): String = asIcal(Calendar(
    prodid = Prodid("-//raboof/bwh2ical//NONSGML v1.0//NL"),
    events = events.toList
  ))

}

class MainLambda extends Main {
  def handleRequest(inputStream: InputStream, outputStream: OutputStream): Unit =
    outputStream.write(calendar(Await.result(fetchEvents(), 20.seconds)).getBytes("UTF-8"))
}

object MainApp extends App with Main {
  print(calendar(Await.result(fetchEvents(), 20.seconds)))
  system.terminate()
}
