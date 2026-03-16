package app.dtos.dnd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DNDTraitDTO(
        @JsonProperty("index")
        String index,

        @JsonProperty("name")
        String name,

        @JsonProperty("url")
        String url
)
{
}
