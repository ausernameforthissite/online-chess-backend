package tsar.alex.dto.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = UserInMatchStatusTrueResponse.class, name = "true"),
        @JsonSubTypes.Type(value = UserInMatchStatusFalseResponse.class, name = "false"),
})
public interface UserInMatchStatusResponse {

}