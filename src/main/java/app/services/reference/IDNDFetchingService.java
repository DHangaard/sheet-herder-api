package app.services.reference;

import app.dtos.dnd.*;

import java.util.List;

public interface IDNDFetchingService
{
    DNDRaceResultDTO fetchAllRaces();
    DNDSubraceResultDTO fetchAllSubraces();
    DNDTraitResultDTO fetchAllTraits();
    DNDLanguageResultDTO fetchAllLanguages();

    List<DNDRaceDetailDTO> fetchAllRacesWithDetails();
    List<DNDSubraceDetailDTO> fetchAllSubracesWithDetails();
    List<DNDTraitDetailDTO> fetchAllTraitsWithDetails();
    List<DNDLanguageDetailDTO> fetchAllLanguagesWithDetails();
}
