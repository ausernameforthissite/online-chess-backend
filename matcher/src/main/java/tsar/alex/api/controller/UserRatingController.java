package tsar.alex.api.controller;


import static tsar.alex.utils.CommonTextConstants.*;
import static tsar.alex.utils.Endpoints.*;

import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tsar.alex.api.client.MatcherRestClient;
import tsar.alex.dto.request.InitializeUsersRatingsRequest;
import tsar.alex.dto.request.UpdateRatingsAfterGameRequest;
import tsar.alex.dto.response.InitializeUsersRatingsBadResponse;
import tsar.alex.dto.response.InitializeUsersRatingsOkResponse;
import tsar.alex.dto.response.InitializeUsersRatingsResponse;
import tsar.alex.dto.response.RestApiOkResponse;
import tsar.alex.dto.response.UpdateRatingsAfterGameBadResponse;
import tsar.alex.dto.response.UpdateRatingsAfterGameResponse;
import tsar.alex.dto.response.UserInGameStatusResponse;
import tsar.alex.dto.response.UsersRatingsDataForGameBadResponse;
import tsar.alex.dto.response.UsersRatingsDataForGameResponse;
import tsar.alex.service.MatcherService;
import tsar.alex.utils.Utils;


@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Slf4j
public class UserRatingController {

    private final MatcherRestClient matcherRestClient;
    private final MatcherService matcherService;

    @GetMapping(USERS_RATINGS)
    public ResponseEntity<UsersRatingsDataForGameResponse> getUsersRatingDataByGameId(@PathVariable("id")  String gameId) {
        String errorMessage = Utils.validateGameId(gameId);

        if (errorMessage != null) {
            return new ResponseEntity<>(new UsersRatingsDataForGameBadResponse(errorMessage), HttpStatus.BAD_REQUEST);
        }

        UsersRatingsDataForGameResponse response = matcherService.getUsersRatingsDataByGameId(gameId);
        HttpStatus httpStatus = response instanceof RestApiOkResponse ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(response, httpStatus);
    }

    @GetMapping(USER_STATUS)
    public ResponseEntity<UserInGameStatusResponse> getUserInGameStatus() {
        UserInGameStatusResponse response = matcherService.getUserInGameStatus();
        HttpStatus httpStatus = response instanceof RestApiOkResponse ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, httpStatus);
    }

    @PostMapping(INITIALIZE_USERS_RATINGS)
    public ResponseEntity<InitializeUsersRatingsResponse> initializeUsersRatings(
            @RequestBody @Valid InitializeUsersRatingsRequest initializeUsersRatingsRequest,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String message = String.format(OBJECT_HAS_ERRORS, "initializeUsersRatingsRequest",
                    Utils.getBindingResultErrorsAsString(bindingResult));
            return new ResponseEntity<>(new InitializeUsersRatingsBadResponse(message), HttpStatus.BAD_REQUEST);
        }

        for (String username : initializeUsersRatingsRequest.getUsernames()) {
            matcherService.initializeUserRatingsIfNotAlready(username);
        }

        return new ResponseEntity<>(new InitializeUsersRatingsOkResponse(), HttpStatus.OK);
    }

    @PostMapping(UPDATE_USERS_RATINGS)
    public ResponseEntity<UpdateRatingsAfterGameResponse> updateRatingsAfterGameFinished(
            @RequestBody @Valid UpdateRatingsAfterGameRequest request, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new UpdateRatingsAfterGameBadResponse(Utils.getBindingResultErrorsAsString(bindingResult)),
                    HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(matcherService.updateRatingsAfterGame(request.getUpdateRatingsIndividualRequests()),
                HttpStatus.OK);
    }

    @PostMapping(AUTH_AVAILABLE)
    public ResponseEntity<Void> authMicroserviceBecameAvailable() {
        return ResponseEntity.ok().build();
    }

    @PostMapping(GAME_MASTER_AVAILABLE)
    public ResponseEntity<Void> gameMasterMicroserviceBecameAvailable() {
        matcherRestClient.setGameMasterMicroserviceAvailable(true);
        return ResponseEntity.ok().build();
    }

}
