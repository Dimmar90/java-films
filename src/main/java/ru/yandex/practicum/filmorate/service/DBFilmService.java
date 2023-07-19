package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmDao;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmLikesDao;
import ru.yandex.practicum.filmorate.storage.dao.film.GenreDao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
public class DBFilmService {
    private final FilmDao filmDao;
    private final DBUserService userService;
    private final GenreDao genreDao;
    private final FilmLikesDao filmLikesDao;

    @Autowired
    public DBFilmService(@Qualifier("filmDaoImpl") FilmDao filmDao, DBUserService userService,
                         GenreDao genreDao, FilmLikesDao filmLikesDao) {
        this.filmDao = filmDao;
        this.userService = userService;
        this.genreDao = genreDao;
        this.filmLikesDao = filmLikesDao;
    }

    public Film create(Film film) {
        Film newFilm = filmDao.createFilm(film);
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            newFilm.setGenres(new HashSet<>());
            log.info("The film {} has no genres.", film);
        } else {
            for (Genre genre : film.getGenres()) {
                genreDao.addGenreInFilm(newFilm.getId(), genre.getId());
            }
            newFilm.setGenres(genreDao.getFilmGenres(film.getId()));
        }
        log.info("Film {} has been CREATED", film);
        return newFilm;
    }

    public Film update(Film film) {
        if (film.getId() == null || !filmDao.checkFilmExist(film.getId())) {
            throw new NotFoundException("Can't update film with ID = null");
        }

        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            genreDao.deleteFilmGenre(film.getId());
            film.setGenres(new HashSet<>());
            log.info("The film {} has no genres.", film);
        } else {
            genreDao.deleteFilmGenre(film.getId());
            for (Genre genre : film.getGenres()) {
                genreDao.addGenreInFilm(film.getId(), genre.getId());
            }
            film.setGenres(genreDao.getFilmGenres(film.getId()));
        }
        log.info("Film {} has been UPDATED", film);
        return filmDao.updateFilm(film);
    }

    public void addLike(Integer filmId, Integer userId) {
        filmDao.checkFilmExist(filmId);
        userService.getUser(userId); // метод getUser() выбросит исключение, если userId не существует
        filmLikesDao.like(filmId, userId);
        log.info("Film with ID = {} was LIKED by user with ID = {}", filmId, userId);
    }

    public void deleteLike(Integer filmId, Integer userId) {
        filmDao.checkFilmExist(filmId);
        userService.getUser(userId); // метод getUser() выбросит исключение, если userId не существует
        filmLikesDao.unlike(filmId, userId);
        log.info("Film with ID = {} was UNLIKED by user with ID = {}", filmId, userId);
    }

    public Film getFilm(Integer id) {
        filmDao.checkFilmExist(id);
        Film film = filmDao.getById(id);
        film.setGenres(genreDao.getFilmGenres(film.getId()));
        log.info("Get a film with ID = {}", id);
        return film;
    }

    public List<Film> getFilms() {
        List<Film> films = new ArrayList<>();
        for (Film film : filmDao.getFilms()) {
            film.setGenres(genreDao.getFilmGenres(film.getId()));
            films.add(film);
        }
        return films;
    }

    public List<Film> getTopFilms(Integer count) {
        log.info("Get {} popular films", count);
        return filmDao.getTopFilms(count);
    }

    public void deleteFilmById(Integer filmId) {
        filmDao.checkFilmExist(filmId);
        filmDao.deleteFilmById(filmId);
        log.info("Delete film from films with ID = {}", filmId);
    }
}
