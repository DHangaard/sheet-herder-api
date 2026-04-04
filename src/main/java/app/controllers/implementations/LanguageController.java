package app.controllers.implementations;

import app.controllers.interfaces.IReferenceController;
import app.dtos.dnd.DNDLanguageDetailDTO;
import app.dtos.reference.LanguageDTO;
import app.services.reference.interfaces.IReferenceDataService;
import io.javalin.http.Context;

public class LanguageController implements IReferenceController
{
    private final IReferenceDataService<DNDLanguageDetailDTO, LanguageDTO> languageService;

    public LanguageController(IReferenceDataService<DNDLanguageDetailDTO, LanguageDTO> languageService)
    {
        this.languageService = languageService;
    }

    @Override
    public void getById(Context ctx)
    {
        Long id = Long.parseLong(ctx.pathParam("id"));
        LanguageDTO languageDTO = languageService.getById(id)
                .orElseThrow(() -> new IllegalArgumentException("Language with id " + id + " not found"));
        ctx.status(200).json(languageDTO);
    }

    @Override
    public void getByName(Context ctx)
    {
        String name = ctx.pathParam("name");
        LanguageDTO languageDTO = languageService.getByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Language with name \"" + name + "\" not found"));
        ctx.status(200).json(languageDTO);
    }

    @Override
    public void getAll(Context ctx)
    {
        ctx.status(200).json(languageService.getAll());
    }
}
