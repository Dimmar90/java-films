package ru.yandex.practicum.filmorate.storage.dao.film;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorDao {

    Director createDirector(Director director);

    Director updateDirector(Director director);

    List<Director> findAllDirectors();

    Director findDirectorById(Integer id);

    void deleteDirector(Integer id);

    boolean checkDirectorExist(Integer id);

}
