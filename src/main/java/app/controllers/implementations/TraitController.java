package app.controllers.implementations;

import app.controllers.interfaces.IReferenceController;
import app.dtos.dnd.DNDTraitDetailDTO;
import app.dtos.reference.TraitDTO;
import app.exceptions.NotFoundException;
import app.services.reference.interfaces.IReferenceDataService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

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
                .orElseThrow(() -> new NotFoundException("Trait with id " + id + " not found"));
        ctx.status(HttpStatus.OK).json(traitDTO);
    }

    @Override
    public void getByName(Context ctx)
    {
        String name = ctx.pathParam("name");
        TraitDTO traitDTO = traitService.getByName(name)
                .orElseThrow(() -> new NotFoundException("Trait with name \"" + name + "\" not found"));
        ctx.status(HttpStatus.OK).json(traitDTO);
    }

    @Override
    public void getAll(Context ctx)
    {
        ctx.status(HttpStatus.OK).json(traitService.getAll());
    }
}
