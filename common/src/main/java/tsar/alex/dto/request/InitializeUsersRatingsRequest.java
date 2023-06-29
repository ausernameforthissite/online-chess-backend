package tsar.alex.dto.request;

import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InitializeUsersRatingsRequest {
    @NotEmpty(message = "Usernames set is empty")
    private Set<@NotBlank(message = "Username is blank") String> usernames;
}