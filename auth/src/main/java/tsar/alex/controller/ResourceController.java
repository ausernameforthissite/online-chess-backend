package tsar.alex.controller;


import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tsar.alex.model.User;
import tsar.alex.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ResourceController {

    private final AuthService authService;

    @GetMapping("/profile")
    public ResponseEntity<Map<String, String>> getHomePage() {
        User user = authService.getCurrentUser();
        Map<String, String> profile = Map.of("profile", "with name " + user.getUsername() + " and id = " + user.getId());
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }
}
