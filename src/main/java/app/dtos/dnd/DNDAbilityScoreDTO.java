package app.dtos.dnd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DNDAbilityScoreDTO(
        @JsonProperty("index")
        String index,

        @JsonProperty("name")
        String name,

        @JsonProperty("url")
        String url
)
{
}
