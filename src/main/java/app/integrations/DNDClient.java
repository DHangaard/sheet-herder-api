package app.integrations;

import app.dtos.dnd.*;
import app.exceptions.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DNDClient implements IDNDClient
{
    private HttpClient client;
    private ObjectMapper objectMapper;

    public DNDClient(HttpClient client, ObjectMapper objectMapper)
    {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    @Override
    public DNDRaceResultDTO fetchAllRaces(String url)
    {
        HttpRequest request = buildRequest(url);
        try
        {
            String response = getResponse(request);
            DNDRaceResultDTO dndRaceResultDTO = objectMapper.readValue(response, DNDRaceResultDTO.class);
            return dndRaceResultDTO;
        }
        catch (IOException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DNDSubraceResultDTO fetchAllSubraces(String url)
    {
        HttpRequest request = buildRequest(url);
        try
        {
            String response = getResponse(request);
            DNDSubraceResultDTO dndSubraceResultDTO = objectMapper.readValue(response, DNDSubraceResultDTO.class);
            return dndSubraceResultDTO;
        }
        catch (IOException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DNDTraitResultDTO fetchAllTraits(String url)
    {
        HttpRequest request = buildRequest(url);
        try
        {
            String response = getResponse(request);
            DNDTraitResultDTO dndTraitResultDTO = objectMapper.readValue(response, DNDTraitResultDTO.class);
            return dndTraitResultDTO;
        }
        catch (IOException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DNDLanguageResultDTO fetchAllLanguages(String url)
    {
        HttpRequest request = buildRequest(url);
        try
        {
            String response = getResponse(request);
            DNDLanguageResultDTO dndLanguageResultDTO = objectMapper.readValue(response, DNDLanguageResultDTO.class);
            return dndLanguageResultDTO;
        }
        catch (IOException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DNDRaceDetailDTO fetchRaceDetails(String url)
    {
        HttpRequest request = buildRequest(url);
        try
        {
            String response = getResponse(request);
            DNDRaceDetailDTO dndRaceDetailDTO = objectMapper.readValue(response, DNDRaceDetailDTO.class);
            return dndRaceDetailDTO;
        }
        catch (IOException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DNDSubraceDetailDTO fetchSubraceDetails(String url)
    {
        HttpRequest request = buildRequest(url);
        try
        {
            String response = getResponse(request);
            DNDSubraceDetailDTO dndSubraceDetailDTO = objectMapper.readValue(response, DNDSubraceDetailDTO.class);
            return dndSubraceDetailDTO;
        }
        catch (IOException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DNDTraitDetailDTO fetchTraitDetails(String url)
    {
        HttpRequest request = buildRequest(url);
        try
        {
            String response = getResponse(request);
            DNDTraitDetailDTO dndTraitDetailDTO = objectMapper.readValue(response, DNDTraitDetailDTO.class);
            return dndTraitDetailDTO;
        }
        catch (IOException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DNDLanguageDetailDTO fetchLanguageDetails(String url)
    {
        HttpRequest request = buildRequest(url);
        try
        {
            String response = getResponse(request);
            DNDLanguageDetailDTO dndLanguageDetailDTO = objectMapper.readValue(response, DNDLanguageDetailDTO.class);
            return dndLanguageDetailDTO;
        }
        catch (IOException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    private String getResponse(HttpRequest request) throws IOException, InterruptedException
    {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200)
        {
            throw new ApiException(response.statusCode(), "DnD5Eapi returned error code: " + response.statusCode());
        }
        return response.body();
    }

    private HttpRequest buildRequest(String url)
    {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("accept", "applicatio/json")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
    }
}
