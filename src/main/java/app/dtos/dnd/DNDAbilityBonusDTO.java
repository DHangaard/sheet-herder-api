package app.dtos.dnd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DNDAbilityBonusDTO(
        @JsonProperty("ability_score")
        DNDAbilityScoreDTO dndAbilityScoreDTO,

        @JsonProperty("bonus")
        int bonus
)
{
}
