package thread;

/**
 * Created by jiazhangheng on 2017/8/31.
 */
public class C {
    private String lock;
    public C(String lock) {
        this.lock = lock;
    }
    public void setValue() {
        try {
            synchronized (lock) {
                if (!ValueObject.value.equals("")) {
                    lock.wait();
                }
                System.out.println("get的值" + ValueObject.value);
                ValueObject.value = "";
                lock.notify();
            }
        } catch (InterruptedException w) {
            w.printStackTrace();
        }
    }
}
