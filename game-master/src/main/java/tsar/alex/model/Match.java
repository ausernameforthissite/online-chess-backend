package tsar.alex.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "match")
public class Match {

    @Id
    private long id;
    private boolean finished;
    private UsersInMatch usersInMatch;
    private ChessCoords enPassantPawnCoords;
    private ChessPiece[][] boardState;

}
