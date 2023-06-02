package tsar.alex.controller;

import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tsar.alex.dto.request.InitializeUserRatingRequest;
import tsar.alex.dto.request.UpdateUsersRatingsRequest;
import tsar.alex.dto.response.InitializeUserRatingBadResponse;
import tsar.alex.dto.response.InitializeUserRatingResponse;
import tsar.alex.dto.response.RestApiOkResponse;
import tsar.alex.dto.response.UpdateUsersRatingsBadResponse;
import tsar.alex.dto.response.UpdateUsersRatingsOkResponse;
import tsar.alex.dto.response.UpdateUsersRatingsResponse;
import tsar.alex.dto.response.UserInGameStatusResponse;
import tsar.alex.dto.response.UsersRatingsDataForGameBadResponse;
import tsar.alex.dto.response.UsersRatingsDataForGameResponse;
import tsar.alex.service.MatcherService;
import tsar.alex.utils.Utils;


@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class UserRatingController {

    private final MatcherService matcherService;

    @GetMapping("/game/{id}/ratings")
    public ResponseEntity<UsersRatingsDataForGameResponse> getUsersRatingDataByGameId(@PathVariable("id") String gameId) {
        String errorMessage = Utils.validateGameId(gameId);

        if (errorMessage != null) {
            return new ResponseEntity<>(new UsersRatingsDataForGameBadResponse(errorMessage), HttpStatus.NOT_FOUND);
        }

        UsersRatingsDataForGameResponse response = matcherService.getUsersRatingsDataByGameId(gameId);

        HttpStatus httpStatus = response instanceof RestApiOkResponse ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(response, httpStatus);
    }


    @GetMapping("/user")
    public ResponseEntity<UserInGameStatusResponse> getUserInGameStatus() {
        UserInGameStatusResponse response = matcherService.getUserInGameStatus();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/initialize_user_rating")
    public ResponseEntity<InitializeUserRatingResponse> initializeUserRating(
            @RequestBody @Valid InitializeUserRatingRequest request, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new InitializeUserRatingBadResponse(
                    Utils.getBindingResultErrorsAsString(bindingResult)), HttpStatus.BAD_REQUEST);
        }

        InitializeUserRatingResponse response = matcherService.initializeUserRating(request.getUsername());
        HttpStatus httpStatus = response instanceof RestApiOkResponse ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, httpStatus);
    }

    @PostMapping("/update_users_ratings")
    public ResponseEntity<UpdateUsersRatingsResponse> updateUsersRatings(
            @RequestBody @Valid UpdateUsersRatingsRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new UpdateUsersRatingsBadResponse(Utils.getBindingResultErrorsAsString(bindingResult)),
                    HttpStatus.BAD_REQUEST);
        }
        matcherService.updateAfterGameFinished(request);

        return new ResponseEntity<>(new UpdateUsersRatingsOkResponse(), HttpStatus.OK);
    }

}
