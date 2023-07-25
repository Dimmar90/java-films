package ru.yandex.practicum.filmorate.service.InMemoryService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;

@Service
@Slf4j
public class InMemoryFilmService {
    private final FilmStorage filmStorage;
    private final InMemoryUserService userService;

    @Autowired
    public InMemoryFilmService(@Qualifier("inMemoryFilmStorage") FilmStorage filmStorage,
                               InMemoryUserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Film create(Film film) {
        log.info("Film {} has been CREATED", film);
        return filmStorage.createFilm(film);
    }

    public Film update(Film film) {
        if (film.getId() == null || filmStorage.getById(film.getId()) == null) {
            throw new NotFoundException("Can't update film with ID = null");
        }
        log.info("Film {} has been UPDATED", film);
        return filmStorage.updateFilm(film);
    }

    public void addLike(Integer filmId, Integer userId) {
        Film film = getFilm(filmId);
        userService.getUser(userId); // метод getUser() выбросит исключение, если userId не существует
        if (film.getLikes().contains(userId)) {
            throw new AlreadyExistException(
                    String.format("The user with ID = %d has ALREADY LIKE the film with ID = %d", userId, filmId));
        }

        film.getLikes().add(userId);
        log.info("Film with ID = {} was LIKED by user with ID = {}", filmId, userId);
    }

    public void deleteLike(Integer filmId, Integer userId) {
        Film film = getFilm(filmId);
        userService.getUser(userId); // метод getUser() выбросит исключение, если userId не существует
        if (!film.getLikes().contains(userId)) {
            throw new NotFoundException(
                    String.format("User with ID = %d has ALREADY DELETED LIKE film with ID = %d", userId, filmId));
        }

        film.getLikes().remove(userId);
        log.info("Film with ID = {} was UNLIKED by user with ID = {}", filmId, userId);
    }

    public Film getFilm(Integer id) {
        if (filmStorage.getById(id) == null) {
            throw new NotFoundException(String.format("Film ID = %d does not exist", id));
        }
        log.info("Get a film with ID = {}", id);
        return filmStorage.getById(id);
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public List<Film> getTopFilms(Integer count) {
        log.info("Get {} popular films", count);
        return filmStorage.getTopFilms(count);
    }
}
