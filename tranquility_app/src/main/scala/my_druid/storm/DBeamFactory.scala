/**
 * DBeamFactory.scala
 *
 * Copyright: (c) SAEKI Yoshiyasu
 * License  : MIT-style license
 *
 */
package my_druid.storm

import com.metamx.common.Granularity
import com.metamx.common.scala.net.curator.Disco
import com.metamx.common.scala.net.curator.DiscoConfig
import com.metamx.tranquility.beam.Beam
import com.metamx.tranquility.beam.ClusteredBeamTuning
import com.metamx.tranquility.druid.DruidBeams
import com.metamx.tranquility.druid.DruidDimensions
import com.metamx.tranquility.druid.DruidLocation
import com.metamx.tranquility.druid.DruidRollup
import com.metamx.tranquility.druid.DruidTuning
import com.metamx.tranquility.storm.BeamFactory
import com.metamx.tranquility.typeclass.Timestamper
import com.metamx.tranquility.finagle.FinagleRegistry
import com.metamx.tranquility.finagle.FinagleRegistryConfig
import io.druid.data.input.impl.TimestampSpec
import io.druid.granularity.QueryGranularity
import io.druid.query.aggregation.AggregatorFactory
import io.druid.query.aggregation.CountAggregatorFactory
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import com.google.common.collect.ImmutableList
import org.joda.time.DateTime
import org.joda.time.Period
import backtype.storm.task.IMetricsContext
import java.util.Map
import java.util.List
import org.scala_tools.time.Imports._

class DBeamFactory extends BeamFactory[Map[String, Any]] {
  def makeBeam(conf: java.util.Map[_, _], metrics: IMetricsContext) = {
    val curator = CuratorFrameworkFactory.newClient(
      conf.get("druid.zookeepers").asInstanceOf[String],
      new ExponentialBackoffRetry(100, 5))
    curator.start()

    val indexService = "overlord"
    val firehosePattern = "druid:firehose:%s"
    val discoveryPath = "/druid/discovery"
    val dataSource = "test_source"
    val aggregators = Seq(new CountAggregatorFactory("events"))

    DruidBeams
      .builder((eventMap: Map[String, Any]) => new DateTime(eventMap.get("timestamp")))
      .curator(curator)
      .discoveryPath(discoveryPath)
      .location(DruidLocation(indexService, firehosePattern, dataSource))
      .timestampSpec(new TimestampSpec("timestamp", "millis"))
      .rollup(DruidRollup(DruidDimensions.schemaless(),
                          aggregators,
                          QueryGranularity.MINUTE))
      .tuning(ClusteredBeamTuning(segmentGranularity = Granularity.MINUTE,
                                  windowPeriod = new Period("PT1M"),
                                  partitions = 2,
                                  replicants = 1))
      .druidTuning(DruidTuning.create(5000000,
                                      new Period("PT1M"),
                                      0))
      .buildBeam()
  }
}

