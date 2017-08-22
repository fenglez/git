package kafka;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Created by jiazhangheng on 2017/8/10.
 */
class DemoCallBack implements Callback {
    private final long startTime;
    private final String key;
    private final String message;

    public DemoCallBack(long startTime, String key, String message) {
        this.startTime = startTime;
        this.key = key;
        this.message = message;
    }

    //并实现接口中的函数：
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        //这里的startTime是发送这条消息时，生成回调函数时传入的消息发送的开始时间，
        //计算出来了这次发送这条消息共花的时间
        long elapsedTime = System.currentTimeMillis() - startTime;
        if (metadata != null) {
            //如果metadata信息不为空，表示消息添加成功，可以得到当前添加成功的消息的offset.
            System.out.println(
                    "message(" + key + ", " + message + ") sent to partition("
                            + metadata.partition() +
                            "), " +
                            "offset(" + metadata.offset() + ") in " + elapsedTime + " ms");
        } else {
            //这种情况下，表示exception有值，也就是添加消息失败了，可以直接打印这个失败的消息的内容。
            exception.printStackTrace();
        }
    }

}