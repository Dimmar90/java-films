package ru.yandex.practicum.filmorate.storage.dao.film;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

public interface MpaDao {
    Mpa findById(Integer id);

    List<Mpa> findAll();
}
