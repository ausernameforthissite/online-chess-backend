package tsar.alex.utils;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestTemplate;
import tsar.alex.dto.request.UpdateUsersRatingsRequest;


public class GameMasterUtils {

    public static String getCurrentUsername() {
        Jwt principal = (Jwt) SecurityContextHolder.
                getContext().getAuthentication().getPrincipal();
        return principal.getClaim("username");
    }

    public static void sendUpdateUsersRatingsRequest(UpdateUsersRatingsRequest request){
        ResponseEntity<Void> response = new RestTemplate().postForEntity(
                "http://localhost:8081/api/update_users_ratings", request, Void.class);
        System.out.println("Update users rating response status: " + response.getStatusCode());
    }
}
