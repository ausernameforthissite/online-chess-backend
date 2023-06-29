package tsar.alex.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = UpdateRatingsOkIndividualResultDto.class, name = "ok"),
        @JsonSubTypes.Type(value = UpdateRatingsBadIndividualResultDto.class, name = "bad"),
})
public interface UpdateRatingsIndividualResultDto {

}
