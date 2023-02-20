package tsar.alex.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;


@Getter
@Setter
@Document(collection = "sequence")
public class PrimarySequence {

    @Id
    private String id;

    private long seq;

}
