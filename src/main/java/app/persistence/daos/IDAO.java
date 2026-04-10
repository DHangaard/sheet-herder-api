package app.persistence.daos;

public interface IDAO<T>
{
    T create(T t);

    T getById(Long id);

    T update(T t);

    Long delete(Long id);
}
