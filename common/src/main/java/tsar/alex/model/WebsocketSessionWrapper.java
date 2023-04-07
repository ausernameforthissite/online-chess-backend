package tsar.alex.model;


import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class WebsocketSessionWrapper {

    private final WebSocketSession session;
    private ScheduledFuture<?> timeoutFinisher;

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();


    public WebsocketSessionWrapper(WebSocketSession session, ScheduledFuture<?> timeoutFinisher) {
        this.session = session;
        this.timeoutFinisher = timeoutFinisher;
    }

    public WebSocketSession getSession() {
        readLock.lock();
        try {
            return session;
        } finally {
            readLock.unlock();
        }
    }

    public ScheduledFuture<?> getTimeoutFinisher() {
        readLock.lock();
        try {
            return timeoutFinisher;
        } finally {
            readLock.unlock();
        }
    }

    public void setTimeoutFinisher(ScheduledFuture<?> timeoutFinisher) {
        writeLock.lock();
        try {
            this.timeoutFinisher = timeoutFinisher;
        } finally {
            writeLock.unlock();
        }
    }

}
