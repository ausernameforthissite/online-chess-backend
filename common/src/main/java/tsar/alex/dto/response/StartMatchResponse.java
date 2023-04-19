package tsar.alex.dto.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = StartMatchOkResponse.class, name = "ok"),
        @JsonSubTypes.Type(value = StartMatchBadResponse.class, name = "bad"),
})
public interface StartMatchResponse {

}
