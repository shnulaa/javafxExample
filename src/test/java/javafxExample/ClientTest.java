package javafxExample;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shnulaa.fx.constant.Constant;
import shnulaa.fx.exception.NioException;

public class ClientTest {
    private static Logger log = LoggerFactory.getLogger(ClientTest.class);

    // private AtomicLong nameAto = new AtomicLong();

    protected String decode(ByteBuffer readBuffer, boolean ignoreBr) {
        try {
            readBuffer.flip(); // flip the buffer for reading
            byte[] bytes = new byte[readBuffer.remaining()]; // create a byte
                                                             // array
                                                             // the length of
                                                             // the
                                                             // number of bytes
                                                             // written to the
                                                             // buffer
            readBuffer.get(bytes); // read the bytes that were written
            String packet = new String(bytes, Constant.CHARSET);
            return ignoreBr ? packet.replaceAll("\n", StringUtils.EMPTY) : packet;
        } catch (Exception ex) {
            log.error("decode ByteBuffer error..", ex);
            throw new NioException("decode ByteBuffer error..", ex);
        }
    }

    @Test
    public void test() throws InterruptedException {
        log.info("test");

        ExecutorService service = Executors.newCachedThreadPool();

        for (int i = 0; i < 1; i++) {
            service.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        SocketChannel sc = SocketChannel.open(new InetSocketAddress(Constant.DEFAULT_PORT));

                        ByteBuffer b = ByteBuffer.allocate(1);
                        // sc.read(b);
                        // log.info(decode(b, true));
                        read(sc, b);

                        // String name =
                        // String.valueOf(nameAto.incrementAndGet());
                        // b.clear();
                        // b.put(name.getBytes(Constant.CHARSET));
                        // log.info(decode(b, true));
                        // write(sc, b);
                        //
                        // b.flip();
                        // sc.read(b);
                        // log.info(decode(b, true));
                        //
                        // b.clear();
                        // for (int j = 0; j < 20; j++) {
                        // sc.read(b);
                        // log.info(decode(b, true));
                        //
                        // b.clear();
                        // b.put((name + "-Hello" + j +
                        // "..\n").getBytes(Constant.CHARSET));
                        //
                        // write(sc, b);
                        // b.flip();
                        // Thread.sleep(1000);
                        // }

                        sc.close();

                    } catch (IOException e) {
                        log.error("IOException InterruptedException occurred when connect to remote..");
                    }
                    // SocketChannel sChannel =
                }

                /**
                 * 
                 * @param sc
                 * @param buffer
                 * @throws IOException
                 */
                private void read(SocketChannel sc, ByteBuffer buffer) throws IOException {
                    int read = sc.read(buffer); // write into buffer
                    while (read != -1) {
                        buffer.flip();
                        while (buffer.hasRemaining()) {
                            // byte[] b = new byte[buffer.remaining()];
                            // buffer.get(b);

                            System.out.println((char) buffer.get());

                            // log.info(new String(b, Constant.CHARSET));
                        }
                        buffer.clear();
                        read = sc.read(buffer);
                    }
                }

                // private void write(SocketChannel sc, ByteBuffer b) throws
                // IOException {
                //
                // // ByteBuffer buffer =
                // // ByteBuffer.wrap(message.getBytes(Constant.CHARSET));
                // while (b.hasRemaining() & sc.write(b) != -1)
                // ;
                // b.flip();
                // }
            });
        }
        service.awaitTermination(20, TimeUnit.HOURS);
        service.shutdown();
    }

}
