package filechannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class FileSend {

    public static void main(String[] args) throws IOException {
        new Thread(() -> {
            var server = new Receiver();
            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        var sender = new Sender();
        System.out.println("sender is going to send message");
        sender.send();
    }

    static class Sender {
        void send() throws IOException {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            socketChannel.connect(new InetSocketAddress("localhost", 8080));
            var bf = ByteBuffer.wrap("hello! this is sender!".getBytes());
            socketChannel.write(bf);
            socketChannel.shutdownInput();
            socketChannel.close();
        }
    }

    static class Receiver {
        void start() throws IOException {
            var ssc = ServerSocketChannel.open();
            ssc.bind(new InetSocketAddress("0.0.0.0", 8080));
            var sc = ssc.accept();
            ByteBuffer bf = ByteBuffer.allocate(2);
            List<Byte> byteList = new ArrayList<>();
            while (sc.read(bf) > 0) {
                bf.flip();
                while (bf.hasRemaining()) {
                    byteList.add(bf.get());
                }
                bf.clear();
            }
            byte[] bytes = new byte[byteList.size()];
            for (int i = 0; i < byteList.size(); i++) {
                bytes[i] = byteList.get(i);
            }
            System.out.println("Receiver: received: " + new String(bytes));
            sc.shutdownInput();
            sc.close();
            ssc.close();
        }
    }
}
