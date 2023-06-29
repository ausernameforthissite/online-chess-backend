package tsar.alex.api.client;


import static tsar.alex.utils.CommonTextConstants.*;
import static tsar.alex.utils.Endpoints.GAME_MASTER_AVAILABLE;
import static tsar.alex.utils.Endpoints.MATCHER_BASE_URL;
import static tsar.alex.utils.Endpoints.UPDATE_USERS_RATINGS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tsar.alex.dto.request.UpdateRatingsAfterGameRequest;
import tsar.alex.dto.response.StartGameBadResponse;
import tsar.alex.dto.response.UpdateRatingsAfterGameBadResponse;
import tsar.alex.dto.response.UpdateRatingsAfterGameResponse;
import tsar.alex.utils.Utils;

@Component
@Slf4j
public class GameMasterRestClient {

    private static final String MATCHER_HI_URL = MATCHER_BASE_URL + GAME_MASTER_AVAILABLE;
    private static final String UPDATE_USERS_RATINGS_URL = MATCHER_BASE_URL + UPDATE_USERS_RATINGS;
    private static final Set<HttpStatus> SERVER_NOT_AVAILABLE_STATUSES = Set.of(HttpStatus.NOT_FOUND, HttpStatus.REQUEST_TIMEOUT);

    private final Validator validator;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private boolean matcherMicroserviceAvailable;

    public GameMasterRestClient(Validator validator, ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.validator = validator;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> setMatcherMicroserviceAvailable(sayHiToMatcherMicroservice()));
        executorService.shutdown();
    }

    public void setMatcherMicroserviceAvailable(boolean matcherMicroserviceAvailable) {
        if (this.matcherMicroserviceAvailable != matcherMicroserviceAvailable) {
            this.matcherMicroserviceAvailable = matcherMicroserviceAvailable;

            if (matcherMicroserviceAvailable) {
                log.debug(String.format(MICROSERVICE_AVAILABLE_LOG, "Matcher"));
            } else {
                log.debug(String.format(MICROSERVICE_UNAVAILABLE_LOG, "Matcher"));
            }
        }
    }

    public boolean isMatcherMicroserviceAvailable() {
        return matcherMicroserviceAvailable;
    }

    private boolean sayHiToMatcherMicroservice() {
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(MATCHER_HI_URL, HttpEntity.EMPTY, Void.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (RestClientException e) {
            log.debug(e.toString());
            return false;
        }
    }

    public UpdateRatingsAfterGameResponse updateRatingsAfterGameFinished(
            UpdateRatingsAfterGameRequest updateRatingsRequest) {

        if (!matcherMicroserviceAvailable) {
            return new UpdateRatingsAfterGameBadResponse(String.format(UNAVAILABLE_SERVICE, "Matcher"));
        }

        HttpEntity<UpdateRatingsAfterGameRequest> httpRequest = new HttpEntity<>(updateRatingsRequest);

        try {
            ResponseEntity<UpdateRatingsAfterGameResponse> response = restTemplate.postForEntity(
                    UPDATE_USERS_RATINGS_URL, httpRequest, UpdateRatingsAfterGameResponse.class);

            HttpStatus httpStatus = response.getStatusCode();

            if (httpStatus == HttpStatus.OK) return checkUpdateRatingsOkResponse(response.getBody());

            return new UpdateRatingsAfterGameBadResponse(String.format(UNEXPECTED_HTTP_STATUS, httpStatus));

        } catch (HttpStatusCodeException e) {

            HttpStatus httpStatus = e.getStatusCode();

            if (httpStatus == HttpStatus.BAD_REQUEST) {
                try {
                    return objectMapper.readValue(e.getResponseBodyAsString(), UpdateRatingsAfterGameBadResponse.class);
                } catch (JsonProcessingException ex) {
                    return new UpdateRatingsAfterGameBadResponse(ex.toString());
                }
            }

            if (SERVER_NOT_AVAILABLE_STATUSES.contains(httpStatus)) {
                setMatcherMicroserviceAvailable(false);
            }

            return new UpdateRatingsAfterGameBadResponse(e.toString());
        } catch (RestClientException e) {
            setMatcherMicroserviceAvailable(false);
            return new UpdateRatingsAfterGameBadResponse(e.toString());
        }
    }

    private UpdateRatingsAfterGameResponse checkUpdateRatingsOkResponse(
            UpdateRatingsAfterGameResponse updateRatingsResponse) {
        if (updateRatingsResponse == null) {
            return new UpdateRatingsAfterGameBadResponse(
                    String.format(RESPONSE_BODY_NULL_LOG, "UpdateRatingsAfterGameOkResponse"));
        }

        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(updateRatingsResponse);

        if (!constraintViolations.isEmpty()) {
            String constraintViolationsAsString = Utils.getConstraintViolationsAsString(constraintViolations);
            String message = String.format(INCORRECT_HTTP_OK_RESPONSE_OBJECT, "UpdateRatingsAfterGameOkResponse",
                    constraintViolationsAsString);
            return new UpdateRatingsAfterGameBadResponse(message);
        }

        return updateRatingsResponse;
    }
}