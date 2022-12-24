package tsar.alex.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tsar.alex.dto.FindMatchResult;
import tsar.alex.dto.InitializeUserRatingRequest;
import tsar.alex.mapper.MatcherMapper;
import tsar.alex.service.MatcherService;


@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class FindMatchController {

    private final MatcherMapper matcherMapper;

    private final MatcherService matcherService;


    @PostMapping("/initialize_user_rating")
    public ResponseEntity<Void> initializeUserRating(@RequestBody InitializeUserRatingRequest request) {

        if (matcherService.initializeUserRating(matcherMapper.mapToUserRating(request))) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/find_match")
    public ResponseEntity<?> findMatch() {
        FindMatchResult result = matcherService.findMatch();

        if (result.isSuccess()) {
            return new ResponseEntity<>(result.getResponse(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result.getError(), HttpStatus.BAD_REQUEST);
        }
    }



}
