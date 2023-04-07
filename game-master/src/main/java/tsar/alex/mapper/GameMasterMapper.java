package tsar.alex.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tsar.alex.dto.MatchStateOkResponse;
import tsar.alex.dto.UpdateUsersRatingsRequest;
import tsar.alex.model.*;


@Mapper(componentModel = "spring")
public interface GameMasterMapper {

    @Mapping(target="finished", source="match.finished")
    @Mapping(target="usersInMatch", source="match.usersInMatch")
    @Mapping(target="matchResult", source="match.result")
    @Mapping(target="matchRecord", source="match.chessMovesRecord")
    MatchStateOkResponse mapToMatchStateOkResponse(Match match);


    @Mapping(target="matchId", source="match.id")
    @Mapping(target="technicalFinish", source="match.result.technicalFinish")
    @Mapping(target="draw", source="match.result.draw")
    @Mapping(target="winnerColor", source="match.result.winnerColor")
    UpdateUsersRatingsRequest mapToUpdateUsersRatingsRequest(Match match);

}