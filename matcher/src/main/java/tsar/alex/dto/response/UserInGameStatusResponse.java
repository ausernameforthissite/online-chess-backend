package tsar.alex.dto.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = UserInGameStatusTrueResponse.class, name = "true"),
        @JsonSubTypes.Type(value = UserInGameStatusFalseResponse.class, name = "false"),
})
public interface UserInGameStatusResponse {

}