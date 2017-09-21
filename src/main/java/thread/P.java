package thread;


/**
 * Created by jiazhangheng on 2017/8/31.
 */
public class P {
    private String lock;
    public P(String lock) {
        this.lock = lock;
    }
    public void setValue() {
        try {
            synchronized (lock) {
                if (!ValueObject.value.equals("")) {
                    lock.wait();
                }
                String valuex = System.currentTimeMillis() + "_" + System.nanoTime();
                ValueObject.value = valuex;
                lock.notify();
            }
        } catch (InterruptedException w) {
            w.printStackTrace();
        }
    }
}
