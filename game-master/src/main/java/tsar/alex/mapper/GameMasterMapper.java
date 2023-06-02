package tsar.alex.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tsar.alex.dto.GameStateOkResponse;
import tsar.alex.dto.request.UpdateUsersRatingsRequest;
import tsar.alex.model.*;


@Mapper(componentModel = "spring")
public interface GameMasterMapper {

    @Mapping(target="finished", source="game.finished")
    @Mapping(target="usersInGame", source="game.usersInGame")
    @Mapping(target="gameResult", source="game.result")
    @Mapping(target="gameRecord", source="game.chessMovesRecord")
    GameStateOkResponse mapToGameStateOkResponse(Game game);


    @Mapping(target="gameId", source="game.id")
    @Mapping(target="technicalFinish", source="game.result.technicalFinish")
    @Mapping(target="draw", source="game.result.draw")
    @Mapping(target="winnerColor", source="game.result.winnerColor")
    UpdateUsersRatingsRequest mapToUpdateUsersRatingsRequest(Game game);
}