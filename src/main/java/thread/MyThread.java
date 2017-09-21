package thread;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Created by jiazhangheng on 2017/8/31.
 */
public class MyThread extends Thread {
    @Override
    public void run() {
        for (int i = 0; i < 50000; i++) {
            if(this.isInterrupted()) {
                break;
            }
            System.out.println("i= " + (i+1));
        }
    }
}
