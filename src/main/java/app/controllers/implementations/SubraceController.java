package app.controllers.implementations;

import app.controllers.interfaces.IReferenceController;
import app.dtos.dnd.DNDSubraceDetailDTO;
import app.dtos.reference.SubraceDTO;
import app.services.reference.interfaces.IReferenceDataService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class SubraceController implements IReferenceController
{
    private final IReferenceDataService<DNDSubraceDetailDTO, SubraceDTO> subraceService;

    public SubraceController(IReferenceDataService<DNDSubraceDetailDTO, SubraceDTO> subraceService)
    {
        this.subraceService = subraceService;
    }

    @Override
    public void getById(Context ctx)
    {
        Long id = Long.parseLong(ctx.pathParam("id"));
        SubraceDTO subraceDTO = subraceService.getById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subrace with id " + id + " not found"));
        ctx.status(HttpStatus.OK).json(subraceDTO);
    }

    @Override
    public void getByName(Context ctx)
    {
        String name = ctx.pathParam("name");
        SubraceDTO subraceDTO = subraceService.getByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Subrace with name \"" + name + "\" not found"));
        ctx.status(HttpStatus.OK).json(subraceDTO);
    }

    @Override
    public void getAll(Context ctx)
    {
        ctx.status(HttpStatus.OK).json(subraceService.getAll());
    }
}
