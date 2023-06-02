package tsar.alex.dto.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = StartGameOkResponse.class, name = "ok"),
        @JsonSubTypes.Type(value = StartGameBadResponse.class, name = "bad"),
})
public interface StartGameResponse {

}
