package aservice;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by jiazhangheng on 2017/8/22.
 */
public class Task <T extends Runnable> implements Delayed {

    private final long time;
    private final T task;
    private final long n;

    private static final AtomicLong atomic = new AtomicLong(0);



    public Task(long timeout, T t) {
        this.time = System.nanoTime() + timeout;
        this.task = t;
        this.n = atomic.getAndIncrement();
    }

    /**
     * 返回与此对象相关的剩余延迟时间，以给定的时间单位表示
     */
    public long getDelay(TimeUnit unit) {
        return unit.convert(this.time - System.nanoTime(), TimeUnit.NANOSECONDS);

    }

    public int compareTo(Delayed o) {
        if (o == this) // compare zero ONLY if same object
            return 0;
        if (o instanceof Task) {
            Task x = (Task) o;
            long diff = time - x.time;
            if (diff < 0)
                return -1;
            else if (diff > 0)
                return 1;
            else if (n < x.n)
                return -1;
            else
                return 1;
        }
        long d = (getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS));
        return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
    }

    public T getTask() {
        return this.task;
    }

    @Override
    public int hashCode() {
        return task.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Task) {
            return object.hashCode() == hashCode() ? true : false;
        }
        return false;
    }

}