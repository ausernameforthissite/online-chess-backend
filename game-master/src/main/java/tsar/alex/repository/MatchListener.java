package tsar.alex.repository;

import static tsar.alex.utils.Constants.*;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import java.security.SecureRandom;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;
import tsar.alex.model.Match;


@Component
@Getter
@Setter
public class MatchListener extends AbstractMongoEventListener<Match> {
    private static final SecureRandom NUMBER_GENERATOR = new SecureRandom();


    @Override
    public void onBeforeConvert(final BeforeConvertEvent<Match> event) {
        if (event.getSource().getId() == null) {
            event.getSource().setId(generateMatchId());
        }
    }

    private String generateMatchId() {
        return NanoIdUtils.randomNanoId(NUMBER_GENERATOR, MATCH_ID_ALPHABET.toCharArray(), MATCH_ID_SIZE);
    }

}
