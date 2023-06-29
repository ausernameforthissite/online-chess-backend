package tsar.alex.mapper;

import lombok.NonNull;
import org.mapstruct.Mapper;
import tsar.alex.dto.response.UsersRatingsDataForGameOkResponse;
import tsar.alex.model.ChessGameUsersRatingsRecord;

@Mapper(componentModel = "spring")
public interface MatcherMapper {

    default UsersRatingsDataForGameOkResponse mapToUsersRatingsDataForGameOkResponse(
            @NonNull ChessGameUsersRatingsRecord gameUserRatingsRecord) {
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
