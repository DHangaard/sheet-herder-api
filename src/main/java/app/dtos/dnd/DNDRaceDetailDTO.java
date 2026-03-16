package app.dtos.dnd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DNDRaceDetailDTO(
        @JsonProperty("index")
        String index,

        @JsonProperty("name")
        String name,

        @JsonProperty("speed")
        int speed,

        @JsonProperty("ability_bonuses")
        List<DNDAbilityBonusDTO> abilityBonuses,

        @JsonProperty("age")
        String ageDescription,

        @JsonProperty("alignment")
        String alignment,

        @JsonProperty("size")
        String size,

        @JsonProperty("size_description")
        String sizeDescription,

        @JsonProperty("languages")
        List<DNDLanguageDTO> languages,

        // TODO Add language options

        @JsonProperty("language_desc")
        String languageDescription,

        @JsonProperty("traits")
        List<DNDTraitDTO> traits,

        @JsonProperty("subraces")
        List<DNDSubraceDTO> subraces,

        @JsonProperty("url")
        String url,

        @JsonProperty("updated_at")
        Instant updatedAt
)
{
}

