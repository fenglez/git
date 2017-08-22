package kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.concurrent.ExecutionException;


public class ProducerDemo {

  static private final String TOPIC = "topic1";
  static private final String BROKER_LIST = "localHost:9092";

  public static void sendOne(boolean isAsync, String messageNo, String messageStr) throws InterruptedException {
    Properties props = new Properties();
    props.put("bootstrap.servers", BROKER_LIST);
    props.put("client.id", "DemoProducer");
    props.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
    long startTime = System.currentTimeMillis();
    if (isAsync) {
      //异步处理，这个过程需要定义一个回调函数来监听发送的消息的响应结果
      // Send asynchronously
      producer.send(new ProducerRecord<String, String>(TOPIC, messageNo, messageStr),
      /*异步处理,回调函数*/
              new DemoCallBack(startTime, messageNo, messageStr));
    } else {
      // 同步处理，发送完成后，等待发送的响应结果。Send synchronously
      try {
        producer.send(new ProducerRecord<String, String>(TOPIC, messageNo, messageStr)).get();
        System.out.println("Sent message: (" + messageNo + ", " + messageStr + ")");
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }
  }
}
