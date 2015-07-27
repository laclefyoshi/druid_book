package my_druid;

import com.metamx.common.Granularity
import com.metamx.tranquility.beam.Beam
import com.metamx.tranquility.beam.ClusteredBeamTuning
import com.metamx.tranquility.druid.DruidBeams
import com.metamx.tranquility.druid.DruidLocation
import com.metamx.tranquility.druid.DruidRollup
import com.metamx.tranquility.druid.DruidTuning
import com.metamx.tranquility.druid.DruidDimensions
import com.metamx.tranquility.druid.SpecificDruidDimensions
import com.metamx.tranquility.storm.BeamFactory
import com.metamx.tranquility.typeclass.Timestamper
import io.druid.data.input.impl.TimestampSpec
import io.druid.granularity.QueryGranularity
import io.druid.query.aggregation.AggregatorFactory
import io.druid.query.aggregation.CountAggregatorFactory
import io.druid.query.aggregation.DoubleSumAggregatorFactory
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import backtype.storm.task.IMetricsContext
import org.joda.time.DateTime
import org.joda.time.Period
import com.twitter.util.Future
import com.twitter.util.Await
import org.json.simple.JSONValue
import java.util.Map
import java.util.List


object App {
  def main(args: Array[String]): Unit = {
    val zookeepers = args(0)
    val curator = CuratorFrameworkFactory
      .newClient(zookeepers, new ExponentialBackoffRetry(100, 5))
    curator.start()

    val indexService = "overlord"
    val firehosePattern = "druid:firehose:%s"
    val discoveryPath = "/druid/discovery"
    val dataSource = "test_source"
    val dimensions = IndexedSeq("id", "domain")
    val aggregators = Seq(new CountAggregatorFactory("count"),
                          new DoubleSumAggregatorFactory("added", "value"))
    val timestamper = (eventMap: Map[String, Any]) => new DateTime(eventMap.get("timestamp"))
    val druidService = DruidBeams
      .builder(timestamper)
      .curator(curator)
      .discoveryPath(discoveryPath)
      .location(DruidLocation(indexService, firehosePattern, dataSource))
      .timestampSpec(new TimestampSpec("timestamp", "millis"))
      .rollup(DruidRollup(SpecificDruidDimensions(dimensions),
                          aggregators,
                          QueryGranularity.MINUTE))
      .tuning(ClusteredBeamTuning(
        segmentGranularity = Granularity.MINUTE,
        windowPeriod = new Period("PT5M"),
        partitions = 2,
        replicants = 1))
      .druidTuning(DruidTuning.create(5000000, new Period("PT1M"), 0))
      .buildService()

    val listOfEvents =
      Seq("{\"timestamp\": 1431067286094, \"domain\": \"jp\", \"id\": \"ebeb7f24-df35-4633-9204-194eadca2142\", \"value\": 17}",
"{\"timestamp\": 1431067316094, \"domain\": \"zw\", \"id\": \"b4976165-cf9d-4f31-904e-c3e8fc1c4ab0\", \"value\": 58}",
"{\"timestamp\": 1431067346094, \"domain\": \"hu\", \"id\": \"9afb1bc1-b47d-47e5-9190-ee4722ad342d\", \"value\": 12}",
"{\"timestamp\": 1431067376094, \"domain\": \"cz\", \"id\": \"f055c2a4-0bc6-4ab6-a732-b2b2c97671af\", \"value\": 66}",
"{\"timestamp\": 1431067406094, \"domain\": \"qa\", \"id\": \"034fb62f-98e6-4773-b878-b8597ad56a73\", \"value\": 15}",
"{\"timestamp\": 1431067436094, \"domain\": \"cz\", \"id\": \"9fd517f1-d9c5-4e18-8dae-69aa8e533c97\", \"value\": 88}",
"{\"timestamp\": 1431067466094, \"domain\": \"kz\", \"id\": \"034ac824-ae77-4721-a333-75ab890e0b57\", \"value\": 86}",
"{\"timestamp\": 1431067496094, \"domain\": \"dz\", \"id\": \"d8b9a8fa-7738-4ae1-9ef5-33ffbd476e1f\", \"value\": 48}",
"{\"timestamp\": 1431067526094, \"domain\": \"cz\", \"id\": \"ee723849-3b7f-49c1-88fa-9132acf7a545\", \"value\": 62}",
"{\"timestamp\": 1431067556094, \"domain\": \"jp\", \"id\": \"e0cb2298-840f-4399-8ac2-1d5de974b3c9\", \"value\": 40}")
      .map(JSONValue.parse(_).asInstanceOf[Map[String, Any]])
    val numSentFuture: Future[Int] = druidService(listOfEvents)
    print(numSentFuture)
    val numSent = Await.result(numSentFuture)
    Thread.sleep(5 * 60)
    print(numSent)
    Await.result(druidService.close())
    curator.close()
  }
}
