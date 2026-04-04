package app.controllers.interfaces;

import io.javalin.http.Context;

public interface IReferenceController
{
    void getById(Context ctx);

    void getByName(Context ctx);

    void getAll(Context ctx);
}
