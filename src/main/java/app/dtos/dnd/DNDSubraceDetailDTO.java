package app.dtos.dnd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DNDSubraceDetailDTO(
        @JsonProperty("index")
        String index,

        @JsonProperty("name")
        String name,

        @JsonProperty("race")
        DNDRaceDTO race,

        @JsonProperty("desc")
        String description,

        @JsonProperty("ability_bonuses")
        List<DNDAbilityBonusDTO> abilityBonuses,

        @JsonProperty("racial_traits")
        List<DNDTraitDTO> traits,

        @JsonProperty("url")
        String url,

        @JsonProperty("updated_at")
        Instant updatedAt
)
{
}
