package tsar.alex.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ResourceController {

    @GetMapping("/home")
    public ResponseEntity<String> getHomePage() {

        return new ResponseEntity<>("Hello! This is our home page", HttpStatus.OK);
    }
}
