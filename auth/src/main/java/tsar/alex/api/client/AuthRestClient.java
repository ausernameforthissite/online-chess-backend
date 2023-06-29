package tsar.alex.api.client;

import static tsar.alex.utils.CommonTextConstants.*;
import static tsar.alex.utils.Endpoints.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tsar.alex.dto.request.InitializeUsersRatingsRequest;
import tsar.alex.dto.response.InitializeUsersRatingsBadResponse;
import tsar.alex.dto.response.InitializeUsersRatingsResponse;
import tsar.alex.service.AuthClientService;
import tsar.alex.utils.Utils;

@Component
@Slf4j
public class AuthRestClient {

    private static final String MATCHER_HI_URL = MATCHER_BASE_URL + AUTH_AVAILABLE;
    private static final Set<HttpStatus> SERVER_NOT_AVAILABLE_STATUSES = Set.of(HttpStatus.NOT_FOUND, HttpStatus.REQUEST_TIMEOUT);

    private final ObjectMapper objectMapper;
    private final AuthClientService authClientService;
    private final RestTemplate restTemplate;
    private boolean matcherMicroserviceAvailable;

    public AuthRestClient(ObjectMapper objectMapper, RestTemplate restTemplate, AuthClientService authClientService) {
        this.objectMapper = objectMapper;
        this.authClientService = authClientService;
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
                initializeRatingsForAllUsersWhoNeed();
            } else {
                log.debug(String.format(MICROSERVICE_UNAVAILABLE_LOG, "Matcher"));
            }
        }
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

    private void initializeRatingsForAllUsersWhoNeed() {
        Set<String> usernames = authClientService.getUsernamesOfUsersWithUninitializedRatings();

        if (!usernames.isEmpty()) {
            if (initializeUsersRatings(new InitializeUsersRatingsRequest(usernames))) {
                authClientService.setMultipleUsersRatingsInitialized(usernames);
            }
        }
    }

    public boolean initializeUsersRatings(InitializeUsersRatingsRequest initializeUsersRatingsRequest) {
        if (!matcherMicroserviceAvailable) return false;

        String url = MATCHER_BASE_URL + INITIALIZE_USERS_RATINGS;
        HttpEntity<InitializeUsersRatingsRequest> httpRequest = new HttpEntity<>(initializeUsersRatingsRequest);

        try {
            ResponseEntity<InitializeUsersRatingsResponse> response = restTemplate.postForEntity(url, httpRequest,
                    InitializeUsersRatingsResponse.class);
            HttpStatus httpStatus = response.getStatusCode();

            if (httpStatus == HttpStatus.OK) {
                return true;
            } else {
                log.error(String.format(UNEXPECTED_HTTP_STATUS, httpStatus));
                return false;
            }
        } catch (HttpStatusCodeException e) {
            HttpStatus httpStatus = e.getStatusCode();

            if (SERVER_NOT_AVAILABLE_STATUSES.contains(httpStatus)) {
                setMatcherMicroserviceAvailable(false);
            }

            if (httpStatus == HttpStatus.BAD_REQUEST) {
                try {
                    InitializeUsersRatingsBadResponse badResponse = objectMapper.readValue(e.getResponseBodyAsString(), InitializeUsersRatingsBadResponse.class);
                    log.error(badResponse.getMessage());
                } catch (JsonProcessingException ex) {
                    log.error(ex.toString());
                }
            }

            return false;
        } catch (RestClientException e) {
            setMatcherMicroserviceAvailable(false);
            log.debug(e.toString());
            return false;
        }
    }
}