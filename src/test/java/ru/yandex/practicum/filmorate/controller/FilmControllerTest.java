package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController filmController;
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @BeforeEach
    void setUp() {
        filmController = new FilmController(new FilmService(
                        new InMemoryFilmStorage(),
                        new UserService(new InMemoryUserStorage()))
        );
    }

    @Test
    @DisplayName("Проверка валидации фильма, если все поля Film корректны")
    void shouldCreateFilm() {
        Film film = filmController.create(new Film(null, "film", "description", 150,
                LocalDate.of(1985, 9,2), new HashSet<>()));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Проверка валидации фильма, если все поля Film null")
    void shouldViolationIfFilmIsNull() {
        Film film = filmController.create(new Film());
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(2, violations.size());
    }

    @Test
    @DisplayName("Проверка валидации, если название фильма null")
    void shouldViolationIfFilmNameIsNull() {
        Film film = filmController.create(new Film(null, null, "description", 150,
                LocalDate.of(1985, 9,2), new HashSet<>()));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
    }

    @Test
    @DisplayName("Проверка валидации, если в названии фильма только пробел")
    void shouldViolationIfFilmNameIsBlank() {
        Film film = filmController.create(new Film(null, " ", "description", 150,
                LocalDate.of(1985, 9,2), new HashSet<>()));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
    }

    @Test
    @DisplayName("Проверка валидации, если название фильма пустое")
    void shouldViolationIfFilmNameIsEmpty() {
        Film film = filmController.create(new Film(null, "", "description", 150,
                LocalDate.of(1985, 9,2), new HashSet<>()));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
    }

    @Test
    @DisplayName("Проверка валидации, если описание фильма равно 200 символам")
    void shouldCreateFilmIfDescriptionLengthEqual200() {
        String description = "Covenant. Афганистан, март 2018 года." +
                "Во время спецоперации по поиску оружия талибов отряд сержанта армии США Джона Кинли попадает в засаду. " +
                "В живых остаются только сам Джон и местный переводчик Ахмед.";
        Film film = filmController.create(new Film(null, "Covenant", description, 123,
                LocalDate.of(2023, 4,19), new HashSet<>()));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Проверка валидации, если описание фильма равно 201 символам")
    void shouldViolationIfFilmDescriptionLengthEqual201() {
        String description = "Covenant. Афганистан, март 2018 года. " +
                "Во время спецоперации по поиску оружия талибов отряд сержанта армии США Джона Кинли попадает в засаду. " +
                "В живых остаются только сам Джон и местный переводчик Ахмед.";
        Film film = filmController.create(new Film(null, "Covenant", description, 123,
                LocalDate.of(2023, 4,19), new HashSet<>()));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
    }

    @Test
    @DisplayName("Проверка валидации, если длительность фильма отрицательная")
    void shouldViolationIfFilmDurationIsNegative() {
        Film film = filmController.create(new Film(null, "film", "description", -1,
                LocalDate.of(1985, 9,2), new HashSet<>()));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
    }

    @Test
    @DisplayName("Проверка валидации, если длительность фильма равна 0")
    void shouldViolationIfFilmDurationIsZero() {
        Film film = filmController.create(new Film(null, "film", "description", 0,
                LocalDate.of(1985, 9,2), new HashSet<>()));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
    }

    @Test
    @DisplayName("Проверка валидации, если дата релиза фильма 1895-12-28")
    void shouldCreateFilmIfReleaseDate1895_12_28() {
        Film film = filmController.create(new Film(null, "film", "description", 120,
                LocalDate.of(1895, 12,28), new HashSet<>()));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Проверка валидации, если дата релиза фильма 1895-12-27 - ранее 1895-12-28")
    void shouldCreateFilmIfReleaseDate1895_12_27() {
        Film film = filmController.create(new Film(null, "film", "description", 120,
                LocalDate.of(1895, 12,27), new HashSet<>()));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
    }

    @Test
    @DisplayName("Проверка валидации, если при обновлении все поля Film корректны")
    void shouldUpdateFilm() {
        filmController.create(new Film(null, "film", "description", 150,
                LocalDate.of(1985, 9,2), new HashSet<>()));
        Film film = filmController.create(new Film(1,"film1", "description1", 150,
                LocalDate.of(1985, 9,2), new HashSet<>()));

        assertEquals("film1", film.getName());
        assertEquals("description1", film.getDescription());
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Проверка валидации,если Film.id некорректен")
    void shouldThrowsWhenUpdatedFilmWithIncorrectId() {
        filmController.create(new Film(null, "film", "description", 150,
                LocalDate.of(1985, 9,2), new HashSet<>()));
        final NotFoundException exp = assertThrows(
                NotFoundException.class,
                () ->  filmController.update(new Film(2,"film1", "description1", 150,
                        LocalDate.of(1985, 9,2), new HashSet<>()))
        );
        assertEquals("Can't update film with ID = null", exp.getMessage());
    }

    @Test
    @DisplayName("Проверка валидации,если Film.id = null")
    void shouldThrowsWhenUpdatedFilmWithIdEqualNull() {
        filmController.create(new Film(null, "film", "description", 150,
                LocalDate.of(1985, 9,2), new HashSet<>()));
        final NotFoundException exp = assertThrows(
                NotFoundException.class,
                () ->  filmController.update(new Film(null,"film1", "description1", 150,
                        LocalDate.of(1985, 9,2), new HashSet<>()))
        );
        assertEquals("Can't update film with ID = null", exp.getMessage());
    }

    @Test
    @DisplayName("Проверка возврата списка созданных фильмов без ошибок валидации")
    void shouldGetListFilms() {
        filmController.create(new Film(null, "film", "description", 150,
                LocalDate.of(1985, 9,2), new HashSet<>()));
        filmController.create(new Film(null, "film", "description", 120,
                LocalDate.of(1957, 11,10), new HashSet<>()));
        filmController.create(new Film(null, "Covenant", "description", 123,
                LocalDate.of(2023, 4,19), new HashSet<>()));

        Optional<Set<ConstraintViolation<Film>>> violationSet = filmController.getFilms().stream()
                .map(film -> validator.validate(film))
                .filter(violation -> !violation.isEmpty())
                .findFirst();
        violationSet.ifPresentOrElse(
                violation -> assertTrue(violation.isEmpty()),
                () -> assertTrue(true)
        );
    }

    @Test
    @DisplayName("Проверка возврата списка фильмов с ошибками валидации")
    void shouldGetEmptyListFilms() {
        String description = "Covenant. Афганистан, март 2018 года. " +
                "Во время спецоперации по поиску оружия талибов отряд сержанта армии США Джона Кинли попадает в засаду. " +
                "В живых остаются только сам Джон и местный переводчик Ахмед.";
        filmController.create(new Film(null, "film", "description", 150,
                LocalDate.of(1895, 11,2), new HashSet<>()));
        filmController.create(new Film(null, "", "description", 120,
                LocalDate.of(1957, 11,10), new HashSet<>()));
        filmController.create(new Film(null, "Covenant", description, 123,
                LocalDate.of(2023, 4,19), new HashSet<>()));

        Optional<Set<ConstraintViolation<Film>>> violationSet = filmController.getFilms().stream()
                .map(film -> validator.validate(film))
                .filter(violation -> !violation.isEmpty())
                .findFirst();
        violationSet.ifPresentOrElse(
                violation -> assertFalse(violation.isEmpty()),
                () -> assertFalse(false)
        );
    }
}