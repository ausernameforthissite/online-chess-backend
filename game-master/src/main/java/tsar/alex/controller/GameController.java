package tsar.alex.controller;

import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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
public class GameController {

    private final GameService gameService;

    @PostMapping("/start")
    public ResponseEntity<StartGameResponse> startGame(@RequestBody @Valid StartGameRequest startGameRequest,
                                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new StartGameBadResponse(Utils.getBindingResultErrorsAsString(bindingResult)),
                    HttpStatus.BAD_REQUEST);
        }
        StartGameResponse response = gameService.startGame(startGameRequest);

        HttpStatus httpStatus = response instanceof RestApiOkResponse ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, httpStatus);
    }


    @GetMapping("/{id}/state")
    public ResponseEntity<GameStateResponse> getGameState(@PathVariable("id") String gameId) {

        String errorMessage = Utils.validateGameId(gameId);

        if (errorMessage != null) {
            return new ResponseEntity<>(new GameStateBadResponse(errorMessage), HttpStatus.NOT_FOUND);
        }

        GameStateResponse response = gameService.getGameState(gameId);

        HttpStatus httpStatus = response instanceof RestApiOkResponse ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(response, httpStatus);
    }
}