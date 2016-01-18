package shnulaa.fx.nio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import shnulaa.fx.constant.Constant;
import shnulaa.fx.exception.NioException;
import shnulaa.fx.info.ClientInfo;
import shnulaa.fx.info.ClientInfo.Status;
import shnulaa.fx.message.MessageOutputImpl;
import shnulaa.fx.pool.TPools;

public abstract class NioServerBase implements IServer {

    private static Logger log = LoggerFactory.getLogger(LocalNioServer.class);
    protected int port;
    protected Selector selector;

    protected final Map<SocketChannel, ClientInfo> clientInfo;
    protected final AtomicLong clientId;
    protected MessageOutputImpl messageOutputImpl;
    protected ByteBuffer readBuffer = ByteBuffer.allocate(Constant.BUFFER_SIZE);

    protected abstract Selector initSelector();

    protected abstract void progress(SelectionKey key) throws IOException;

    protected abstract void stopServer();

    // protected abstract void logoff();

    public NioServerBase() {
        clientInfo = Maps.newConcurrentMap();
        clientId = new AtomicLong(1);
    }

    public NioServerBase(MessageOutputImpl output, int port) {
        this();
        this.port = port;
        this.messageOutputImpl = output;
        selector = initSelector();
    }

    @Override
    public void run() {
        Thread t = Thread.currentThread();
        while (!t.isInterrupted()) {
            try {
                int r = selector.select();
                if (r <= 0) {
                    log.warn("selector.select() ret <= 0");
                    continue;
                }

                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();
                    if (!key.isValid()) {
                        log.warn("key is not valid..");
                        continue;
                    }

                    // if (key.is)
                    progress(key);
                }
            } catch (Exception e) {
                log.error("Exception occurred when Accept, Read, Write..", e);
                // logoff();1
            }
        }
    }

    protected void outPut(String message) {
        this.messageOutputImpl.output(message);
    }

    public boolean isLogin(final SocketChannel channel) {
        return clientInfo.containsKey(channel);
    }

    public Status getStatus(final SocketChannel channel) {
        ClientInfo info = getInfo(channel);
        return (info == null) ? Status.NOT_LOGIN : info.getStatus();
    }

    public void setStatus(final SocketChannel channel, final Status status) {
        ClientInfo info = getInfo(channel);
        info.setStatus(status);
    }

    public ClientInfo getInfo(final SocketChannel channel) {
        return clientInfo.get(channel);
    }

    /**
     * 
     * @param channel
     * @param name
     * @param t
     */
    public void login(final SocketChannel channel, final String name, final Thread t) {
        if (clientInfo.containsKey(channel)) {
            log.warn("name: {} already registed..", name);
        } else {
            long id = clientId.getAndIncrement();
            final ClientInfo c = new ClientInfo(id, name, t);
            clientInfo.put(channel, c);
            c.setStatus(Status.LOGIN_SUCCESS);
        }
    }

    protected String decode(ByteBuffer readBuffer) {
        try {
            return new String(readBuffer.array(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            log.error("decode ByteBuffer error..", ex);
            throw new NioException("decode ByteBuffer error..", ex);
        }
    }

    public void stop() {
        Iterator<SocketChannel> i = clientInfo.keySet().iterator();
        while (i.hasNext()) {
            SocketChannel sc = (SocketChannel) i.next();
            if (sc != null && sc.isOpen()) {
                try {
                    sc.close();
                } catch (IOException e) {
                    log.error("IOException occurred when close socketChannel..", e);
                }
            }

            ClientInfo info = getInfo(sc);
            Thread t = info.getThread();
            if (t != null) {
                t.interrupt();
            }

        }

        stopServer();

        TPools.getInstance().stopInternal();

    }
}
