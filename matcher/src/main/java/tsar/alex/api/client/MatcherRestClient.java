package tsar.alex.api.client;

import static tsar.alex.utils.CommonTextConstants.*;
import static tsar.alex.utils.Endpoints.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tsar.alex.dto.request.CheckRegisteredRequest;
import tsar.alex.dto.request.StartGameRequest;
import tsar.alex.dto.response.CheckRegisteredBadResponse;
import tsar.alex.dto.response.CheckRegisteredResponse;
import tsar.alex.dto.response.StartGameBadResponse;
import tsar.alex.dto.response.StartGameResponse;
import tsar.alex.dto.response.UpdateRatingsAfterGameBadResponse;
import tsar.alex.utils.Utils;
import tsar.alex.websocket.UsersWaitingForGameWebsocketHolder;


@Component
@Slf4j
public class MatcherRestClient {

    private static final String AUTH_HI_URL = AUTH_BASE_URL + MATCHER_AVAILABLE;
    private static final String GAME_MASTER_HI_URL = GAME_MASTER_BASE_URL + MATCHER_AVAILABLE;
    private static final String CHECK_REGISTERED_URL = AUTH_BASE_URL + CHECK_REGISTERED;
    private static final String START_GAMES_URL = GAME_MASTER_BASE_URL + START_GAMES;
    private static final Set<HttpStatus> SERVER_NOT_AVAILABLE_STATUSES = Set.of(HttpStatus.NOT_FOUND,
            HttpStatus.REQUEST_TIMEOUT);

    private final Validator validator;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    private UsersWaitingForGameWebsocketHolder uwfgWebsocketHolder;

    private boolean gameMasterMicroserviceAvailable;

    public MatcherRestClient(Validator validator, ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.validator = validator;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> sayHiToMicroservice(AUTH_HI_URL));
        executorService.shutdown();
    }

    @Lazy
    @Autowired
    public void setUwfgWebsocketHolder(UsersWaitingForGameWebsocketHolder uwfgWebsocketHolder) {
        this.uwfgWebsocketHolder = uwfgWebsocketHolder;
    }

    public void setGameMasterMicroserviceAvailable(boolean gameMasterMicroserviceAvailable) {
        if (this.gameMasterMicroserviceAvailable != gameMasterMicroserviceAvailable) {
            this.gameMasterMicroserviceAvailable = gameMasterMicroserviceAvailable;

            if (!gameMasterMicroserviceAvailable) {
                log.info(String.format(MICROSERVICE_UNAVAILABLE_LOG, "Game Master"));
                uwfgWebsocketHolder.abortAllSearches();
            } else {
                log.info(String.format(MICROSERVICE_AVAILABLE_LOG, "Game Master"));
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isGameMasterMicroserviceAvailable() {
        return gameMasterMicroserviceAvailable;
    }

    private boolean sayHiToMicroservice(String url) {
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(url, HttpEntity.EMPTY, Void.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (RestClientException e) {
            log.debug(e.toString());
            return false;
        }
    }

    public CheckRegisteredResponse checkIfUserIsRegistered(CheckRegisteredRequest checkRegisteredRequest) {
        try {
            HttpEntity<CheckRegisteredRequest> httpRequest = new HttpEntity<>(checkRegisteredRequest);
            ResponseEntity<CheckRegisteredResponse> response = restTemplate.exchange(CHECK_REGISTERED_URL,
                    HttpMethod.PATCH, httpRequest, CheckRegisteredResponse.class);

            HttpStatus httpStatus = response.getStatusCode();

            if (httpStatus == HttpStatus.OK) {
                return response.getBody();
            }

            return new CheckRegisteredBadResponse(String.format(UNEXPECTED_HTTP_STATUS, httpStatus));

        } catch (HttpStatusCodeException e) {

            HttpStatus httpStatus = e.getStatusCode();

            if (httpStatus == HttpStatus.BAD_REQUEST) {
                try {
                    return objectMapper.readValue(e.getResponseBodyAsString(), CheckRegisteredBadResponse.class);
                } catch (JsonProcessingException ex) {
                    return new CheckRegisteredBadResponse(ex.toString());
                }
            }

            return new CheckRegisteredBadResponse(e.toString());

        } catch (RestClientException e) {
            return new CheckRegisteredBadResponse(e.toString());
        }
    }

    public StartGameResponse startGames(StartGameRequest startGameRequest) {

        if (!gameMasterMicroserviceAvailable) {
            return new StartGameBadResponse(String.format(UNAVAILABLE_SERVICE, "Game Master"));
        }

        HttpEntity<StartGameRequest> httpRequest = new HttpEntity<>(startGameRequest);

        try {
            ResponseEntity<StartGameResponse> response = restTemplate.postForEntity(START_GAMES_URL, httpRequest,
                    StartGameResponse.class);

            HttpStatus httpStatus = response.getStatusCode();

            if (httpStatus == HttpStatus.OK) return checkStartGamesOkResponse(response.getBody());

            return new StartGameBadResponse(String.format(UNEXPECTED_HTTP_STATUS, httpStatus));

        } catch (HttpStatusCodeException e) {

            HttpStatus httpStatus = e.getStatusCode();

            if (httpStatus == HttpStatus.BAD_REQUEST) {
                try {
                    return objectMapper.readValue(e.getResponseBodyAsString(), StartGameBadResponse.class);
                } catch (JsonProcessingException ex) {
                    return new StartGameBadResponse(ex.toString());
                }
            }

            if (SERVER_NOT_AVAILABLE_STATUSES.contains(httpStatus)) {
                setGameMasterMicroserviceAvailable(false);
            }

            return new StartGameBadResponse(e.toString());
        } catch (RestClientException e) {
            setGameMasterMicroserviceAvailable(false);
            return new StartGameBadResponse(e.toString());
        }
    }

    private StartGameResponse checkStartGamesOkResponse(StartGameResponse startGameResponse) {
        if (startGameResponse == null) {
            return new StartGameBadResponse(String.format(RESPONSE_BODY_NULL_LOG, "StartGameOkResponse"));
        }

        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(startGameResponse);

        if (!constraintViolations.isEmpty()) {
            String constraintViolationsAsString = Utils.getConstraintViolationsAsString(constraintViolations);
            String message = String.format(INCORRECT_HTTP_OK_RESPONSE_OBJECT, "StartGameOkResponse",
                    constraintViolationsAsString);
            return new StartGameBadResponse(message);
        }

        return startGameResponse;
    }

    @Scheduled(fixedDelay = 30 * 60 * 1000, initialDelay = 0)
    public void sayHiToGameMasterMicroservice() {
        log.debug(String.format(PRELIMINARY_AVAILABILITY_STATUS_LOG, gameMasterMicroserviceAvailable));

        if (!gameMasterMicroserviceAvailable) {
            setGameMasterMicroserviceAvailable(sayHiToMicroservice(GAME_MASTER_HI_URL));
        }
    }
}
