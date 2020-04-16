package com.liukai.javainterview.nio;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * BIO 服务端
 */
public class BIOServer {

  public static void main(String[] args) throws IOException {

    // 服务端处理客户端连接请求
    ServerSocket serverSocket = new ServerSocket(3333);

    // 接收客户端的请求之后，为每一个客户端创建一个新的线程进行链路处理
    new Thread(() -> doAcceptorClientReq(serverSocket)).start();

    System.out.println("服务端已经启动！");

  }

  private static void doAcceptorClientReq(ServerSocket serverSocket) {
    while (true) {
      try {
        // 阻塞方法获取新的连接
        Socket socket = serverSocket.accept();

        new Thread(() -> {
          int len;
          byte[] bytes = new byte[1024];
          try {
            // 按照字节流方式读取数据
            InputStream is = socket.getInputStream();
            while ((len = is.read(bytes)) > 0) {
              System.out.println("收到客户端数据：" + new String(bytes, 0, len));
            }
            socket.shutdownInput();
            socket.getOutputStream().write("服务器端：你好！".getBytes());
            socket.shutdownOutput();
          } catch (IOException ignored) {
          }

        }).start();

      } catch (Exception ignored) {

      }
    }
  }

}
