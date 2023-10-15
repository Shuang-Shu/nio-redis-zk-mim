package filechannel;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FileCopy {
    public static void main(String[] args) throws IOException {
        FileInputStream fis = new FileInputStream("source.txt");
        FileOutputStream fos = new FileOutputStream("target.txt");
        var fic = fis.getChannel();
        var foc = fos.getChannel();
        // read
        var byteBuf = ByteBuffer.allocate(4);
        while (fic.read(byteBuf) > 0) {
            byteBuf.flip();
            foc.write(byteBuf);
            byteBuf.clear();
        }
        fic.close();
        foc.close();
        fis.close();
        fos.close();
    }
}