package app.services.reference.implementations;

import app.dtos.dnd.*;
import app.integrations.IDNDClient;
import app.services.reference.interfaces.IDNDFetchingService;
import app.utils.ThreadUtil;

import java.util.List;
import java.util.concurrent.Callable;

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

        List<Callable<DNDRaceDetailDTO>> tasks = resultDTO.races().stream()
                .map(race -> {
                    String url = String.format(BASE_URL, race.url());
                    return (Callable<DNDRaceDetailDTO>) () -> dndClient.fetchRaceDetails(url);
                })
                .toList();

        return ThreadUtil.fetchConcurrently(tasks);
    }

    @Override
    public List<DNDSubraceDetailDTO> fetchAllSubracesWithDetails()
    {
        DNDSubraceResultDTO resultDTO = dndClient.fetchAllSubraces(ALL_SUBRACES_URL);

        List<Callable<DNDSubraceDetailDTO>> tasks = resultDTO.subraces().stream()
                .map(subrace -> {
                    String url = String.format(BASE_URL, subrace.url());
                    return (Callable<DNDSubraceDetailDTO>) () -> dndClient.fetchSubraceDetails(url);
                })
                .toList();

        return ThreadUtil.fetchConcurrently(tasks);
    }

    @Override
    public List<DNDTraitDetailDTO> fetchAllTraitsWithDetails()
    {
        DNDTraitResultDTO resultDTO = dndClient.fetchAllTraits(ALL_TRAITS_URL);

        List<Callable<DNDTraitDetailDTO>> tasks = resultDTO.traits().stream()
                .map(trait -> {
                    String url = String.format(BASE_URL, trait.url());
                    return (Callable<DNDTraitDetailDTO>) () -> dndClient.fetchTraitDetails(url);
                })
                .toList();

        return ThreadUtil.fetchConcurrently(tasks);
    }

    @Override
    public List<DNDLanguageDetailDTO> fetchAllLanguagesWithDetails()
    {
        DNDLanguageResultDTO resultDTO = dndClient.fetchAllLanguages(ALL_LANGUAGES_URL);

        List<Callable<DNDLanguageDetailDTO>> tasks = resultDTO.languages().stream()
                .map(language -> {
                    String url = String.format(BASE_URL, language.url());
                    return (Callable<DNDLanguageDetailDTO>) () -> dndClient.fetchLanguageDetails(url);
                })
                .toList();

        return ThreadUtil.fetchConcurrently(tasks);
    }
}
