package tsar.alex.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebsocketSessionMap {

    private final Map<String, WebsocketSessionWrapper> websocketSessionMap = new ConcurrentHashMap<>();

    public WebsocketSessionWrapper put(String sessionId, WebsocketSessionWrapper websocketSessionWrapper) {
        return websocketSessionMap.put(sessionId, websocketSessionWrapper);
    }

    public WebsocketSessionWrapper remove(String sessionId) {
        return websocketSessionMap.remove(sessionId);
    }

    public WebsocketSessionWrapper get(String sessionId) {
        return websocketSessionMap.get(sessionId);
    }


    @Override
    public String toString() {
        return websocketSessionMap.toString();
    }
}