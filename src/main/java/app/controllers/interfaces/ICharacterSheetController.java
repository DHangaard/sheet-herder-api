package app.controllers.interfaces;

import io.javalin.http.Context;

public interface ICharacterSheetController
{
    void create(Context ctx);

    void getById(Context ctx);

    void update(Context ctx);

    void delete(Context ctx);

    void getAllByUser(Context ctx);
}
