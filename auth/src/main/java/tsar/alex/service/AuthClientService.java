package tsar.alex.service;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsar.alex.repository.UserRepository;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class AuthClientService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Set<String> getUsernamesOfUsersWithUninitializedRatings() {
        return userRepository.getUsernamesByRatingInitializedFalse();
    }

    public void setMultipleUsersRatingsInitialized(Set<String> usernames) {
        userRepository.setRatingsInitializedByUsernames(usernames);
    }
}