package shnulaa.fx.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.Charset;
import java.util.Iterator;

import javafx.scene.control.TextArea;
import shnulaa.fx.constant.Constant;
import shnulaa.fx.exception.NioException;
import shnulaa.fx.manager.Manager;

/**
 * 
 * @author shnulaa
 *
 */
public class LocalNioServer implements Runnable {

    // private int port;
    private ServerSocketChannel server;
    private Selector selector;
    private TextArea textArea;
    private Manager manager;

    public LocalNioServer(TextArea textArea, int port) {
        // this.port = port;
        this.textArea = textArea;
        this.manager = Manager.getInstance();
        try {
            this.server = SelectorProvider.provider().openServerSocketChannel();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(port));
            this.selector = Selector.open();
            server.register(selector, SelectionKey.OP_ACCEPT);
            outPut("listening port:" + port);
        } catch (IOException e) {
            throw new NioException("initialize Nio server error", e);
        }
    }

    private void outPut(String message) {
        textArea.appendText(message + Constant.BR);
    }

    @Override
    public void run() {
        Thread t = Thread.currentThread();
        while (!t.isInterrupted()) {
            try {

                int r = selector.select();
                if (r <= 0) {
                    continue;
                }

                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    if (!key.isValid()) {
                        outPut("key is not valid!!");
                        continue;
                    }

                    if (key.isConnectable()) {
                        // ServerSocketChannel c = (ServerSocketChannel)
                        // key.channel();
                        outPut("Connectable");
                    } else if (key.isAcceptable()) {
                        outPut("Acceptable");
                        ServerSocketChannel c = (ServerSocketChannel) key.channel();
                        SocketChannel sc = c.accept(); // receive socket
                        sc.configureBlocking(false);
                        sc.register(selector, SelectionKey.OP_WRITE);

                        // manager.registeClient(sc, name, t);

                    } else if (key.isReadable()) {
                        outPut("Readable");
                        SocketChannel sc = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(16384);
                        sc.read(buffer);
                        outPut(new String(buffer.array()));
                    } else if (key.isWritable()) {
                        SocketChannel sc = (SocketChannel) key.channel();

                        if (manager.isLogin(sc)) {

                        } else {
                            ByteBuffer b = ByteBuffer.wrap(Constant.PROPMT.getBytes(Charset.forName("UTF-8")));
                            sc.write(b);
                        }

                    }

                }

            } catch (IOException e) {
                throw new NioException("Accept, Read, Write error..", e);
            }

        }

    }

}
