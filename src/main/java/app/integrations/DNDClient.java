package app.integrations;

import app.dtos.dnd.*;
import app.exceptions.ExternalApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
public class DNDClient implements IDNDClient
{
    private final HttpClient client;
    private final ObjectMapper objectMapper;

    public DNDClient(HttpClient client, ObjectMapper objectMapper)
    {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    @Override
    public DNDRaceResultDTO fetchAllRaces(String url)
    {
        return fetch(url, DNDRaceResultDTO.class);
    }

    @Override
    public DNDSubraceResultDTO fetchAllSubraces(String url)
    {
        return fetch(url, DNDSubraceResultDTO.class);
    }

    @Override
    public DNDTraitResultDTO fetchAllTraits(String url)
    {
        return fetch(url, DNDTraitResultDTO.class);
    }

    @Override
    public DNDLanguageResultDTO fetchAllLanguages(String url)
    {
        return fetch(url, DNDLanguageResultDTO.class);
    }

    @Override
    public DNDRaceDetailDTO fetchRaceDetails(String url)
    {
        return fetch(url, DNDRaceDetailDTO.class);
    }

    @Override
    public DNDSubraceDetailDTO fetchSubraceDetails(String url)
    {
        return fetch(url, DNDSubraceDetailDTO.class);
    }

    @Override
    public DNDTraitDetailDTO fetchTraitDetails(String url)
    {
        return fetch(url, DNDTraitDetailDTO.class);
    }

    @Override
    public DNDLanguageDetailDTO fetchLanguageDetails(String url)
    {
        return fetch(url, DNDLanguageDetailDTO.class);
    }

    private <T> T fetch(String url, Class<T> responseType)
    {
        HttpRequest request = buildRequest(url);
        try
        {
            String body = getResponse(request);
            return objectMapper.readValue(body, responseType);
        }
        catch (IOException | InterruptedException e)
        {
            if (e instanceof InterruptedException)
                Thread.currentThread().interrupt();

            log.error("Failed to fetch from DnD 5E API at {}: {}", url, e.getMessage(), e);
            throw new ExternalApiException("DnD 5E API is unavailable");
        }
    }

    private String getResponse(HttpRequest request) throws IOException, InterruptedException
    {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200)
        {
            log.error("DnD 5E API returned unexpected status {} for {}", response.statusCode(), request.uri());
            throw new ExternalApiException("DnD 5E API is unavailable");
        }
        return response.body();
    }

    private HttpRequest buildRequest(String url)
    {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("accept", "application/json")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
    }
}
