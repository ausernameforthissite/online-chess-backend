package tsar.alex.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = StartGameOkPersonalResultDto.class, name = "ok"),
        @JsonSubTypes.Type(value = StartGameBadEnemyPersonalResultDto.class, name = "badEnemy"),
        @JsonSubTypes.Type(value = StartGameAlreadyInGamePersonalResultDto.class, name = "alreadyInGame"),
        @JsonSubTypes.Type(value = StartGameMultipleActiveGamesPersonalResultDto.class, name = "multipleActiveGames"),
})
public interface StartGamePersonalResultDto {
}