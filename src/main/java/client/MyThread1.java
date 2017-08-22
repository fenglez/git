package client;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiazhangheng on 2017/8/1.
 */
public class MyThread1 extends Thread {

    public static void main(String[] args) {
        client();
    }
    public static  void iomethod() {
        InputStream in;
        try {
            in = new BufferedInputStream(
                    new FileInputStream("/Users/jiazhangheng/data/appendonly.aof"));
            byte[] buf = new byte[1024];
            int bytesRead = in.read(buf);
            while(bytesRead != -1) {
                for(int i=0;i<bytesRead;i++)
                    System.out.print((char)buf[i]);
                bytesRead = in.read(buf);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void niomethod() {
        RandomAccessFile afile;
        try {
            afile = new RandomAccessFile(
                    "/Users/jiazhangheng/data/appendonly.aof", "rw");
            FileChannel fileChannel = afile.getChannel();
            ByteBuffer buf = ByteBuffer.allocate(1024);
            int bytesRead = fileChannel.read(buf);
            while(bytesRead != -1) {
                buf.flip();
                while (buf.hasRemaining()) {
                    System.out.print((char)buf.get());
                }
                buf.compact();
                bytesRead = fileChannel.read(buf);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void client() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        SocketChannel socketChannel = null;
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress("127.0.0.1",8080));

            if (socketChannel.finishConnect()) {
                int i=0;
                while (true) {
                    TimeUnit.SECONDS.sleep(1);
                    String info = "I'm "+i+++"-th information from client";
                    buffer.clear();
                    buffer.put(info.getBytes());
                    buffer.flip();
                    while(buffer.hasRemaining()) {
                        System.out.println(buffer);
                        socketChannel.write(buffer);
                    }
                }
            }
        } catch (IOException  e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally{
            try {
                if(socketChannel!=null){
                    socketChannel.close();
                }
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }


}
