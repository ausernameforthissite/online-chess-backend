package tsar.alex.mapper;

import org.mapstruct.Mapper;
import tsar.alex.dto.response.StartGameOkResponse;
import tsar.alex.dto.request.StartGameRequest;
import tsar.alex.dto.response.UsersRatingsDataForGameOkResponse;
import tsar.alex.model.ChessGameTypeWithTimings;
import tsar.alex.model.ChessGameUserRatingsRecord;
import tsar.alex.model.Pair;
import tsar.alex.model.CurrentUserRating;
import tsar.alex.model.UserWaitingForGame;

@Mapper(componentModel = "spring")
public interface MatcherMapper {

    default StartGameRequest mapToStartGameRequest(ChessGameTypeWithTimings chessGameTypeWithTimings,
            Pair<UserWaitingForGame> uwfgPair) {
        return new StartGameRequest(chessGameTypeWithTimings,
                new Pair<>(uwfgPair.get(0).getCurrentUserRating().getUsername(),
                        uwfgPair.get(1).getCurrentUserRating().getUsername()));
    }

    default ChessGameUserRatingsRecord mapToChessGameUserRatingsRecord(StartGameOkResponse response,
            ChessGameTypeWithTimings chessGameType, Pair<UserWaitingForGame> uwfgPair) {
        CurrentUserRating whitePlayer;
        CurrentUserRating blackPlayer;

        if (response.isSameUsersOrder()) {
            whitePlayer = uwfgPair.get(0).getCurrentUserRating();
            blackPlayer = uwfgPair.get(1).getCurrentUserRating();
        } else {
            whitePlayer = uwfgPair.get(1).getCurrentUserRating();
            blackPlayer = uwfgPair.get(0).getCurrentUserRating();
        }

        return ChessGameUserRatingsRecord.builder().gameId(response.getGameId()).chessGameType(chessGameType)
                .startedAt(response.getStartedAt()).finished(false).whiteUsername(whitePlayer.getUsername())
                .whiteInitialRating(whitePlayer.getRating()).blackUsername(blackPlayer.getUsername())
                .blackInitialRating(blackPlayer.getRating()).build();
    }

    default UsersRatingsDataForGameOkResponse mapToUsersRatingsDataForGameOkResponse(
            ChessGameUserRatingsRecord gameUserRatingsRecord) {
        UsersRatingsDataForGameOkResponse okResponse = new UsersRatingsDataForGameOkResponse();
        okResponse.setWhiteInitialRating(gameUserRatingsRecord.getWhiteInitialRating());
        okResponse.setBlackInitialRating(gameUserRatingsRecord.getBlackInitialRating());

        if (gameUserRatingsRecord.isFinished()) {
            if (!gameUserRatingsRecord.isTechnicalFinish()) {
                okResponse.setWhiteRatingChange(gameUserRatingsRecord.getWhiteRatingChange());
                okResponse.setBlackRatingChange(gameUserRatingsRecord.getBlackRatingChange());
            }
        } else {
            okResponse.setWhiteRatingChange(null);
            okResponse.setBlackRatingChange(null);
        }

        return okResponse;
    }

}