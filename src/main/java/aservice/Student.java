package aservice;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**

 * @author 小e
 *
 * 2010-4-30 下午11:14:25
 */
public class Student implements Runnable,Delayed {

    private String name;
    private long submitTime;//交卷时间
    private long workTime;//考试时间
    public Student() {
        // TODO Auto-generated constructor stub
    }
    public Student(String name, long submitTime) {
        super();
        this.name = name;
        workTime = submitTime;
        //都转为转为ns
        this.submitTime = TimeUnit.NANOSECONDS.convert(submitTime, TimeUnit.MILLISECONDS) + System.nanoTime();
    }

    public void run() {
        System.out.println(name + " 交卷,用时" + workTime/100 + "分钟");
    }


    public long getDelay(TimeUnit unit) {
        return unit.convert(submitTime - System.nanoTime(), unit.NANOSECONDS);
    }

    public int compareTo(Delayed o) {
        Student that = (Student) o;
        return submitTime > that.submitTime?1:(submitTime < that.submitTime ? -1 : 0);
    }
    public static class EndExam extends Student {
        private ExecutorService exec;
        public EndExam(int submitTime,ExecutorService exec) {
            super(null,submitTime);
            this.exec = exec;
        }
        @Override
        public void run() {
            exec.shutdownNow();
        }
    }

   public class Teacher implements Runnable {
        private DelayQueue<Student> students;
        private ExecutorService exec;

        public Teacher(DelayQueue<Student> students,ExecutorService exec) {
            super();
            this.students = students;
            this.exec = exec;
        }


        public void run() {
            try {
                System.out.println("考试开始……");
                while (!Thread.interrupted()) {
                    students.take().run();
                }
                System.out.println("考试结束……");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }


}
