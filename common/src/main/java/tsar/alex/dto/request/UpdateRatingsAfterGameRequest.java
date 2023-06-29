package tsar.alex.dto.request;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.*;
import tsar.alex.dto.UpdateRatingsIndividualRequestDto;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRatingsAfterGameRequest {

    @NotEmpty(message = "updateRatingsIndividualRequests list is empty")
    private List<@NotNull(message = "updateRatingsIndividualRequestDto is null") UpdateRatingsIndividualRequestDto> updateRatingsIndividualRequests;

}
