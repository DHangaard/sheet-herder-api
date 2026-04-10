package app.persistence.daos.reference.interfaces;

import app.persistence.daos.IDAO;

import java.util.Collection;
import java.util.List;

public interface IReferenceDAO<T> extends IDAO<T>
{
    T getByName(String name);

    List<T> getByNames(Collection<String> names);

    List<T> getAll();

    List<T> syncAll(List<T> entities);
}
