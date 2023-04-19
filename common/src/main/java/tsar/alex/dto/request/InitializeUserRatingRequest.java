package tsar.alex.dto.request;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InitializeUserRatingRequest {
    @NotBlank(message = "Username is blank")
    private String username;
}