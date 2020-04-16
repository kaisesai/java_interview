package com.liukai.javainterview.nio;

import java.io.*;
import java.net.Socket;

/**
 * IO 客户端
 *
 * @author liukai
 */
public class IOClient {

  public static void main(String[] args) {
    // 创建多个线程模拟多个客户端调用
    // new Thread(() -> {
    try {
      Socket socket = new Socket("127.0.0.1", 3333);
      // readWriteReuseSocket(socket);
      readWriteFromStream(socket);
    } catch (Exception e) {
      e.printStackTrace();
    }
    // }).start();
  }

  /**
   * 通过指定长度告知对方已发送完命令
   *
   * @param socket
   * @throws IOException
   * @throws InterruptedException
   */
  private static void readWriteReuseSocket(Socket socket) throws IOException, InterruptedException {
    OutputStream os = socket.getOutputStream();
    int i = 0;
    while (true) {
      // 向服务端写入数据
      String msg = "你好啊！" + i++;
      // 计算得知消息的长度
      byte[] bytes = msg.getBytes();
      // 优先发送消息的长度
      os.write(bytes.length >> 8);
      os.write(bytes.length);
      // 再发送消息
      os.write(bytes);
      Thread.sleep(2000);
    }
  }

  /**
   * 通过约定符号告知对方已发送完命令
   *
   * @param socket
   * @throws IOException
   */
  private static void readWriteFromReaderWriter(Socket socket) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    int i = 0;
    while (i != 20) {
      // 向服务端写入数据
      String line = "你好啊！" + i++;
      bw.write(line);
      bw.newLine();
      bw.flush();
      // 读取客户端的消息
      String readLine = br.readLine();
      System.out.println("收到来自服务器端的消息：" + readLine);
    }
  }

  /**
   * 通过Socket关闭输出流的方式，告知对方已经发送完命令
   *
   * @param socket
   * @throws IOException
   * @throws InterruptedException
   */
  private static void readWriteFromStream(Socket socket) throws IOException, InterruptedException {

    OutputStream os = socket.getOutputStream();
    InputStream is = socket.getInputStream();
    // 向服务端写入数据
    String line = "你好啊！";
    os.write(line.getBytes());
    // 通过 shutdownOutput 告诉服务器已经发送完数据，后续只能接收数据
    socket.shutdownOutput();

    StringBuilder sb = new StringBuilder();
    int len;
    byte[] bytes = new byte[1024];
    while ((len = is.read(bytes)) != -1) {
      sb.append(new String(bytes, 0, len));
    }
    System.out.println("服务器端返回：" + sb);

    os.close();
    is.close();
    socket.close();
  }

}

