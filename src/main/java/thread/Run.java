package thread;

/**
 * Created by jiazhangheng on 2017/8/31.
 */
public class Run {

    public static void main(String[] args) {
        try {
            MyThread thread = new MyThread();
            thread.start();
            Thread.sleep(30);
            thread.interrupt();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
