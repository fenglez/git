package thread;

import com.sun.corba.se.spi.orbutil.threadpool.Work;
import com.sun.xml.internal.ws.api.pipe.FiberContextSwitchInterceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by jiazhangheng on 2017/9/20.
 */
public class DefaultThreadPool<Job extends  Runnable> extends ThreadPool<Job> {
    private static final int MAX_WORKER_NUMBERS = 10;
    private static final int DEFAULT_WORKER_NUMBERS = 5;
    private static final int MIN_WORKER_NUMBERS = 1;
    private final LinkedList<Job> jobs = new LinkedList<Job>();
    private final List<Worker> workers = Collections.synchronizedList(new ArrayList<Worker>());
    private int workerNum = DEFAULT_WORKER_NUMBERS;
    private AtomicLong threadNum = new AtomicLong();
    public DefaultThreadPool() {
        init(DEFAULT_WORKER_NUMBERS);
    }
    public DefaultThreadPool(int num) {
        workerNum = num > MAX_WORKER_NUMBERS ? MAX_WORKER_NUMBERS:num<MIN_WORKER_NUMBERS?MIN_WORKER_NUMBERS:num;
        init(DEFAULT_WORKER_NUMBERS);
    }
    private void init(int defaultWorkerNumbers) {
        for (int i= 0; i< defaultWorkerNumbers; i++) {
            Worker worker = new Worker();
            workers.add(worker);
            Thread thread = new Thread(worker, "worker-" + threadNum.incrementAndGet());
            thread.start();
        }
    }
    public void exeute(Job job) {
        if(job!= null) {
            synchronized (jobs) {
                jobs.addLast(job);
                jobs.notify();
            }
        }
    }
    public void shutdown() {
        for(Worker worker : workers) {
            worker.shutdown();
        }
    }

    public void addWorkers(int num) {
        synchronized (jobs) {
            if(num + this.workerNum > MAX_WORKER_NUMBERS) {
                num = MAX_WORKER_NUMBERS - this.workerNum;
            }
            init(num);
            this.workerNum += num;
        }
    }
    public void removeWorker(int num) {
        synchronized (jobs) {
            if(num >= this.workerNum) {
                throw new IllegalArgumentException("超过线程数量");
            }
            int count = 0;
            while ( count < num) {
                Worker worker = workers.get(count);
                if( workers.remove(worker)) {
                    worker.shutdown();
                    count++;
                }
            }
            this.workerNum -= count;
        }
    }
    public int getJobSize() {
        return jobs.size();
    }

    class Worker implements Runnable {
        private volatile boolean running = true;
        public void run() {
            while(running) {
                Job job = null;
                synchronized (jobs) {
                    while(jobs.isEmpty()) {
                        try {
                            jobs.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            e.printStackTrace();
                            return;
                        }
                    }
                    job = jobs.removeFirst();
                }
                if(job != null) {
                    job.run();
                }
            }

        }

        public void shutdown() {
            running = false;
        }
    }
}
