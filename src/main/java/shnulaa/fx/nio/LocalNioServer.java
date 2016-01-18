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
            outPut("listening port:" + port);
        } catch (IOException e) {
            log.error("initialize Nio server error", e);
            throw new NioException("initialize Nio server error", e);
        }
        return selector;
    }

    @Override
    protected void progress(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            ServerSocketChannel c = (ServerSocketChannel) key.channel();
            SocketChannel sc = c.accept(); // receive socket
            sc.configureBlocking(false);
            sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        } else if (key.isReadable()) {
            SocketChannel sc = (SocketChannel) key.channel();

            readBuffer.clear();
            sc.read(readBuffer);
            final String context = decode(readBuffer).replaceAll("\n", StringUtils.EMPTY);

            Status status = getStatus(sc);
            switch (status) {
            case NOT_LOGIN:
                // not login yet
                login(sc, context, Thread.currentThread());
                outPut("User:" + context + " login successfully..");
                sc.register(selector, SelectionKey.OP_WRITE);
                break;
            case LOGIN_SUCCESS:
                setStatus(sc, Status.CHAT);
                break;
            case CHAT:
                final ClientInfo info = getInfo(sc);
                final String message = info.getName() + " said:" + context;
                outPut(message);
                break;
            default:
                break;
            }
            sc.register(selector, SelectionKey.OP_WRITE);
        } else if (key.isWritable()) {
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
