package tsar.alex.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tsar.alex.dto.StartMatchRequest;
import tsar.alex.dto.StartMatchResponse;
import tsar.alex.service.StartMatchService;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class MatchController {

    private final StartMatchService startMatchService;

    @PostMapping("/start_match")
    public ResponseEntity<StartMatchResponse> startMatch(@RequestBody StartMatchRequest startMatchRequest) {
        StartMatchResponse response = startMatchService.startMatch(startMatchRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
