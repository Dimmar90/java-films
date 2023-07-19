package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmDao;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmLikesDao;
import ru.yandex.practicum.filmorate.storage.dao.film.GenreDao;
import ru.yandex.practicum.filmorate.storage.dao.film.MpaDao;
import ru.yandex.practicum.filmorate.storage.dao.user.UserDao;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmDaoImplTest {
    private final FilmDao filmStorage;
    private final UserDao userStorage;
    private final GenreDao genreStorage;
    private final MpaDao mpaStorage;
    private final FilmLikesDao likesStorage;
    private Film film;
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @BeforeEach
    void setUp() {
        film = Film.builder()
                .name("some film")
                .description("description some film")
                .duration(180)
                .releaseDate(LocalDate.of(2005, 6, 5))
                .mpa(new Mpa(1, "G"))
                .genres(new HashSet<>())
                .build();
    }

    @Test
    void shouldCreateFilm() {
        Film newFilm = filmStorage.createFilm(film);
        Set<ConstraintViolation<Film>> violations = validator.validate(newFilm);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldUpdateFilm() {
        filmStorage.createFilm(film);
        filmStorage.updateFilm(
                Film.builder()
                        .id(1)
                        .name("some newFilm")
                        .description("new description some film")
                        .duration(150)
                        .releaseDate(LocalDate.of(2005, 6, 5))
                        .mpa(new Mpa(1, "G"))
                        .genres(new HashSet<>())
                        .build()
        );

        assertEquals("some newFilm", filmStorage.getById(1).getName());
        assertEquals("new description some film", filmStorage.getById(1).getDescription());
        Set<ConstraintViolation<Film>> violations = validator.validate(filmStorage.getById(1));
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldGetListFilmsWithoutViolations() {
        filmStorage.createFilm(film);
        filmStorage.createFilm(
                Film.builder()
                        .name("some newFilm")
                        .description("new description some film")
                        .duration(150)
                        .releaseDate(LocalDate.of(2001, 3, 8))
                        .mpa(new Mpa(1, "G"))
                        .genres(new HashSet<>())
                        .build()
        );

        Optional<Set<ConstraintViolation<Film>>> violationSet = filmStorage.getFilms().stream()
                .map(film -> validator.validate(film))
                .filter(violation -> !violation.isEmpty())
                .findFirst();
        violationSet.ifPresentOrElse(
                violation -> assertTrue(violation.isEmpty()),
                () -> assertTrue(true)
        );
    }

    @Test
    void shouldGetFilmById() {
        filmStorage.createFilm(film);
        assertEquals(film, filmStorage.getById(1));
    }

    @Test
    void shouldGetTopFilms() {
        User user = userStorage.createUser(
                User.builder()
                        .email("user1@gmail.com")
                        .login("alex")
                        .name("Alex")
                        .birthday(LocalDate.of(1980, 5, 25))
                        .build());

        Film film1 = filmStorage.createFilm(film);
        Film film2 = filmStorage.createFilm(
                Film.builder()
                        .name("some newFilm")
                        .description("new description some film")
                        .duration(150)
                        .releaseDate(LocalDate.of(2001, 3, 8))
                        .mpa(new Mpa(1, "G"))
                        .genres(new HashSet<>())
                        .build()
        );

        likesStorage.like(film2.getId(), user.getId());
        List<Film> topFilms = List.of(film2, film1);
        assertEquals(topFilms, filmStorage.getTopFilms(2));
    }

    @Test
    void shouldThrowsInvalidCheckCheckFilmExist() {
        filmStorage.createFilm(film);

        final NotFoundException e = assertThrows(
                NotFoundException.class,
                () -> filmStorage.checkFilmExist(2)
        );
        assertEquals("Film ID = 2 does not exist", e.getMessage());
    }

    @Test
    void shouldGetMpaById() {
        assertEquals("R", mpaStorage.getMpa(4).getName());
    }

    @Test
    void shouldGetListAllMpa() {
        List<Mpa> listMpa = List.of(
                new Mpa(1, "G"),
                new Mpa(2, "PG"),
                new Mpa(3, "PG-13"),
                new Mpa(4, "R"),
                new Mpa(5, "NC-17")
        );

        assertEquals(listMpa, mpaStorage.getAllMpa());
    }

    @Test
    void shouldThrowsIfLikedTwice() {
        Film film1 = filmStorage.createFilm(film);
        User user = userStorage.createUser(
                User.builder()
                        .email("user1@gmail.com")
                        .login("alex")
                        .name("Alex")
                        .birthday(LocalDate.of(1980, 5, 25))
                        .build());
        likesStorage.like(film1.getId(), user.getId());

        assertThrows(RuntimeException.class, () -> likesStorage.like(film1.getId(), user.getId()));
    }

    @Test
    void shouldThrowsIfUnlikedTwice() {
        Film film1 = filmStorage.createFilm(film);
        User user = userStorage.createUser(
                User.builder()
                        .email("user1@gmail.com")
                        .login("alex")
                        .name("Alex")
                        .birthday(LocalDate.of(1980, 5, 25))
                        .build());

        Film film2 = filmStorage.createFilm(
                Film.builder()
                        .name("some newFilm")
                        .description("new description some film")
                        .duration(150)
                        .releaseDate(LocalDate.of(2001, 3, 8))
                        .mpa(new Mpa(1, "G"))
                        .genres(new HashSet<>())
                        .build()
        );

        List<Film> topFilms = List.of(film2, film1);
        likesStorage.like(film2.getId(), user.getId());
        assertEquals(topFilms, filmStorage.getTopFilms(2));
        likesStorage.unlike(film2.getId(), user.getId());
        assertNotEquals(topFilms, filmStorage.getTopFilms(2));
    }

    @Test
    void shouldGetGenreById() {
        assertEquals("Триллер", genreStorage.getGenre(4).getName());
    }

    @Test
    void shouldGetListGenres() {
        List<Genre> listGenres = List.of(
                new Genre(1, "Комедия"),
                new Genre(2, "Драма"),
                new Genre(3, "Мультфильм"),
                new Genre(4, "Триллер"),
                new Genre(5, "Документальный"),
                new Genre(6, "Боевик")
        );

        assertEquals(listGenres, genreStorage.getAllGenres());
    }

    @Test
    void shouldAddGenreInFilm() {
        filmStorage.createFilm(film);
        genreStorage.addGenreInFilm(film.getId(), 6);

        assertTrue(filmStorage.getById(film.getId())
                .getGenres()
                .contains(genreStorage.getGenre(6)));
    }

    @Test
    void shouldGetFilmGenres() {
        filmStorage.createFilm(film);
        genreStorage.addGenreInFilm(film.getId(), 6);
        genreStorage.addGenreInFilm(film.getId(), 2);

        Set<Genre> genres = Set.of(
                genreStorage.getGenre(6),
                genreStorage.getGenre(2)
        );
        assertEquals(genres, genreStorage.getFilmGenres(film.getId()));
    }

    @Test
    void shouldDeleteGenresFromFilm() {
        filmStorage.createFilm(film);
        genreStorage.addGenreInFilm(film.getId(), 2);
        genreStorage.addGenreInFilm(film.getId(), 6);

        assertFalse(filmStorage.getById(film.getId())
                .getGenres()
                .isEmpty());

        genreStorage.deleteFilmGenre(film.getId());

        assertTrue(filmStorage.getById(film.getId())
                .getGenres()
                .isEmpty());
    }

    @Test
    void shouldDeleteFilmById() {
        filmStorage.createFilm(film);
        List<Film> expectedFilms = filmStorage.getFilms();
        film.setId(1);
        genreStorage.addGenreInFilm(film.getId(), 2);

        assertThat(expectedFilms).hasSize(1).contains(film);
        filmStorage.deleteFilmById(film.getId());
        assertThat(filmStorage.getFilms()).hasSize(0);
        assertThat(genreStorage.getFilmGenres(film.getId())).hasSize(0);
    }
}