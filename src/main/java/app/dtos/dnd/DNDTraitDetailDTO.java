package app.dtos.dnd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DNDTraitDetailDTO(
        @JsonProperty("index")
        String index,

        @JsonProperty("races")
        List<DNDRaceDTO> races,

        @JsonProperty("subraces")
        List<DNDSubraceDTO> subraces,

        @JsonProperty("name")
        String name,

        @JsonProperty("desc")
        List<String> descriptions,

        // TODO add proficiencies

        @JsonProperty("url")
        String url,

        @JsonProperty("updated_at")
        Instant updatedAt
)
{
}
