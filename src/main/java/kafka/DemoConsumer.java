package kafka;

import java.util.*;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class DemoConsumer {

	public static void main(String[] args) {
		zz();
	}
	public void cc (String[] args) {
		//args = new String[] { "zookeeper0:2181/kafka", "topic1", "group1", "consumer1" };
		if (args == null || args.length != 4) {
			System.err.println("Usage:\n\tjava -jar kafka_consumer.jar ${zookeeper_list} ${topic_name} ${group_name} ${consumer_id}");
			System.exit(1);
		}
		String zk = args[0];
		String topic = args[1];
		String groupid = args[2];
		String consumerid = args[3];
		Properties props = new Properties();
		props.put("zookeeper.connect", zk);
		props.put("group.id", groupid);
		props.put("client.id", "test");
		props.put("consumer.id", consumerid);
		props.put("auto.offset.reset", "smallest");
		props.put("auto.commit.enable", "true");
		props.put("auto.commit.interval.ms", "100");

		ConsumerConfig consumerConfig = new ConsumerConfig(props);
		ConsumerConnector consumerConnector = Consumer.createJavaConsumerConnector(consumerConfig);

		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put(topic, 1);
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumerConnector.createMessageStreams(topicCountMap);

		KafkaStream<byte[], byte[]> stream1 = consumerMap.get(topic).get(0);
		ConsumerIterator<byte[], byte[]> interator = stream1.iterator();
		while (interator.hasNext()) {
			MessageAndMetadata<byte[], byte[]> messageAndMetadata = interator.next();
			String message = String.format(
					"Topic:%s, GroupID:%s, Consumer ID:%s, PartitionID:%s, Offset:%s, Message Key:%s, Message Payload: %s",
					messageAndMetadata.topic(), groupid, consumerid, messageAndMetadata.partition(),
					messageAndMetadata.offset(), new String(messageAndMetadata.key()),
					new String(messageAndMetadata.message()));
			System.out.println(message);
		}
	}
	public static void zz() {
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092");
		props.put("group.id", "test");
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);

		consumer.subscribe(Arrays.asList("mykafka"));  //核心函数1：订阅topic
		while (true) {
			ConsumerRecords<String, String> records = consumer.poll(100); //核心函数2：long poll，一次拉取回来多个消息
			for (ConsumerRecord<String, String> record : records)
				System.out.printf("offset = %d, key = %s, value = %s", record.offset(), record.key(), record.value());
		}
	}
}
