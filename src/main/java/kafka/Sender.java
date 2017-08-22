package kafka;

import org.apache.kafka.clients.ClientRequest;
import org.apache.kafka.clients.KafkaClient;
import org.apache.kafka.clients.Metadata;
import org.apache.kafka.clients.producer.internals.RecordAccumulator;
import org.apache.kafka.clients.producer.internals.RecordBatch;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.metrics.Metrics;
import org.apache.kafka.common.utils.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by jiazhangheng on 2017/8/10.
 */
public class Sender {

    private static final Logger log = LoggerFactory.getLogger(org.apache.kafka.clients.producer.internals.Sender.class);
    private final KafkaClient client;
    private final RecordAccumulator accumulator;
    private final Metadata metadata;
    private final boolean guaranteeMessageOrder;
    private final int maxRequestSize;
    private final short acks;
    private final int retries;
    private final Time time;
    private volatile boolean running;
    private volatile boolean forceClose;
    // private final org.apache.kafka.clients.producer.internals.Sender.SenderMetrics sensors;
    private final int requestTimeout;

    public Sender(KafkaClient client, Metadata metadata, RecordAccumulator accumulator, boolean guaranteeMessageOrder, int maxRequestSize, short acks, int retries, Metrics metrics, Time time, int requestTimeout) {
        this.client = client;
        this.accumulator = accumulator;
        this.metadata = metadata;
        this.guaranteeMessageOrder = guaranteeMessageOrder;
        this.maxRequestSize = maxRequestSize;
        this.running = true;
        this.acks = acks;
        this.retries = retries;
        this.time = time;
        ///this.sensors = new org.apache.kafka.clients.producer.internals.Sender.SenderMetrics(metrics);
        this.requestTimeout = requestTimeout;
    }
    /**
     * The main run loop for the sender thread
     */
    public void run() {
        log.debug("Starting Kafka producer I/O thread.");
        //KafkaProducer实例生成时，KafkaThread线程启动后，会执行Sender实例中的run函数，
        while (running) {
            //如果producer没有执行shutdown操作前，run函数会一直在这个地方进行执行，不断的执行run函数传入当前的执行时的系统时间。
            try {
                run(time.milliseconds());
            } catch (Exception e) {
                log.error("Uncaught error in kafka producer I/O thread: ", e);
            }
        }
        // 如果流程执行到这里，说明produce已经执行了shutdown操作，准备执行停止producer的操作。
        log.debug("Beginning shutdown of Kafka producer I/O thread, sending remaining records.");

                // okay we stopped accepting requests but there may still be
                // requests in the accumulator or waiting for acknowledgment,
                // wait until these are completed.
        while (!forceClose && (this.accumulator.hasUnsent() ||
                this.client.inFlightRequestCount() > 0)) {
            // 如果当前的accumulator的缓冲区还有数据没有被处理，同时networkClient中还有正在进行的请求,迭代执行run函数，直到数据被全部发送完成。
            try {
                run(time.milliseconds());
            } catch (Exception e) {
                log.error("Uncaught error in kafka producer I/O thread: ", e);
            }
        }
        if (forceClose) {
            this.accumulator.abortIncompleteBatches();
        }
        // 关闭网络连接。
        try {
            this.client.close();
        } catch (Exception e) {
            log.error("Failed to close network client", e);
        }

        log.debug("Shutdown of Kafka producer I/O thread has completed.");
    }

    // 执行缓冲区数据的发送操作的函数：

