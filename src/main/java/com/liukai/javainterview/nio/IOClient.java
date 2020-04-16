package com.liukai.javainterview.nio;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;

/**
 * IO 客户端
 *
 * @author liukai
 */
public class IOClient {

  public static void main(String[] args) {
    // 创建多个线程模拟多个客户端调用
    new Thread(() -> {
      try {
        Socket socket = new Socket("127.0.0.1", 3333);
        while (true) {

          // 向服务端写入数据
          String str = new Date() + ": hello world!";
          OutputStream os = socket.getOutputStream();
          os.write(str.getBytes());
          os.flush();
          socket.shutdownOutput();
          System.out.println("客户端输出：" + str);

          // 从服务端读取数据
          int len;
          byte[] bytes = new byte[1024];
          InputStream is = socket.getInputStream();
          while ((len = is.read(bytes)) > 0) {
            System.out.println("服务端返回：" + new String(bytes, 0, len));
          }
          // is.close();
          // socket.shutdownInput();

          System.out.println("客户端结束会话...");
          Thread.sleep(2000);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();
  }

}
