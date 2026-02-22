package app.daos;

import java.util.List;


public interface IDAO <T>
{
    T create(T t);
    T getById(Long id);
    List<T> getAll();
    T update(T t);
    Long delete(Long id);
}