    /**
     * Run a single iteration of sending
     *
     * @param now
     *            The current POSIX time in milliseconds
     */
    public void run(long now) {
        // 得到当前cluster中所有的broker节点的连接信息。
//        Cluster cluster = metadata.fetch();
//
//        // 这里从缓冲区中的所有的partition的batch中进行计算，取出已经准备好的需要进行发送的broker的节点集合，具体流程：
//                 //  1,对缓冲区中batchs集合进行迭代，取出每个partition对应的双端队列(存储数据缓存的batch),
//                // 2,如果partition在cluster对应的partitionsByTopicPartition集合中存在，表示这个topic的metadata已经被加载过来，得到这个partition的leader,
//                // 3,如果partition的leader不存在，设置这个函数返回ReadyCheckResult类型的unknownLeadersExist值为true.
//                 // 4,如果迭代的partition的leader存在，取出这个partition的队列中的第一个batch，如果这个batch存在，表示有缓存的数据，
////        4,1检查这个batch是否已经被提交过，重试次数大于0,
////                同时上一次重试的时间已经大于了retry.backoff.ms(默认100ms)配置的等待时间，
////        把这个partition的leader添加到返回的ReadyCheckResult实例中的readyNodes集合中。
////         （readyNodes是一个set集合）
////        4,2如果这个partition对应的队列中已经缓存有超过一个以上的batch，
////        或者说有batch的缓存大小已经达到了batchSize的配置大小时,
////                把这个leader添加到readyNodes中。
////        4,3如果这个partition的队列中有batch已经达到了linger.ms（默认值0）配置的等待时间，
////        把这个leader添加到readyNodes中。
//        // 5,这个返回的ReadyCheckResult实例中，属性nextReadyCheckDelayMs的值，表示要delay到的下一次时间，也就是下一次执行的wait时间，如果当前的所有的batch中没有超过等待时间时（retry.backoff.ms/linger.ms），也就是当前执行时间与等待时间的差值。
//        // get the list of partitions with data ready to send
//        RecordAccumulator.ReadyCheckResult result = this.accumulator.ready(cluster, now);
//
//        // 如果上面的执行返回的结果中unknownLeadersExist属性值为true，表示topic的metadata还没有被加载过来（这个情况一般不会发生），标记metadata需要被更新。
//        if (result.unknownLeadersExist) {
//            this.metadata.requestUpdate();
//        }
//
////        对返回的结果集中readyNodes集合中准备好的节点进行迭代，这个while的迭代中主要执行如下的流程：
////        1,通过NetworkClient检查这个node是否已经被连接，同时metadata还没有达到需要更新的时间，同时连接队列中个数数小于max.in.flight.requests.per.connection配置的连接个数。那么这个node会被保留，
////        2,如果当前迭代的node的连接已经超时，或者metadata需要被更新，或者node对应的broker还没有被创建连接，移出这个node.
//                // remove any nodes we aren't ready to send to
//        Iterator<Node> iter = result.readyNodes.iterator();
//        long notReadyTimeout = Long.MAX_VALUE;
//        while (iter.hasNext()) {
//            Node node = iter.next();
//            if (!this.client.ready(node, now)) {
//                iter.remove();
//                //如果connection是已经被关闭掉的连接，this.client.connectionDelay(node, now)返回的timeout是reconnect.backoff.ms(50ms)配置的值。
//                notReadyTimeout = Math.min(notReadyTimeout,
//                        this.client.connectionDelay(node, now));
//            }
//        }
//
////        流程执行到这里时，result中的readyNodes集合中包含的是已经与broker创建有连接的node的信息。
////        这里根据可以发起连接的broker的nodes集合，迭代每个node中的所有的partition的队列，
////        取出这个队列中的第一个recordBatch（如果这个batch已经发送失败过一次，同时还没到重试的时间间隔，跳过这个batch）,关闭这个batch（表示这个batch不能在写入）同时把这个batch添加到要返回的map集合中，这个迭代的过程直到找完所有的node中对应的partition中队列的第一个元素,
////                或者达到max.request.size配置的最大的请求的消息字节数的大小为结束。
//        // create produce requests
//        Map<Integer, List<RecordBatch>> batches = this.accumulator.drain(cluster,
//                result.readyNodes,
//                this.maxRequestSize,
//                now);
//        // 这里根据request.timeout.ms(默认30秒)配置的请求超时时间，得到缓冲区中所有请求超时的batch的集合（通过batch的最后一次写入消息的时间来判断是否达到了超时时间），如果发现batch已经起时，从缓冲区中移出这个batch,并回收这个batch对应的buffer.
//
//
//        List<RecordBatch> expiredBatches = this.accumulator.abortExpiredBatches(this.requestTimeout, now);
//        Iterator i$ = expiredBatches.iterator();
//
//        while(i$.hasNext()) {
//            RecordBatch expiredBatch = (RecordBatch)i$.next();
//            this.sensors.recordErrors(expiredBatch.topicPartition.topic(), expiredBatch.recordCount);
//        }
//
//
//        //检查是否需要更新metadata,如果需要，重新向broker发送metadata的请求，并更新metadata.接收请求的响应信息，并调用对应的callback函数。
//        sensors.updateProduceRequestMetrics(batches);
//
//        // 根据每个broker对应的partition的batch的消息集合，生成对应的ProduceRequest请求，
//        // 这个请求每一个broker生成一个请求，这个请求中包含了这个broker中所有的partition的buffer的集合。
//        List<ClientRequest> requests = createProduceRequests(batches, now);
//
//        //这里计算出下一次执行需要的等待间隔，根据retry.backoff.ms/linger.ms配置的时间，如果说这次需要进行提交数据到指定的broker的readyNodes的集合大于0,设置这个间隔时间为0.
//        long pollTimeout = Math.min(result.nextReadyCheckDelayMs, notReadyTimeout);
//        if (result.readyNodes.size() > 0) {
//            log.trace("Nodes with data ready to send: {}", result.readyNodes);
//            log.trace("Created {} produce requests: {}", requests.size(), requests);
//            pollTimeout = 0;
//        }
//
//        //迭代每一个broker的Produce的请求，通过NetworkClient向每一个broker发送对应的请求。
//        this.sendProduceRequests(batches, now); //循环调用
//        //检查连接中是否有超过指定的时间connections.max.idle.ms（默认9分钟）没有活动的broker连接，如果有，关闭这个连接。
//        this.client.poll(pollTimeout, now);
//    }
    }
}
