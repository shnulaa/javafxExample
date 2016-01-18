package shnulaa.fx.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.control.TextArea;
import shnulaa.fx.constant.Constant;
import shnulaa.fx.exception.NioException;
import shnulaa.fx.info.ClientInfo;
import shnulaa.fx.info.ClientInfo.Status;
import shnulaa.fx.message.MessageOutputImpl;

/**
 * 
 * @author shnulaa
 *
 */
@SuppressWarnings("restriction")
public class LocalNioServer extends NioServerBase {

    private static Logger log = LoggerFactory.getLogger(LocalNioServer.class);

    private ServerSocketChannel server;

    public LocalNioServer(TextArea textArea, int port) {
        this(new MessageOutputImpl(textArea), port);
    }

    public LocalNioServer(MessageOutputImpl outputImpl, int port) {
        super(outputImpl, port);
    }

    @Override
    protected Selector initSelector() {
        try {
            this.server = SelectorProvider.provider().openServerSocketChannel();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(port));
            selector = Selector.open();
            server.register(selector, SelectionKey.OP_ACCEPT);
            outPut("listening port: " + port + " successfully..", true);
        } catch (IOException e) {
            log.error("initialize Nio server error", e);
            throw new NioException("initialize Nio server error", e);
        }
        return selector;
    }

    @Override
    protected void progress(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            accept(key);
        } else if (key.isReadable()) {
            read(key);
        } else if (key.isWritable()) {
            write(key);
        } else {
            log.warn("key is not Acceptable, Readable and Writable..");
        }
    }

    /**
     * accept
     * 
     * @param key
     * @throws IOException
     * @throws ClosedChannelException
     */
    private void accept(SelectionKey key) throws IOException, ClosedChannelException {
        ServerSocketChannel c = (ServerSocketChannel) key.channel();
        SocketChannel sc = c.accept(); // receive socket
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    /**
     * read
     * 
     * @param key
     * @throws IOException
     * @throws ClosedChannelException
     */
    private void read(SelectionKey key) throws IOException, ClosedChannelException {
        SocketChannel sc = (SocketChannel) key.channel();

        readBuffer.clear();
        sc.read(readBuffer);
        final String readed = decode(readBuffer, true);

        String message = StringUtils.EMPTY;
        Status status = getStatus(sc);
        switch (status) {
        case NOT_LOGIN:
            login(sc, readed, Thread.currentThread());
            message = "User:" + readed + " login successfully..";
            outPut(message, true);
            break;
        case LOGIN_SUCCESS:
        case CHAT:
            setStatus(sc, Status.CHAT);
            final ClientInfo info = getInfo(sc);
            message = info.getName() + " said:" + readed;
            outPut(message);
            break;
        default:
            break;
        }

        if (StringUtils.isNotEmpty(message)) {
            notify(sc, message);
        }
        sc.register(selector, SelectionKey.OP_WRITE);
    }

    /**
     * write
     * 
     * @param key
     * @throws IOException
     * @throws ClosedChannelException
     */
    private void write(SelectionKey key) throws IOException, ClosedChannelException {
        SocketChannel sc = (SocketChannel) key.channel();

        String writeContext = StringUtils.EMPTY;
        Status status = getStatus(sc);

        switch (status) {
        case NOT_LOGIN:
            writeContext = Constant.PROPMT;
            break;
        case LOGIN_SUCCESS:
            writeContext = Constant.LOGIN_SUCCESS;
            break;
        case CHAT:
            writeContext = Constant.CHAT;
            break;
        default:
            break;
        }

        ByteBuffer b = ByteBuffer.wrap(writeContext.getBytes(Charset.forName("UTF-8")));
        sc.write(b);
        sc.register(selector, SelectionKey.OP_READ);
    }

    @Override
    protected void stopServer() {
        if (server != null) {
            try {
                server.close();
            } catch (IOException e) {
                log.error("IOException occurred when close channel server..", e);
            }
        }

    }

}
