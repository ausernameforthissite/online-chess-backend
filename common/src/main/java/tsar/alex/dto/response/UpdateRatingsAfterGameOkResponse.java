package tsar.alex.dto.response;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import tsar.alex.dto.UpdateRatingsIndividualResultDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UpdateRatingsAfterGameOkResponse implements UpdateRatingsAfterGameResponse, RestApiOkResponse {

    @NotEmpty(message = "updateRatingsIndividualResults list is empty")
    private List<@NotNull(message = "updateRatingsIndividualResultDto is null") UpdateRatingsIndividualResultDto> updateRatingsIndividualResults;

}
