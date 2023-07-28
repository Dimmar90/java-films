package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dao.film.GenreDao;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DBGenreService {
    private final GenreDao genreDao;

    public Genre getGenre(Integer id) throws NotFoundException {
        log.info("Get a genre with ID = {}", id);
        return genreDao.findById(id);
    }

    public List<Genre> getAllGenres() {
        log.info("Get all genres");
        return genreDao.findAll();
    }
}
