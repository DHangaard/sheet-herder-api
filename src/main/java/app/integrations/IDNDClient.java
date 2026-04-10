package app.integrations;

import app.dtos.dnd.*;

public interface IDNDClient
{
    DNDRaceResultDTO fetchAllRaces(String url);

    DNDSubraceResultDTO fetchAllSubraces(String url);

    DNDTraitResultDTO fetchAllTraits(String url);

    DNDLanguageResultDTO fetchAllLanguages(String url);

    DNDRaceDetailDTO fetchRaceDetails(String url);

    DNDSubraceDetailDTO fetchSubraceDetails(String url);

    DNDTraitDetailDTO fetchTraitDetails(String url);

    DNDLanguageDetailDTO fetchLanguageDetails(String url);
}
