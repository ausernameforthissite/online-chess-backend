package tsar.alex.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tsar.alex.dto.*;
import tsar.alex.model.ChessMove;
import tsar.alex.service.MatchManagementService;
import tsar.alex.service.MatchService;
import tsar.alex.utils.sse.CustomSseEmitter;

@RestController
@RequestMapping("/api/match")
@AllArgsConstructor
public class MatchController {

    private final MatchManagementService matchManagementService;
    private final MatchService matchService;

    @PostMapping("/start")
    public ResponseEntity<StartMatchResponse> startMatch(@RequestBody StartMatchRequest startMatchRequest) {
        StartMatchResponse response = matchManagementService.startMatch(startMatchRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{id}/make_move")
    public ResponseEntity<MakeMoveResponse> makeMove(@PathVariable("id") long matchId, @RequestBody ChessMove chessMove) {
        MakeMoveResponse response = matchService.makeMove(matchId, chessMove);
        if (response instanceof MakeMoveOKResponse) {
            return ResponseEntity.ok().build();
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

    @GetMapping("/{id}/subscribe")
    public ResponseEntity<SseEmitter> subscribeToMatch(@PathVariable("id") long matchId) {
        CustomSseEmitter emitter = new CustomSseEmitter(-1L);
        System.out.println("Subscribe start");
        if (matchManagementService.subscribeToMatch(matchId, emitter)) {
            return new ResponseEntity<>(emitter, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(emitter, HttpStatus.BAD_REQUEST);
        }
    }


}
