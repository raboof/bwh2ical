import java.time.{ ZonedDateTime, ZoneId, ZoneOffset }
import java.io.{ InputStream, OutputStream }
import java.net.URL

import scala.language.{ postfixOps, implicitConversions }

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import icalendar._
import icalendar.Properties._
import icalendar.CalendarProperties._
import icalendar.ical.Writer._


import net.ruippeixotog.scalascraper.model._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser

import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._

trait Main {
  implicit def liftOption[T](value: T): Option[T] = Some(value)

  def links(doc: Document): List[URL] =
    (doc >> element("ul#event") >> elementList("li")).map(_ >> attr("href")("a")).map(new URL(_))

  def parseMonth(monthString: String): Int = monthString match {
    case "januari" => 1
    case "februari" => 2
    case "maart" => 3
    case "april" => 4
    case "mei" => 5
    case "juni" => 6
    case "juli" => 7
    case "augustus" => 8
    case "september" => 9
    case "oktober" => 10
    case "november" => 11
    case "december" => 12
  }

  def parseDate(dateString: String): ZonedDateTime = {
    val regex = ".*?(\\d+) (\\w+) (\\d+) om (\\d+):(\\d+)$".r
    dateString match {
      case regex(day, month, year, hour, minute) =>
        ZonedDateTime.of(year.toInt, parseMonth(month), day.toInt, hour.toInt, minute.toInt, 0, 0, ZoneId.of("Europe/Amsterdam"))
    }
  }

  def parseEvent(link: URL, doc: Document): Event = {
    val id = ".*/agenda/(\\d+)".r.findFirstMatchIn(link.toString).get.group(1)
    val summary = (doc >> text(".event_title")) +
      " - " + (doc >> element(".event_sub_title") >> elementList(".word")).map(text(_)).reduce(_ + " " + _)
    val description = (doc >> elementList("#sub_col_right p")).map(text(_)).reduce(_ + "\n\n" + _)
    Event(
      uid = Uid(s"bwh2ical-$id"),
      dtstart = Dtstart(parseDate(doc >> attr("content")("meta[property=\"og:title\"]"))),
      summary = Summary(summary),
      description = Description(description),
      url = link
    )
  }

  val url = new URL("http://burgerweeshuis.nl/")
  val browser = JsoupBrowser()

  def fetchDetails(url: URL): Future[Event] =
    fetchDocument(url).map(doc => parseEvent(url, doc))

  def fetchDocument(url: URL): Future[Document] = Future {
    browser.get(url.toString)
  }

  def fetchIndex(url: URL): Future[List[Event]] =
    fetchDocument(url)
      .map(links)
      .flatMap(links => Future.sequence(links.map(fetchDetails)))

  def events() = Await.result(fetchIndex(url), 20 seconds)

  def fetchCalendar(): String = asIcal(Calendar(
    prodid = Prodid("-//raboof/bwh2ical//NONSGML v1.0//NL"),
    events = events()
  ))
}

class MainLambda extends Main {
  def handleRequest(inputStream: InputStream, outputStream: OutputStream): Unit =
    outputStream.write(fetchCalendar().getBytes("UTF-8"));
}

object MainApp extends App with Main {
  print(fetchCalendar())
}
