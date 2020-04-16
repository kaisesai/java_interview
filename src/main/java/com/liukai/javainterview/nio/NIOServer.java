package com.liukai.javainterview.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO 服务器端
 */
public class NIOServer {

  public static void main(String[] args) throws IOException {
    // 1. serverSelector 负责轮询是否有新的连接，服务器端检测到新的连接之后，不在创建一个新的线程
    // 而是直接将新连接绑定到 clientSelector 上，这样就是不用 IO 模型中 1 万个 while 循环在死等
    Selector serverSelector = Selector.open();
    // 2. clientSelector 负责轮询连接是否有数据可读
    Selector clientSelector = Selector.open();

    new Thread(() -> {
      try {
        // 对应 IO 编程中的服务器端启动
        try (ServerSocketChannel listenerChannel = ServerSocketChannel.open()) {
          listenerChannel.socket().bind(new InetSocketAddress(3333));
          // 配置是否阻塞
          listenerChannel.configureBlocking(false);
          listenerChannel.register(serverSelector, SelectionKey.OP_ACCEPT);

          while (true) {
            // 检测是否有新的连接，这里的 1 指的是阻塞的时间为 1ms
            if (serverSelector.select(1) > 0) {
              Set<SelectionKey> selectionKeys = serverSelector.selectedKeys();
              Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
              while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                if (key.isAcceptable()) {
                  try {
                    // （1）每来一个连接，不需要创建一个线程，而是直接注册到 clientSelector
                    SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
                    clientChannel.configureBlocking(false);
                    clientChannel.register(clientSelector, SelectionKey.OP_READ);
                  } finally {
                    keyIterator.remove();
                  }
                }
              }

            }
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }).start();

    new Thread(() -> {
      try {
        while (true) {
          // （2）批量轮询是否有哪些连接有数据可读，这里的 1 指的是阻塞的时间为 1ms
          if (clientSelector.select(1) > 0) {
            Set<SelectionKey> selectionKeys = clientSelector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

            while (keyIterator.hasNext()) {
              SelectionKey key = keyIterator.next();

              if (key.isReadable()) {
                try {
                  SocketChannel clientChannel = (SocketChannel) key.channel();
                  ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                  // （3）面向 Buffer
                  clientChannel.read(byteBuffer);
                  System.out.println(
                    "收到客户端的消息：" + Charset.defaultCharset().newDecoder().decode(byteBuffer)
                      .toString());
                  byteBuffer.flip();
                } finally {
                  keyIterator.remove();
                  key.interestOps(SelectionKey.OP_READ);
                }
              }

            }

          }

        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }).start();
  }

}
