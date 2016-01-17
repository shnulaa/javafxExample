package shnulaa.fx.manager;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import com.google.common.collect.Maps;

import shnulaa.fx.info.ClientInfo;

/**
 * 
 * @author shnulaa
 *
 */
public class Manager {

    private static final Logger log = Logger.getLogger(Manager.class.getName());

    private final Map<SocketChannel, ClientInfo> cacheInfo = Maps.newConcurrentMap();
    private final AtomicLong clientId = new AtomicLong(1);
    private ServerSocketChannel server;

    public void setServer(ServerSocketChannel server) {
        this.server = server;
    }

    private Manager() {
    }

    public boolean isLogin(final SocketChannel channel) {
        return cacheInfo.containsKey(channel);
    }

    public void stop() {
        Iterator<SocketChannel> i = cacheInfo.keySet().iterator();
        while (i.hasNext()) {
            SocketChannel sc = (SocketChannel) i.next();
            if (sc != null && sc.isOpen()) {
                try {
                    sc.close();
                } catch (IOException e) {
                    // log.error("IOException occurred when close
                    // socketChannel..", e);
                }
            }
        }
        
        

    }

    /**
     * 
     * @param channel
     * @param name
     * @param t
     */
    public void registeClient(final SocketChannel channel, final String name, final Thread t) {
        if (cacheInfo.containsKey(channel)) {
            log.warning("name: {} already registed..");
        } else {
            long id = clientId.getAndIncrement();
            final ClientInfo c = new ClientInfo(id, name, t);
            cacheInfo.put(channel, c);
        }
    }

    static class SingletonHolder {
        public static final Manager MANAGER = new Manager();
    }

    public static Manager getInstance() {
        return SingletonHolder.MANAGER;
    }

}
