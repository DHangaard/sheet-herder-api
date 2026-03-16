package app.services.reference;

import app.dtos.dnd.*;
import app.integrations.IDNDClient;

import java.util.ArrayList;
import java.util.List;

public class DNDFetchingService implements IDNDFetchingService
{
    private IDNDClient dndClient;

    private static final String BASE_URL = "https://www.dnd5eapi.co%s";
    private static final String ALL_RACES_URL = String.format(BASE_URL, "/api/2014/races");
    private static final String ALL_SUBRACES_URL = String.format(BASE_URL, "/api/2014/subraces");
    private static final String ALL_TRAITS_URL = String.format(BASE_URL, "/api/2014/traits");
    private static final String ALL_LANGUAGES_URL = String.format(BASE_URL, "/api/2014/languages");

    public DNDFetchingService(IDNDClient dndClient)
    {
        this.dndClient = dndClient;
    }

    @Override
    public DNDRaceResultDTO fetchAllRaces()
    {
        return dndClient.fetchAllRaces(ALL_RACES_URL);
    }

    @Override
    public DNDSubraceResultDTO fetchAllSubraces()
    {
        return dndClient.fetchAllSubraces(ALL_SUBRACES_URL);
    }

    @Override
    public DNDTraitResultDTO fetchAllTraits()
    {
        return dndClient.fetchAllTraits(ALL_TRAITS_URL);
    }

    @Override
    public DNDLanguageResultDTO fetchAllLanguages()
    {
        return dndClient.fetchAllLanguages(ALL_LANGUAGES_URL);
    }

    @Override
    public List<DNDRaceDetailDTO> fetchAllRacesWithDetails()
    {
        DNDRaceResultDTO resultDTO = dndClient.fetchAllRaces(ALL_RACES_URL);
        List<DNDRaceDetailDTO> detailDTOs = new ArrayList<>();

        resultDTO.races().forEach(race ->
        {
            String url = String.format(BASE_URL, race.url());
            detailDTOs.add(dndClient.fetchRaceDetails(url));
        });
        return detailDTOs;
    }

    @Override
    public List<DNDSubraceDetailDTO> fetchAllSubracesWithDetails()
    {
        DNDSubraceResultDTO resultDTO = dndClient.fetchAllSubraces(ALL_SUBRACES_URL);
        List<DNDSubraceDetailDTO> detailDTOs = new ArrayList<>();

        resultDTO.subraces().forEach(subrace ->
        {
            String url = String.format(BASE_URL, subrace.url());
            detailDTOs.add(dndClient.fetchSubraceDetails(url));
        });
        return detailDTOs;
    }

    @Override
    public List<DNDTraitDetailDTO> fetchAllTraitsWithDetails()
    {
        DNDTraitResultDTO resultDTO = dndClient.fetchAllTraits(ALL_TRAITS_URL);
        List<DNDTraitDetailDTO> detailDTOs = new ArrayList<>();

        resultDTO.traits().forEach(trait ->
        {
            String url = String.format(BASE_URL, trait.url());
            detailDTOs.add(dndClient.fetchTraitDetails(url));
        });
        return detailDTOs;
    }

    @Override
    public List<DNDLanguageDetailDTO> fetchAllLanguagesWithDetails()
    {
        DNDLanguageResultDTO resultDTO = dndClient.fetchAllLanguages(ALL_LANGUAGES_URL);
        List<DNDLanguageDetailDTO> detailDTOs = new ArrayList<>();

        resultDTO.languages().forEach(language ->
        {
            String url = String.format(BASE_URL, language.url());
            detailDTOs.add(dndClient.fetchLanguageDetails(url));
        });
        return detailDTOs;
    }
}
