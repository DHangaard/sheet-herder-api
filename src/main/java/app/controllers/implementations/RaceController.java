package app.controllers.implementations;

import app.controllers.interfaces.IReferenceController;
import app.dtos.dnd.DNDRaceDetailDTO;
import app.dtos.reference.RaceDTO;
import app.exceptions.NotFoundException;
import app.services.reference.interfaces.IReferenceDataService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class RaceController implements IReferenceController
{
    private final IReferenceDataService<DNDRaceDetailDTO, RaceDTO>  raceService;

    public RaceController(IReferenceDataService<DNDRaceDetailDTO, RaceDTO>  raceService)
    {
        this.raceService = raceService;
    }

    @Override
    public void getById(Context ctx)
    {
        Long id = Long.parseLong(ctx.pathParam("id"));
        RaceDTO raceDTO = raceService.getById(id)
                .orElseThrow(() -> new NotFoundException("Race with id " + id + " not found"));
        ctx.status(HttpStatus.OK).json(raceDTO);
    }

    @Override
    public void getByName(Context ctx)
    {
        String name = ctx.pathParam("name");
        RaceDTO raceDTO = raceService.getByName(name)
                .orElseThrow(() -> new NotFoundException("Race with name \"" + name + "\" not found"));
        ctx.status(HttpStatus.OK).json(raceDTO);
    }

    @Override
    public void getAll(Context ctx)
    {
        ctx.status(HttpStatus.OK).json(raceService.getAll());
    }
}
