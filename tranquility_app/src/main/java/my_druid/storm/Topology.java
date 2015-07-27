/**
 * Topology.java
 *
 * Copyright: (c) SAEKI Yoshiyasu
 * License  : MIT-style license
 *
 */
package my_druid.storm;

import java.io.IOException;
import java.util.Properties;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;

import com.metamx.tranquility.storm.BeamBolt;

public class Topology {

    /**
     * メイン.
     * @param args [0] == 設定ファイルパス
     * @throws IOException ファイルがない
     */
    public static void main(final String[] args) throws IOException {
        TopologyConfig config = readConfigFile(args[0]);
        StormTopology topology = buildTopology(config);
        try {
            if (!config.isLocal()) {
                submitTopology(topology, config);
            } else {
                submitTopologyLocal(topology, config);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * トポロジを作成する.
     * @param config 設定
     * @return トポロジ
     */
    public static StormTopology buildTopology(final TopologyConfig config) {
        TopologyBuilder builder = new TopologyBuilder();
        Properties props = new Properties();
        props.put("group.id", config.getConsumerGroup());
        props.put("zookeeper.connect",
                  config.getConsumerZk() + config.getConsumerZkRoot());
        props.put("zookeeper.session.timeout.ms", "12000");
        props.put("zookeeper.connection.timeout.ms", "12000");
        props.put("zookeeper.sync.time.ms", "4000");
        builder.setSpout("kafka_spout",
                         new KafkaSpout(config.getTopic(), props),
                         config.getPartitions());
        builder.setBolt("parser", new ParserBolt(), config.getPartitions())
            .localOrShuffleGrouping("kafka_spout");
        builder.setBolt("druid_beam",
                        new BeamBolt(new DBeamFactory(),
                                     config.getBeamBatchSize()),
                        config.getPartitions())
            .localOrShuffleGrouping("parser");
        return builder.createTopology();
    }

    /**
     * クラスタモードでトポロジをデプロイ.
     * @param topology トポロジ
     * @param config 設定
     */
    private static void submitTopology(final StormTopology topology,
                                       final TopologyConfig config) {
        Config stormconf = new Config();
        stormconf.setDebug(false);
        stormconf.setMaxSpoutPending(100);
        stormconf.put("druid.zookeepers", config.getDruidZk());
        try {
            stormconf.setNumWorkers(config.getWorkers());
            StormSubmitter.submitTopology(config.getTopologyName(),
                                          stormconf, topology);
        } catch (AlreadyAliveException aae) {
            aae.printStackTrace();
        } catch (InvalidTopologyException ite) {
            ite.printStackTrace();
        }
    }

    /**
     * ローカルモードでトポロジをデプロイ.
     * @param topology トポロジ
     * @param config 設定
     */
    private static void submitTopologyLocal(final StormTopology topology,
                                            final TopologyConfig config) {
        LocalCluster cluster = new LocalCluster();
        Config stormconf = new Config();
        stormconf.setDebug(true);
        stormconf.setMaxSpoutPending(10);
        stormconf.put("druid.zookeepers", config.getDruidZk());
        cluster.submitTopology("test_topology", stormconf, topology);
        Utils.sleep(60000);
        cluster.killTopology("test_topology");
        cluster.shutdown();
    }
}

