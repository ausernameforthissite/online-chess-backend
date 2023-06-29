package tsar.alex.model;


import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class WebsocketSessionWrapper {

    private final WebSocketSession session;
    private ScheduledFuture<?> timeoutDisconnectTask;

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    protected final Lock readLock = rwLock.readLock();
    protected final Lock writeLock = rwLock.writeLock();


    public WebsocketSessionWrapper(WebSocketSession session, ScheduledFuture<?> timeoutDisconnectTask) {
        this.session = session;
        this.timeoutDisconnectTask = timeoutDisconnectTask;
    }

    public WebSocketSession getSession() {
        readLock.lock();
        try {
            return session;
        } finally {
            readLock.unlock();
        }
    }

    public ScheduledFuture<?> getTimeoutDisconnectTask() {
        readLock.lock();
        try {
            return timeoutDisconnectTask;
        } finally {
            readLock.unlock();
        }
    }

    public void setTimeoutDisconnectTask(ScheduledFuture<?> timeoutDisconnectTask) {
        writeLock.lock();
        try {
            this.timeoutDisconnectTask = timeoutDisconnectTask;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public String toString() {
        readLock.lock();
        try {
            return "WebsocketSessionWrapper{" +
                    "session=" + session +
                    ", timeoutDisconnectTask=" + timeoutDisconnectTask +
                    '}';
        } finally {
            readLock.unlock();
        }
    }
}
