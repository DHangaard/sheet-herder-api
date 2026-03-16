package app.dtos.dnd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DNDLanguageDetailDTO(
        @JsonProperty("index")
        String index,

        @JsonProperty("name")
        String name,

        @JsonProperty("desc")
        String description,

        @JsonProperty("type")
        String type,

        @JsonProperty("typical_speakers")
        List<String> typicalSpeakers,

        @JsonProperty("script")
        String script,

        @JsonProperty("url")
        String url,

        @JsonProperty("updated_at")
        Instant updatedAt
)
{
}
