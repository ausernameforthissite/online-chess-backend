package tsar.alex.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tsar.alex.dto.InitializeUserRatingRequest;
import tsar.alex.dto.UpdateUsersRatingsRequest;
import tsar.alex.dto.UsersRatingsDataForMatchOkResponse;
import tsar.alex.dto.UsersRatingsDataForMatchResponse;
import tsar.alex.service.MatcherService;


@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class UserRatingController {

    private final MatcherService matcherService;

    @GetMapping("/match/{id}/ratings")
    public ResponseEntity<UsersRatingsDataForMatchResponse> getUsersRatingDataByMatchId(@PathVariable("id") long matchId) {
        UsersRatingsDataForMatchResponse response = matcherService.getUsersRatingsDataByMatchId(matchId);

        if (response instanceof UsersRatingsDataForMatchOkResponse) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/initialize_user_rating")
    public ResponseEntity<Void> initializeUserRating(@RequestBody InitializeUserRatingRequest request) {

        if (matcherService.initializeUserRating(request.getUsername())) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/update_users_ratings")
    public ResponseEntity<Void> updateUsersRatings(@RequestBody UpdateUsersRatingsRequest request) {
        System.out.println(request);
        if (matcherService.updateAfterMatchFinished(request)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
