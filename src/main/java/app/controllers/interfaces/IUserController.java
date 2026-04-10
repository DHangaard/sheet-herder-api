package app.controllers.interfaces;

import io.javalin.http.Context;

public interface IUserController
{
    void update(Context ctx);

    void delete(Context ctx);
}
