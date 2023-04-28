package tsar.alex.mapper;

import org.mapstruct.Mapper;
import tsar.alex.dto.response.StartMatchOkResponse;
import tsar.alex.dto.request.StartMatchRequest;
import tsar.alex.dto.response.UsersRatingsDataForMatchOkResponse;
import tsar.alex.model.ChessGameTypeWithTimings;
import tsar.alex.model.ChessMatchUserRatingsRecord;
import tsar.alex.model.Pair;
import tsar.alex.model.CurrentUserRating;
import tsar.alex.model.UserWaitingForMatch;

@Mapper(componentModel = "spring")
public interface MatcherMapper {


    default StartMatchRequest mapToStartMatchRequest(ChessGameTypeWithTimings chessGameTypeWithTimings, Pair<UserWaitingForMatch> UWFMPair) {
        return new StartMatchRequest(chessGameTypeWithTimings, new Pair<>(UWFMPair.get(0).getCurrentUserRating().getUsername(),
                                        UWFMPair.get(1).getCurrentUserRating().getUsername()));
    }

    default ChessMatchUserRatingsRecord mapToChessMatchUserRatingsRecord(StartMatchOkResponse response, ChessGameTypeWithTimings chessGameType,
                                                                         Pair<UserWaitingForMatch> UWFMPair) {
        CurrentUserRating whitePlayer;
        CurrentUserRating blackPlayer;

        if (response.isSameUsersOrder()) {
            whitePlayer = UWFMPair.get(0).getCurrentUserRating();
            blackPlayer = UWFMPair.get(1).getCurrentUserRating();
        } else {
            whitePlayer = UWFMPair.get(1).getCurrentUserRating();
            blackPlayer = UWFMPair.get(0).getCurrentUserRating();
        }

        return ChessMatchUserRatingsRecord.builder()
                .matchId(response.getMatchId())
                .chessGameType(chessGameType)
                .startedAt(response.getStartedAt())
                .finished(false)
                .whiteUsername(whitePlayer.getUsername()).whiteInitialRating(whitePlayer.getRating())
                .blackUsername(blackPlayer.getUsername()).blackInitialRating(blackPlayer.getRating()).build();
    }

    default UsersRatingsDataForMatchOkResponse mapToUsersRatingsDataForMatchOkResponse(ChessMatchUserRatingsRecord
                                                                                        matchUserRatingsRecord) {
        UsersRatingsDataForMatchOkResponse okResponse = new UsersRatingsDataForMatchOkResponse();
        okResponse.setWhiteInitialRating(matchUserRatingsRecord.getWhiteInitialRating());
        okResponse.setBlackInitialRating(matchUserRatingsRecord.getBlackInitialRating());

        if (matchUserRatingsRecord.isFinished()) {
            if (!matchUserRatingsRecord.isTechnicalFinish()) {
                okResponse.setWhiteRatingChange(matchUserRatingsRecord.getWhiteRatingChange());
                okResponse.setBlackRatingChange(matchUserRatingsRecord.getBlackRatingChange());
            }
        } else {
            okResponse.setWhiteRatingChange(null);
            okResponse.setBlackRatingChange(null);
        }

        return okResponse;
    }

}