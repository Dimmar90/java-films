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
public class DBFilmService {
    private final FilmDao filmDao;
    private final DBUserService userService;
    private final GenreDao genreDao;
    private final FilmLikesDao filmLikesDao;
    private final DirectorDao directorDao;
    private final EventDao eventDao;

    @Autowired
    public DBFilmService(@Qualifier("filmDaoImpl") FilmDao filmDao, DBUserService userService,
                         GenreDao genreDao, FilmLikesDao filmLikesDao, DirectorDao directorDao, EventDao eventDao) {
        this.filmDao = filmDao;
        this.userService = userService;
        this.genreDao = genreDao;
        this.filmLikesDao = filmLikesDao;
        this.directorDao = directorDao;
        this.eventDao = eventDao;
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

        addDirectorsToFilm(film);

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

        addDirectorsToFilm(film);

        log.info("Film {} has been UPDATED", film);
        return filmDao.updateFilm(film);
    }

    public void addLike(Integer filmId, Integer userId) {
        filmDao.checkFilmExist(filmId);
        userService.getUser(userId); // метод getUser() выбросит исключение, если userId не существует
        filmLikesDao.like(filmId, userId);
        log.info("Film with ID = {} was LIKED by user with ID = {}", filmId, userId);
        eventDao.addEvent(userId, "LIKE", "ADD", filmId); // добавляю событие в ленту
    }

    public void deleteLike(Integer filmId, Integer userId) {
        filmDao.checkFilmExist(filmId);
        userService.getUser(userId); // метод getUser() выбросит исключение, если userId не существует
        filmLikesDao.unlike(filmId, userId);
        log.info("Film with ID = {} was UNLIKED by user with ID = {}", filmId, userId);
        eventDao.addEvent(userId, "LIKE", "REMOVE", filmId); // удаляю событие из ленты
    }

    public Film getFilm(Integer id) {
        filmDao.checkFilmExist(id);
        Film film = filmDao.getById(id);
        film.setGenres(genreDao.getFilmGenres(film.getId()));
        directorDao.addDirectorsListToFilm(film);
        log.info("Get a film with ID = {}", id);
        return film;
    }

    public List<Film> getFilms() {
        List<Film> films = new ArrayList<>();
        for (Film film : filmDao.getFilms()) {
            film.setGenres(genreDao.getFilmGenres(film.getId()));
            directorDao.addDirectorsListToFilm(film);
            films.add(film);
        }
        return films;
    }

    public List<Film> getDirectorsFilms(Integer directorsId, String sortBy) {
        List<Film> sortedDirectorsFilms = new ArrayList<>();
        if (sortBy.equals("likes")) {
            sortedDirectorsFilms = getSortedListOfFilms(directorsId, filmDao.findDirectorsFilmsSortedByRate(directorsId));
        } else if (sortBy.equals("year")) {
            sortedDirectorsFilms = getSortedListOfFilms(directorsId, filmDao.findDirectorsFilmsSortedByYears(directorsId));
        }
        return sortedDirectorsFilms;
    }

    public List<Film> getSortedListOfFilms(Integer directorsId, List<Film> sortedListOfFilms) {
        directorDao.checkDirectorExist(directorsId);
        List<Film> directorFilms = new ArrayList<>();
        for (Film film : sortedListOfFilms) {
            film.setGenres(genreDao.getFilmGenres(film.getId()));
            directorFilms.add(film);
        }
        return directorFilms;
    }

    public List<Film> getTopFilms(Integer count, Integer genreId, Integer year) {
        log.info("Get {} popular films", count);
        return filmDao.getTopFilms(count, genreId, year);
    }

    public void addDirectorsToFilm(Film film) {
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            directorDao.deleteFilmFromDirector(film.getId());
            film.setDirectors(new HashSet<>());
        } else {
            for (Director director : film.getDirectors()) {
                if (directorDao.checkDirectorExist(director.getId())) {
                    directorDao.addFilmToDirector(director.getId(), film.getId());
                }
            }
            directorDao.addDirectorsListToFilm(film);
        }
    }

    public void deleteFilmById(Integer filmId) {
        filmDao.checkFilmExist(filmId);
        filmDao.deleteFilmById(filmId);
        log.info("Delete film from films with ID = {}", filmId);
    }

    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        userService.getUser(userId); // метод getUser() выбросит исключение, если userId не существует
        userService.getUser(friendId); // метод getUser() выбросит исключение, если friendId не существует
        log.info("Get common films of users with ID = {} and ID = {}", userId, friendId);
        return filmDao.getCommonFilms(userId, friendId);
    }

    public List<Film> search(String keyWord, String whereSearch) {
        return filmDao.search(keyWord, whereSearch);
    }
}
