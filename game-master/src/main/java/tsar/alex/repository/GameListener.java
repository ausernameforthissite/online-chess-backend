package tsar.alex.repository;

import static tsar.alex.utils.Constants.*;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import java.security.SecureRandom;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;
import tsar.alex.model.Game;


@Component
@Getter
@Setter
public class GameListener extends AbstractMongoEventListener<Game> {

    private static final SecureRandom NUMBER_GENERATOR = new SecureRandom();

    @Override
    public void onBeforeConvert(final BeforeConvertEvent<Game> event) {
        if (event.getSource().getId() == null) {
            event.getSource().setId(generateGameId());
        }
    }

    private String generateGameId() {
        return NanoIdUtils.randomNanoId(NUMBER_GENERATOR, GAME_ID_ALPHABET.toCharArray(), GAME_ID_SIZE);
    }

}