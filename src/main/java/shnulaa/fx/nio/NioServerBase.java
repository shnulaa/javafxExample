package shnulaa.fx.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import shnulaa.fx.constant.Constant;
import shnulaa.fx.exception.NioException;
import shnulaa.fx.info.ClientInfo;
import shnulaa.fx.info.ClientInfo.Status;
import shnulaa.fx.message.MessageOutputImpl;
import shnulaa.fx.pool.TPools;

/**
 * NioServerBase
 * 
 * @author liuyq
 *
 */
public abstract class NioServerBase implements IServer {

    private static Logger log = LoggerFactory.getLogger(NioServerBase.class);
    protected int port;
    protected Selector selector;

    protected final Map<SocketChannel, ClientInfo> clientInfo;
    protected final AtomicLong clientId;
    protected MessageOutputImpl messageOutputImpl;
    protected ByteBuffer readBuffer = ByteBuffer.allocate(Constant.BUFFER_SIZE);

    protected abstract Selector initSelector();

    protected abstract void progress(SelectionKey key) throws IOException;

    protected abstract void stopServer();

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

                    try {
                        progress(key);
                    } catch (IOException ex) {
                        key.cancel();
                        final SelectableChannel channel = key.channel();
                        if (channel != null && (channel instanceof SocketChannel)) {
                            ClientInfo info = clientInfo.remove(channel);
                            if (info != null) {
                                final String message = "User:" + info.getName() + " logout successfully..";

                                outPut(message, true);
                                notify((SocketChannel) channel,
                                        message + Constant.BR + Constant.SPLIT2 + Constant.BR + Constant.CHAT);
                            }
                            channel.close();
                        } else {
                            log.warn("channel is not the instance of SocketChannel when IOException occurred..");
                        }
                    }
                }
            } catch (ClosedChannelException ex) {
                log.error("ClosedChannelException occurred when Accept, Read, Write, break..", ex);
                break;
            } catch (ClosedSelectorException ex) {
                log.error("ClosedSelectorException occurred when Accept, Read, Write, break..", ex);
                break;
            } catch (Exception e) {
                log.error("Exception occurred when Accept, Read, Write..", e);
            }
        }
    }

    protected void notify(SocketChannel self, String message) {
        Iterator<SocketChannel> i = clientInfo.keySet().iterator();

        while (i.hasNext()) {
            SocketChannel item = (SocketChannel) i.next();
            if (self != item) {
                try {
                    ByteBuffer b = ByteBuffer.wrap(message.getBytes(Constant.CHARSET));
                    item.write(b);
                } catch (IOException e) {
                    log.error("IOException occurred when notify..", e);
                }
            } else {
                log.debug("self notify is not allowed..");
            }
        }
    }

    protected void outPut(String message) {
        this.messageOutputImpl.output(message);
    }

    protected void outPut(String message, boolean withSplit) {
        this.messageOutputImpl.output(message, withSplit);
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

    public void stop() {
        Iterator<SocketChannel> i = clientInfo.keySet().iterator();
        while (i.hasNext()) {
            SocketChannel sc = (SocketChannel) i.next();
            if (sc != null) {
                try {
                    if (sc.isOpen())
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

        if (selector != null) {
            try {
                if (selector.isOpen())
                    selector.close();
            } catch (IOException e) {
                log.error("IOException occurred when close selector..", e);
            }
        }

        stopServer();

        TPools.getInstance().stopInternal();

    }
}
