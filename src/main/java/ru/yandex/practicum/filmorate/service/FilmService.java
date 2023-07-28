package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dao.film.DirectorDao;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmDao;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmLikesDao;
import ru.yandex.practicum.filmorate.storage.dao.film.GenreDao;
import ru.yandex.practicum.filmorate.storage.dao.user.EventDao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
public class FilmService {
    private final FilmDao filmDao;
    private final UserService userService;
    private final GenreDao genreDao;
    private final FilmLikesDao filmLikesDao;
    private final DirectorDao directorDao;
    private final EventDao eventDao;

    @Autowired
    public FilmService(@Qualifier("filmDaoImpl") FilmDao filmDao, UserService userService,
                       GenreDao genreDao, FilmLikesDao filmLikesDao, DirectorDao directorDao, EventDao eventDao) {
        this.filmDao = filmDao;
        this.userService = userService;
        this.genreDao = genreDao;
        this.filmLikesDao = filmLikesDao;
        this.directorDao = directorDao;
        this.eventDao = eventDao;
    }

    public Film create(Film film) {
        Film newFilm = filmDao.create(film);

        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            newFilm.setGenres(new HashSet<>());
            log.info("The film {} has no genres.", film);
        } else {
            for (Genre genre : film.getGenres()) {
                genreDao.add(newFilm.getId(), genre.getId());
            }
            newFilm.setGenres(genreDao.findFilmGenres(film.getId()));
        }

        addDirectorsToFilm(film);

        log.info("Film {} has been CREATED", film);
        return newFilm;
    }

    public Film update(Film film) {
        if (film.getId() == null || !filmDao.checkExist(film.getId())) {
            throw new NotFoundException("Can't update film with ID = null");
        }

        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            genreDao.deleteFilmGenres(film.getId());
            film.setGenres(new HashSet<>());
            log.info("The film {} has no genres.", film);
        } else {
            genreDao.deleteFilmGenres(film.getId());
            for (Genre genre : film.getGenres()) {
                genreDao.add(film.getId(), genre.getId());
            }
            film.setGenres(genreDao.findFilmGenres(film.getId()));
        }

        addDirectorsToFilm(film);

        log.info("Film {} has been UPDATED", film);
        return filmDao.update(film);
    }

    public void addLike(Long filmId, Long userId) {
        filmDao.checkExist(filmId);
        userService.getById(userId); // метод getUser() выбросит исключение, если userId не существует
        filmLikesDao.like(filmId, userId);
        log.info("Film with ID = {} was LIKED by user with ID = {}", filmId, userId);
        eventDao.add(userId, "LIKE", "ADD", filmId); // добавляю событие в ленту
    }

    public void deleteLike(Long filmId, Long userId) {
        filmDao.checkExist(filmId);
        userService.getById(userId); // метод getUser() выбросит исключение, если userId не существует
        filmLikesDao.unlike(filmId, userId);
        log.info("Film with ID = {} was UNLIKED by user with ID = {}", filmId, userId);
        eventDao.add(userId, "LIKE", "REMOVE", filmId); // удаляю событие из ленты
    }

    public Film getById(Long id) {
        filmDao.checkExist(id);
        Film film = filmDao.findById(id);
        film.setGenres(genreDao.findFilmGenres(film.getId()));
        directorDao.addDirectorsToFilm(film);
        log.info("Get a film with ID = {}", id);
        return film;
    }

    public List<Film> getAll() {
        List<Film> films = new ArrayList<>();
        for (Film film : filmDao.findAll()) {
            film.setGenres(genreDao.findFilmGenres(film.getId()));
            directorDao.addDirectorsToFilm(film);
            films.add(film);
        }
        return films;
    }

    public List<Film> getDirectorsFilms(Long directorsId, String sortBy) {
        List<Film> sortedDirectorsFilms = new ArrayList<>();
        if (sortBy.equals("likes")) {
            sortedDirectorsFilms = getSortedBy(directorsId, filmDao.findDirectorsFilmsSortedByRate(directorsId));
        } else if (sortBy.equals("year")) {
            sortedDirectorsFilms = getSortedBy(directorsId, filmDao.findDirectorsFilmsSortedByYears(directorsId));
        }
        return sortedDirectorsFilms;
    }

    public List<Film> getSortedBy(Long directorsId, List<Film> sortedListOfFilms) {
        directorDao.checkExist(directorsId);
        List<Film> directorFilms = new ArrayList<>();
        for (Film film : sortedListOfFilms) {
            film.setGenres(genreDao.findFilmGenres(film.getId()));
            directorFilms.add(film);
        }
        return directorFilms;
    }

    public List<Film> getTop(Integer count, Integer genreId, Integer year) {
        log.info("Get {} popular films", count);
        return filmDao.findTop(count, genreId, year);
    }

    public void addDirectorsToFilm(Film film) {
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            directorDao.deleteFromFilm(film.getId());
            film.setDirectors(new HashSet<>());
        } else {
            for (Director director : film.getDirectors()) {
                if (directorDao.checkExist(director.getId())) {
                    directorDao.addToFilm(director.getId(), film.getId());
                }
            }
            directorDao.addDirectorsToFilm(film);
        }
    }

    public void delete(Long filmId) {
        filmDao.checkExist(filmId);
        filmDao.delete(filmId);
        log.info("Delete film from films with ID = {}", filmId);
    }

    public List<Film> getCommon(Long userId, Long friendId) {
        userService.getById(userId); // метод getUser() выбросит исключение, если userId не существует
        userService.getById(friendId); // метод getUser() выбросит исключение, если friendId не существует
        log.info("Get common films of users with ID = {} and ID = {}", userId, friendId);
        return filmDao.findCommon(userId, friendId);
    }

    public List<Film> search(String keyWord, String whereSearch) {
        return filmDao.search(keyWord, whereSearch);
    }
}
