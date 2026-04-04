package app.controllers.implementations;

import app.controllers.interfaces.IReferenceController;
import app.dtos.dnd.DNDTraitDetailDTO;
import app.dtos.reference.TraitDTO;
import app.services.reference.interfaces.IReferenceDataService;
import io.javalin.http.Context;

public class TraitController implements IReferenceController
{
    private final IReferenceDataService<DNDTraitDetailDTO, TraitDTO> traitService;

    public TraitController(IReferenceDataService<DNDTraitDetailDTO, TraitDTO> traitService)
    {
        this.traitService = traitService;
    }

    @Override
    public void getById(Context ctx)
    {
        Long id = Long.parseLong(ctx.pathParam("id"));
        TraitDTO traitDTO = traitService.getById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trait with id " + id + " not found"));
        ctx.status(200).json(traitDTO);
    }

    @Override
    public void getByName(Context ctx)
    {
        String name = ctx.pathParam("name");
        TraitDTO traitDTO = traitService.getByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Trait with name \"" + name + "\" not found"));
        ctx.status(200).json(traitDTO);
    }

    @Override
    public void getAll(Context ctx)
    {
        ctx.status(200).json(traitService.getAll());
    }
}
