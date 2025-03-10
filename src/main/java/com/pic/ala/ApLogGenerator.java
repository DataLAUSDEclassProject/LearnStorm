/**
 * 1. Configure the topic name in ApLogAnalyzer.properties. Assume your topic is 'mytopic'.
 *
 * 2. Create a topic 'mytopic' with command line:
 *
 *     /usr/hdp/current/kafka-broker/bin/kafka-topics.sh --create --zookeeper hdp01.localdomain:2181 --replication-factor 1 --partition 1 --topic mytopic
 *
 * 3. Submit this topology:
 *
 *     storm jar target/LearnStorm-0.0.1-SNAPSHOT.jar com.pic.ala.ApLogGenerator
 *
 * 4. Monitor the topic:
 *
 *     /usr/hdp/current/kafka-broker/bin/kafka-console-consumer.sh --zookeeper hdp01.localdomain:2181 --topic mytopic --from-beginning
 *
 * 5. How to delete the topic?
 *
 *     /usr/hdp/current/kafka-broker/bin/kafka-topics.sh --zookeeper hdp01.localdomain:2181 --delete --topic mytopic
 *
 */

package com.pic.ala;

import java.util.Properties;

import org.apache.kafka.clients.producer.ProducerConfig;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.AuthorizationException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.TopologyBuilder;
import storm.kafka.bolt.KafkaBolt;
import storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import storm.kafka.bolt.selector.DefaultTopicSelector;

public class ApLogGenerator extends ApLogBaseTopology {

	private static String brokerUrl;
	private Config conf;

	public ApLogGenerator(String configFileLocation) throws Exception {
		super(configFileLocation);
		conf = new Config();
		conf.setNumWorkers(1);
	}

	private void configureRandomLogSpout(TopologyBuilder builder) {
		builder.setSpout("RandomLogSpout", new RandomLogSpout(), 3);
	}

	private void configureKafkaBolt(TopologyBuilder builder) {
		String topic = topologyConfig.getProperty("kafka.topic");
		conf.setMaxSpoutPending(20);
		Properties props = new Properties();
		props.put("metadata.broker.list", brokerUrl);
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
		props.put("serializer.class", "kafka.serializer.StringEncoder");
		props.put("request.required.acks", "1");
		props.put(ProducerConfig.CLIENT_ID_CONFIG, "storm-kafka-producer");
		conf.put(KafkaBolt.KAFKA_BROKER_PROPERTIES, props);
		KafkaBolt kafkaBolt = new KafkaBolt().withTopicSelector(new DefaultTopicSelector(topic))
										.withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper("key", "log"));
		builder.setBolt("KafkaBolt", kafkaBolt, 3).shuffleGrouping("RandomLogSpout");
	}

	private void buildAndSubmit() throws AlreadyAliveException, InvalidTopologyException, AuthorizationException {
		TopologyBuilder builder = new TopologyBuilder();
		configureRandomLogSpout(builder);
		configureKafkaBolt(builder);
		StormSubmitter.submitTopology("ApLogGenerator", conf, builder.createTopology());
	}

	public static void main(String[] args) throws Exception {

		String configFileLocation = "ApLogAnalyzer.properties";
		ApLogGenerator topology = new ApLogGenerator(configFileLocation);

		if (args.length == 0) {
			brokerUrl = topologyConfig.getProperty("metadata.broker.list");
		} else if (args.length == 1) {
			brokerUrl = args[0];
		} else {
			System.out.println("Usage: ApLogKafkaTopology [kafka broker url]");
			System.exit(1);
		}

		topology.buildAndSubmit();
	}

}
