package tsar.alex.repository;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;
import tsar.alex.model.Match;
import tsar.alex.service.PrimarySequenceService;



@Component
@Getter
@Setter
public class MatchListener extends AbstractMongoEventListener<Match> {

    private final PrimarySequenceService primarySequenceService;

    public MatchListener(final PrimarySequenceService primarySequenceService) {
        this.primarySequenceService = primarySequenceService;
    }

    @Override
    public void onBeforeConvert(final BeforeConvertEvent<Match> event) {
        if (event.getSource().getId() == 0) {
            event.getSource().setId(primarySequenceService.getNextValue());
        }
    }

}
