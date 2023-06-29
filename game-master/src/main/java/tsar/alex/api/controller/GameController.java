package tsar.alex.api.controller;

import static tsar.alex.utils.CommonTextConstants.OBJECT_HAS_ERRORS;
import static tsar.alex.utils.Endpoints.GAME_STATE;
import static tsar.alex.utils.Endpoints.MATCHER_AVAILABLE;
import static tsar.alex.utils.Endpoints.START_GAMES;

import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tsar.alex.api.client.GameMasterRestClient;
import tsar.alex.dto.*;
import tsar.alex.dto.request.StartGameRequest;
import tsar.alex.dto.response.RestApiOkResponse;
import tsar.alex.dto.response.StartGameBadResponse;
import tsar.alex.dto.response.StartGameResponse;
import tsar.alex.service.GameService;
import tsar.alex.utils.Utils;


@RestController
@RequestMapping("/api/game")
@AllArgsConstructor
@Slf4j
public class GameController {

    private final GameService gameService;
    private final GameMasterRestClient gameMasterRestClient;


    @PostMapping(START_GAMES)
    public ResponseEntity<StartGameResponse> startGames(@RequestBody @Valid StartGameRequest startGameRequest,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String message = String.format(OBJECT_HAS_ERRORS, "startGameRequest",
                    Utils.getBindingResultErrorsAsString(bindingResult));
            return new ResponseEntity<>(new StartGameBadResponse(message), HttpStatus.BAD_REQUEST);
        }

        try {
            return new ResponseEntity<>(gameService.startGamesIfNotAlready(startGameRequest), HttpStatus.OK);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new ResponseEntity<>(new StartGameBadResponse(e.toString()), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping(GAME_STATE)
    public ResponseEntity<GameStateResponse> getGameState(@PathVariable("id") String gameId) {

        String errorMessage = Utils.validateGameId(gameId);

        if (errorMessage != null) {
            return new ResponseEntity<>(new GameStateBadResponse(errorMessage), HttpStatus.NOT_FOUND);
        }

        GameStateResponse response = gameService.getGameState(gameId);

        HttpStatus httpStatus = response instanceof RestApiOkResponse ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(response, httpStatus);
    }


    @PostMapping(MATCHER_AVAILABLE)
    public ResponseEntity<Void> matcherMicroserviceBecameAvailable() {
        gameMasterRestClient.setMatcherMicroserviceAvailable(true);
        return ResponseEntity.ok().build();
    }
}