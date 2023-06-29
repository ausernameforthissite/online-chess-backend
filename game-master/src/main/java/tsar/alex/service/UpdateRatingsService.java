package tsar.alex.service;

import static tsar.alex.utils.CommonTextConstants.INCORRECT_RESPONSE;
import static tsar.alex.utils.CommonTextConstants.UNEXPECTED_OBJECT_CLASS;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tsar.alex.api.client.GameMasterRestClient;
import tsar.alex.dto.UpdateRatingsBadIndividualResultDto;
import tsar.alex.dto.UpdateRatingsIndividualRequestDto;
import tsar.alex.dto.UpdateRatingsIndividualResultDto;
import tsar.alex.dto.UpdateRatingsOkIndividualResultDto;
import tsar.alex.dto.request.UpdateRatingsAfterGameRequest;
import tsar.alex.dto.response.UpdateRatingsAfterGameBadResponse;
import tsar.alex.dto.response.UpdateRatingsAfterGameOkResponse;
import tsar.alex.dto.response.UpdateRatingsAfterGameResponse;
import tsar.alex.exception.UnexpectedObjectClassException;
import tsar.alex.mapper.GameMasterMapper;
import tsar.alex.model.Game;
import tsar.alex.repository.GameRepository;

@Service
@AllArgsConstructor
@Slf4j
public class UpdateRatingsService {

    private final GameMasterRestClient gameMasterRestClient;
    private final GameMasterMapper mapper;
    private final GameRepository gameRepository;

    public void updateRatingsAfterGameFinished(List<Game> games) {
        new UpdateRatingsAfterGameFinishedHandler(games).updateRatingsAfterGameFinished();
    }

    private class UpdateRatingsAfterGameFinishedHandler {

        private final int numberOfGames;
        private final List<Game> games;
        private final List<UpdateRatingsIndividualRequestDto> individualRequests;
        private final List<Game> updatedRatingsGames;

        public UpdateRatingsAfterGameFinishedHandler(List<Game> games) {
            this.games = games;
            numberOfGames = games.size();
            individualRequests = games.stream().map(mapper::mapToUpdateRatingsIndividualRequestDto).toList();
            updatedRatingsGames = new ArrayList<>(numberOfGames);
        }

        public void updateRatingsAfterGameFinished() {

            UpdateRatingsAfterGameRequest updateRatingsAfterGameRequest = new UpdateRatingsAfterGameRequest(
                    individualRequests);
            UpdateRatingsAfterGameResponse updateRatingsResponse = gameMasterRestClient.updateRatingsAfterGameFinished(
                    updateRatingsAfterGameRequest);

            if (updateRatingsResponse instanceof UpdateRatingsAfterGameBadResponse badResponse) {
                log.error(badResponse.getMessage());
            } else if (updateRatingsResponse instanceof UpdateRatingsAfterGameOkResponse okResponse) {
                List<UpdateRatingsIndividualResultDto> updateRatingsIndividualResults = okResponse.getUpdateRatingsIndividualResults();

                if (updateRatingsIndividualResults.size() == numberOfGames) {
                    for (int i = 0; i < numberOfGames; i++) {
                        handleUpdateRatingsIndividualResult(updateRatingsIndividualResults.get(i), i);
                    }

                    if (!updatedRatingsGames.isEmpty()) {
                        gameRepository.saveAll(updatedRatingsGames);
                    }
                } else {
                    throw new RuntimeException(String.format(INCORRECT_RESPONSE, updateRatingsResponse));
                }
            } else {
                throw new UnexpectedObjectClassException(String.format(UNEXPECTED_OBJECT_CLASS, "updateRatingsResponse",
                        updateRatingsResponse.getClass()));
            }
        }

        private void handleUpdateRatingsIndividualResult(UpdateRatingsIndividualResultDto individualResult, int index) {
            if (individualResult instanceof UpdateRatingsOkIndividualResultDto) {
                Game game = games.get(index);
                game.setRatingsUpdated(true);
                updatedRatingsGames.add(game);
            } else if (individualResult instanceof UpdateRatingsBadIndividualResultDto badResult) {
                log.error(badResult.getMessage());
            } else {
                throw new UnexpectedObjectClassException(
                        String.format(UNEXPECTED_OBJECT_CLASS, "individualResult", individualResult.getClass()));
            }
        }
    }


}
