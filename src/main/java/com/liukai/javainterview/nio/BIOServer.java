package com.liukai.javainterview.nio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * BIO 服务端
 */
public class BIOServer {

  public static void main(String[] args) throws IOException {

    // 服务端处理客户端连接请求
    ServerSocket serverSocket = new ServerSocket(3333);
    // 接收客户端的请求之后，为每一个客户端创建一个新的线程进行链路处理
    doAcceptorClientReq(serverSocket);

  }

  private static void doAcceptorClientReq(ServerSocket serverSocket) {
    try {

      // 固定数量的线程池
      ExecutorService executorService = Executors.newFixedThreadPool(100);

      while (true) {
        // 阻塞方法获取新的连接
        Socket socket = serverSocket.accept();
        // 每一个新的连接都创建一个线程，负责读取数据
        Runnable runnable = () -> {
          try {
            readWriteReuseSocket(socket);
          } catch (IOException e) {
            e.printStackTrace();
          }
        };
        executorService.submit(runnable);

      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 通过指定长度告知对方已发送完命令
   *
   * @param socket
   * @throws IOException
   */
  private static void readWriteReuseSocket(Socket socket) throws IOException {
    // 获取输入流，并建立缓冲区进行读取
    InputStream is = socket.getInputStream();
    byte[] bytes;
    // 因为可以复用 socket且能判断长度，所以可以一个 socket 用到底
    while (true) {
      // 首先读取两个字节表示的长度
      int first = is.read();
      // 如果读取的值为-1，则说已经到达流的末尾，socket 已经被关闭了，此时将不能再去读取
      if (first == -1) {
        break;
      }
      // 第二个字节
      int second = is.read();
      // 用位运算将两个字节拼起来成为真正的长度
      int len = (first << 8) + second;
      // 构建指定大小的 byte 数组
      bytes = new byte[len];
      // 读取指定长度的消息
      is.read(bytes);
      System.out.println("客户端输入：" + new String(bytes));
    }
    is.close();
    socket.close();
  }

  /**
   * 通过约定符号告知对方已发送完命令
   *
   * @param socket
   * @throws IOException
   * @throws InterruptedException
   */
  private static void readWriteFromReader(Socket socket) throws IOException, InterruptedException {
    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    while (true) {
      // 读取一行数据
      String readLine = br.readLine();
      System.out.println("来自客户端消息：" + readLine);
      // 向客户端写入一行数据
      bw.write("收到了消息，" + readLine);
      bw.newLine();
      bw.flush();
      // 等待 3秒
      Thread.sleep(500);
    }
  }

  /**
   * 通过Socket关闭输出流的方式，告知对方已经发送完命令
   *
   * @param socket
   * @throws IOException
   */
  private static void readWriteFromStream(Socket socket) throws IOException {
    InputStream is = socket.getInputStream();
    OutputStream os = socket.getOutputStream();

    // 读取一行数据
    byte[] bytes = new byte[1024];
    int len;
    StringBuilder sb = new StringBuilder();
    // 按字节流方式读取数据
    // 注意 InputStream 的 read 方法会一直阻塞，直到输入有输入数据，文件达到末尾，或者有异常抛出
    // 当流到达文件的末尾时，返回-1，即流断开之后返回-1
    while ((len = is.read(bytes)) != -1) {
      sb.append(new String(bytes, 0, len));
    }
    System.out.println("来自客户端消息：" + sb);

    // 服务器端写入消息
    os.write("我收到你的消息了！".getBytes());
    // 通过 shutdownOutput 告诉客户端已经发送完消息
    socket.shutdownOutput();

    is.close();
    os.close();
    socket.close();
  }

}
