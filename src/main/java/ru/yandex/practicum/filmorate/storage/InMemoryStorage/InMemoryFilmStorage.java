package ru.yandex.practicum.filmorate.storage.InMemoryStorage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Component("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private final HashMap<Integer, Film> films = new HashMap<>();
    private Integer filmId = 1;

    @Override
    public Film create(Film film) {
        film.setId(filmId++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film findById(Integer id) {
        return films.get(id);
    }

    @Override
    public List<Film> findTop(Integer count) {
        return findAll().stream()
                .sorted(Collections.reverseOrder(Comparator.comparingInt(film -> film.getLikes().size())))
                .limit(count)
                .collect(Collectors.toList());
    }
}
