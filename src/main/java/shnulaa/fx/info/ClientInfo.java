package shnulaa.fx.info;

import java.io.Serializable;
import java.util.Queue;

import com.google.common.collect.Queues;

/**
 * 
 * @author shnulaa
 *
 */
public class ClientInfo implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -1712911849773956991L;

    public ClientInfo() {
    }

    public ClientInfo(long id, String name, Thread t) {
        this.id = id;
        this.name = name;
        this.thread = t;
        this.setStatus(Status.NOT_LOGIN);
    }

    private long id;
    private String name;
    private Thread thread;
    private Status status;
    private Queue<String> history = Queues.newConcurrentLinkedQueue();

    public void addMessage(String message) {
        history.add(message);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Thread getThread() {
        return thread;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public static enum Status {
        NOT_LOGIN, LOGIN_SUCCESS, CHAT, QUIT
    }

}
