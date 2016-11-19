import java.time.{ ZonedDateTime, ZoneId }
import java.net.URL

import icalendar.ical.Writer._

import net.ruippeixotog.scalascraper.browser.JsoupBrowser

import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._

import org.scalatest._

class Test extends WordSpec with Main with Matchers {
  "The HTML scraping algorithm" should {
    "correctly find links in agenda.html" in {
      val doc = browser.parseResource("/index.html")
      val urls = links(doc)
      urls.size should be(46)
      urls(0) should be(new URL("http://burgerweeshuis.nl/agenda/4379-Russkaja"))
      urls(1) should be(new URL("http://burgerweeshuis.nl/agenda/4431-Giant-Tiger-Hooch"))
    }

    "correctly convert a details page to an event" in {
      val doc = browser.parseResource("/morphine.html")
      val event = parseEvent(new URL("http://burgerweeshuis.nl/agenda/4507-Morphine"), doc)
      event.uid.value.text should equal("bwh2ical-4507")
      event.summary.get.value.text should equal("Morphine - Vapors of")
      event.dtstart.get.value.dt should equal(ZonedDateTime.of(2016, 11, 17, 21, 0, 0, 0, ZoneId.of("Europe/Amsterdam")))
    }

    "correctly convert a details page for an event without subtitle" in {
      val doc = browser.parseResource("/4517-John-Mark-Nelson.html")
      val event = parseEvent(new URL("http://burgerweeshuis.nl/agenda/4517-John-Mark-Nelson"), doc)
      event.uid.value.text should equal("bwh2ical-4517")
      event.summary.get.value.text should equal("John Mark Nelson")
      event.dtstart.get.value.dt should equal(ZonedDateTime.of(2016, 11, 23, 21, 0, 0, 0, ZoneId.of("Europe/Amsterdam")))
    }
  }
}
