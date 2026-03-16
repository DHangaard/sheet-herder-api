package app.dtos.dnd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DNDLanguageResultDTO(
        @JsonProperty("count")
        int count,

        @JsonProperty("results")
        List<DNDLanguageDTO> languages
)
{
}
