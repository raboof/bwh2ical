import java.time.{ZoneId, ZonedDateTime}
import java.net.URL

import akka.stream.scaladsl.{Sink, Source, StreamConverters}
import icalendar.ical.Writer._
import org.scalatest._

import scala.concurrent.duration._

import scala.concurrent.Await

class Test extends WordSpec with Main with Matchers with BeforeAndAfterAll {
  "The JSON scraping algorithm" should {
    "parse the example json" in {
      val events = Await.result(StreamConverters.fromInputStream(() => classOf[Test].getResourceAsStream("/events.json"))
        .via(parse)
        .runWith(Sink.seq), 10.seconds)
      println(asIcal(events(0)))
      events.length should be(40)
    }
  }

  override protected def afterAll() = {
    system.terminate()
    super.afterAll()
  }

}
