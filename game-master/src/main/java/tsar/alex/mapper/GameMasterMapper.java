package tsar.alex.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tsar.alex.dto.GameStateOkResponse;
import tsar.alex.dto.StartGameAlreadyInGamePersonalResultDto;
import tsar.alex.dto.StartGameOkPersonalResultDto;
import tsar.alex.dto.UpdateRatingsIndividualRequestDto;
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
    UpdateRatingsIndividualRequestDto mapToUpdateRatingsIndividualRequestDto(Game game);

    @Mapping(target="requestingUsername", source="requestingUsername")
    @Mapping(target="gameId", source="game.id")
    @Mapping(target="gameType", source="game.gameType")
    @Mapping(target="startedAt", source="game.startedAt")
    @Mapping(target="whiteUsername", source="game.usersInGame.whiteUsername")
    @Mapping(target="blackUsername", source="game.usersInGame.blackUsername")
    StartGameAlreadyInGamePersonalResultDto mapToStartGameAlreadyInGamePersonalResultDto(Game game, String requestingUsername);

    @Mapping(target="gameId", source="game.id")
    @Mapping(target="whiteUsername", source="game.usersInGame.whiteUsername")
    @Mapping(target="blackUsername", source="game.usersInGame.blackUsername")
    StartGameOkPersonalResultDto mapToStartGameOkPersonalResultDto(Game game);


}