package tsar.alex.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tsar.alex.dto.*;
import tsar.alex.service.MatchService;


@RestController
@RequestMapping("/api/match")
@AllArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @PostMapping("/start")
    public ResponseEntity<StartMatchResponse> startMatch(@RequestBody StartMatchRequest startMatchRequest) {
        StartMatchResponse response = matchService.startMatch(startMatchRequest.getPairOfUsernames());

        if (response instanceof StartMatchOkResponse) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/{id}/state")
    public ResponseEntity<MatchStateResponse> getMatchState(@PathVariable("id") long matchId) {
        MatchStateResponse response = matchService.getMatchState(matchId);
        if (response instanceof MatchStateOkResponse) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

}
