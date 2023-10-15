package reactor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Reactor {
    public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
        new Thread(
                () -> {
                    var server = new Reactor().new EchoServer();
                    try {
                        server.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
        Thread.sleep(1000);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    var client = new Socket("localhost", 8080);
                    var sos = client.getOutputStream();
                    var ios = client.getInputStream();
                    String msg = "!!!";
                    System.out.println("client send: " + msg);
                    sos.write(msg.getBytes());
                    System.out.println("client receive: " + new String(ios.readAllBytes()));
                    // close resources
                    System.out.println("======");
                    sos.close();
                    ios.close();
                    client.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
        Thread.sleep(2000);
    }

    class EchoServer {
        ThreadPoolEchoHandler executorService = new ThreadPoolEchoHandler();

        interface Handler {
            void handle(SelectionKey key, Selector selector) throws ClosedChannelException, IOException;
        }

        class AcceptHandler implements Handler {
            final Selector[] selectors;
            final AtomicInteger current = new AtomicInteger(0);

            public AcceptHandler(Selector[] selectors) {
                this.selectors = selectors;
            }

            @Override
            public void handle(SelectionKey key, Selector selector) throws IOException {
                var sc = ((ServerSocketChannel) key.channel()).accept();
                sc.configureBlocking(false);
                // 开启新的SocketChannel并注册
                selector = selectors[(current.getAndIncrement() % selectors.length + selectors.length)
                        % selectors.length];
                var scKey = sc.register(selector, SelectionKey.OP_READ);
                scKey.attach(executorService);
            }
        }

        class EchoHandler implements Handler {
            @Override
            public void handle(SelectionKey key, Selector selector) throws IOException {
                var ch = (SocketChannel) key.channel();
                var buf = ByteBuffer.allocate(2);
                var bos = new ByteArrayOutputStream();
                int readByte = 0;
                while ((readByte = ch.read(buf)) > 0) {
                    buf.flip();
                    bos.write(buf.array(), 0, buf.limit());
                    buf.clear();
                }
                if (readByte == -1) {
                    // 客户端关闭连接，清理连接
                    key.channel().close();
                    key.cancel();
                    return;
                }
                var msg = new String(bos.toByteArray());
                System.out.println("server receive: " + msg);
                var wbuf = ByteBuffer.wrap(msg.getBytes());
                while (ch.write(wbuf) > 0) {
                }
                key.cancel();
                ch.close();
            }
        }

        class SubReactor {
            final Selector selector;

            public SubReactor(Selector selector) {
                this.selector = selector;
            }

            void start() {
                new Thread(() -> {
                    while (true) {
                        try {
                            if (selector.select(100) > 0) {
                                var keys = selector.selectedKeys();
                                var keyIter = keys.iterator();
                                while (keyIter.hasNext()) {
                                    var k = keyIter.next();
                                    var handler = (Handler) k.attachment();
                                    handler.handle(k, selector);
                                }
                                keys.clear();
                            }
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }).start();
            }
        }

        class ThreadPoolEchoHandler implements Handler {

            static final AtomicInteger threadId = new AtomicInteger();

            ExecutorService executorService = new ThreadPoolExecutor(4, 8, 2000,
                    TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(1), new ThreadFactory() {
                        @Override
                        public Thread newThread(Runnable r) {
                            var t = new Thread(r);
                            t.setName("my-threadpool-" + threadId.getAndIncrement());
                            return t;
                        }
                    });

            @Override
            public void handle(SelectionKey key, Selector selector) throws ClosedChannelException, IOException {
                key.cancel();
                executorService.execute(() -> {
                    try {
                        System.out.println("thread: " + Thread.currentThread() + " processing");
                        var ch = (SocketChannel) key.channel();
                        var buf = ByteBuffer.allocate(2);
                        var bos = new ByteArrayOutputStream();
                        Thread.sleep(1000);
                        if (!ch.isOpen()) {
                            ch.close();
                            key.cancel();
                            return;
                        }
                        while (ch.read(buf) > 0) {
                            buf.flip();
                            bos.write(buf.array(), 0, buf.limit());
                            buf.clear();
                        }
                        var msg = new String(bos.toByteArray());
                        System.out.println("server receive: " + msg);
                        var wbuf = ByteBuffer.wrap(msg.getBytes());
                        while (ch.write(wbuf) > 0) {
                        }
                        ch.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                });
            }
        }

        Selector[] selectors;
        static final int N_SELECTORS = 1;

        void start() throws IOException {
            var ssc = ServerSocketChannel.open();
            selectors = new Selector[N_SELECTORS];
            for (int i = 0; i < N_SELECTORS; i++) {
                selectors[i] = Selector.open();
            }
            // 为selectors[0]添加事件处理器（selectos[0]相当于mainReactor）
            var selector = selectors[0];
            ssc.configureBlocking(false);
            ssc.bind(new InetSocketAddress("localhost", 8080));
            var key = ssc.register(selector, SelectionKey.OP_ACCEPT);
            key.attach(new AcceptHandler(selectors));
            // 启动所有的SubReactors
            for (var s : selectors) {
                new SubReactor(s).start();
            }
        }
    }
}