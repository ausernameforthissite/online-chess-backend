package tsar.alex.controller;

import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tsar.alex.dto.*;
import tsar.alex.dto.request.StartMatchRequest;
import tsar.alex.dto.response.RestApiOkResponse;
import tsar.alex.dto.response.StartMatchBadResponse;
import tsar.alex.dto.response.StartMatchResponse;
import tsar.alex.service.MatchService;
import tsar.alex.utils.Utils;


@RestController
@RequestMapping("/api/match")
@AllArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @PostMapping("/start")
    public ResponseEntity<StartMatchResponse> startMatch(@RequestBody @Valid StartMatchRequest startMatchRequest,
                                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new StartMatchBadResponse(Utils.getBindingResultErrorsAsString(bindingResult)),
                    HttpStatus.BAD_REQUEST);
        }
        StartMatchResponse response = matchService.startMatch(startMatchRequest);

        HttpStatus httpStatus = response instanceof RestApiOkResponse ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, httpStatus);
    }


    @GetMapping("/{id}/state")
    public ResponseEntity<MatchStateResponse> getMatchState(@PathVariable("id") String matchId) {

        String errorMessage = Utils.validateMatchId(matchId);

        if (errorMessage != null) {
            return new ResponseEntity<>(new MatchStateBadResponse(errorMessage), HttpStatus.NOT_FOUND);
        }

        MatchStateResponse response = matchService.getMatchState(matchId);

        HttpStatus httpStatus = response instanceof RestApiOkResponse ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(response, httpStatus);
    }
}