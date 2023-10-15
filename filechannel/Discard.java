package filechannel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Discard {
    public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
        new Thread(() -> {
            try {
                new Server().start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        Thread.sleep(1000);
        var client = new Socket("localhost", 8080);
        var sos = client.getOutputStream();
        sos.write("hello world!".getBytes());
        sos.close();
        client.close();
    }

    static class Server {
        void start() throws IOException {
            var ssc = ServerSocketChannel.open();
            ssc.bind(new InetSocketAddress("0.0.0.0", 8080));
            ssc.configureBlocking(false);
            var selector = Selector.open();
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                if (selector.select(100) > 0) {
                    var selectKeys = selector.selectedKeys();
                    var keyIter = selectKeys.iterator();
                    while (keyIter.hasNext()) {
                        var key = keyIter.next();
                        if (key.isReadable()) {
                            // for socketChannel
                            var byteBuf = ByteBuffer.allocate(4);
                            var sc = (SocketChannel) key.channel();
                            var bos = new ByteArrayOutputStream();
                            while (sc.read(byteBuf) > 0) {
                                byteBuf.flip();
                                bos.write(byteBuf.array());
                                byteBuf.clear();
                            }
                            System.out.println(new String(bos.toByteArray()));
                            sc.shutdownOutput();
                            sc.close();
                            key.cancel();
                        } else if (key.isAcceptable()) {
                            var sc = ((ServerSocketChannel) key.channel()).accept();
                            sc.configureBlocking(false);
                            sc.register(selector, SelectionKey.OP_READ);
                        }
                        keyIter.remove();
                    }
                }
            }
        }
    }
}
