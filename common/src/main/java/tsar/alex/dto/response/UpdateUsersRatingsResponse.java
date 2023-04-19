package tsar.alex.dto.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = UpdateUsersRatingsOkResponse.class, name = "ok"),
        @JsonSubTypes.Type(value = UpdateUsersRatingsBadResponse.class, name = "bad"),
})
public interface UpdateUsersRatingsResponse {

}
