package tsar.alex.model;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UserWaitingForGame implements Comparable<UserWaitingForGame> {

    private boolean waitingForAnswer;
    private ChessGameTypeWithTimings searchGameType;
    private WebsocketSessionWrapper sessionWrapper;
    private CurrentUserRating currentUserRating;

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    public boolean isWaitingForAnswer() {
        readLock.lock();
        try {
            return waitingForAnswer;
        } finally {
            readLock.unlock();
        }
    }

    public void setWaitingForAnswer(boolean waitingForAnswer) {
        writeLock.lock();
        try {
            this.waitingForAnswer = waitingForAnswer;
        } finally {
            writeLock.unlock();
        }
    }

    public CurrentUserRating getCurrentUserRating() {
        readLock.lock();
        try {
            return currentUserRating;
        } finally {
            readLock.unlock();
        }
    }

    public void setCurrentUserRating(CurrentUserRating currentUserRating) {
        writeLock.lock();
        try {
            this.currentUserRating = currentUserRating;
        } finally {
            writeLock.unlock();
        }
    }

    public WebsocketSessionWrapper getSessionWrapper() {
        readLock.lock();
        try {
            return sessionWrapper;
        } finally {
            readLock.unlock();
        }
    }

    public void setSessionWrapper(WebsocketSessionWrapper sessionWrapper) {
        writeLock.lock();
        try {
            this.sessionWrapper = sessionWrapper;
        } finally {
            writeLock.unlock();
        }
    }

    public ChessGameTypeWithTimings getSearchGameType() {
        readLock.lock();
        try {
            return searchGameType;
        } finally {
            readLock.unlock();
        }
    }

    public void setSearchGameType(ChessGameTypeWithTimings searchGameType) {
        writeLock.lock();
        try {
            this.searchGameType = searchGameType;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public int compareTo(UserWaitingForGame o) {
        return Integer.compare(this.getCurrentUserRating().getRating(), o.getCurrentUserRating().getRating());
    }
}
