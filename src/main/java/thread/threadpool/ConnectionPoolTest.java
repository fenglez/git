package thread.threadpool;

import thread.threadpool.ConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jiazhangheng on 2017/9/19.
 */
public class ConnectionPoolTest {
    static ConnectionPool pool = new ConnectionPool(10);
    static CountDownLatch start = new CountDownLatch(1);
    static CountDownLatch end;
    public static void main(String[] args) throws Exception {
        int threadCount = 50;
        end = new CountDownLatch(threadCount);
        int count = 20;
        AtomicInteger got = new AtomicInteger();
        AtomicInteger notgot = new AtomicInteger();
        for (int i = 0;i<threadCount;i++) {
            Thread thread = new Thread(new ConnectionRunner(count, got, notgot), "connn");
            thread.start();
        }
        start.countDown();
        end.await();
        System.out.println("获取次数:" + (threadCount * count));
        System.out.println("got connection:" + got);
        System.out.println("not got connection:" + notgot);
    }

    static class ConnectionRunner implements Runnable {
        int count;
        AtomicInteger got;
        AtomicInteger notGot;
        public ConnectionRunner(int count, AtomicInteger got, AtomicInteger notgot) {
            this.count = count;
            this.got = got;
            this.notGot = notgot;
        }
        public void run() {
            try {
                start.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (count > 0) {
                try {
                    Connection connection = pool.fetchConnection(1000);
                    if (connection != null) {
                        try {
                            connection.createStatement();
                            connection.commit();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            pool.releaseConnection(connection);
                            got.incrementAndGet();
                        }

                    } else {
                        notGot.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    count--;
                }

            }
            end.countDown();
        }
    }
}
