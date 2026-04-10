package app.services.reference.interfaces;

import java.util.List;
import java.util.Optional;

public interface IReferenceDataService<T, R>
{
    List<R> persistAll(List<T> dtos);

    Optional<R> getById(Long id);

    Optional<R> getByName(String name);

    List<R> getAll();

    Long delete(Long id);
}
