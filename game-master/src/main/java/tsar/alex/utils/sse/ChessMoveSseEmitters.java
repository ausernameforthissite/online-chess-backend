package tsar.alex.utils.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import tsar.alex.model.ChessMove;

import java.util.*;


public class ChessMoveSseEmitters {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, CustomSseEmitter> emitters = new HashMap<>(2);


    public synchronized boolean contains(String username) {
        return emitters.containsKey(username);
    }

    public synchronized CustomSseEmitter put(String username, CustomSseEmitter emitter) {

        CustomSseEmitter currentEmitter = emitters.get(username);


        emitter.sendOkResponse();

        if (currentEmitter != null) {
            currentEmitter.complete();

        }

        emitters.put(username, emitter);

        emitter.onTimeout(() -> {

            emitter.complete();
            emitters.remove(username);
        });

        return emitter;
    }

    public synchronized void sendToAllSubscribers(ChessMove chessMove) {
        List<String> failedEmitters = new ArrayList<>();


        emitters.forEach((key, value) -> {
            try {
                value.send(mapper.writeValueAsString(chessMove));
            } catch (Exception e) {
                value.completeWithError(e);
                failedEmitters.add(key);
            }
        });

        failedEmitters.forEach(emitters::remove);
    }




    public synchronized void completeAll() {
        emitters.forEach((key, value) -> {
            value.complete();
        });
    }

    @Override
    public String toString() {
        return emitters.toString();
    }
}
