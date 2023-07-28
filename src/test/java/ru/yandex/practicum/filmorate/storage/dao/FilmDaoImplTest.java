package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmDao;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmLikesDao;
import ru.yandex.practicum.filmorate.storage.dao.film.GenreDao;
import ru.yandex.practicum.filmorate.storage.dao.film.MpaDao;
import ru.yandex.practicum.filmorate.storage.dao.review.ReviewDao;
import ru.yandex.practicum.filmorate.storage.dao.review.ReviewDislikeDao;
import ru.yandex.practicum.filmorate.storage.dao.review.ReviewLikeDao;
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
    private final ReviewDao reviewStorage;
    private final ReviewLikeDao reviewLikeStorage;
    private final ReviewDislikeDao reviewDislikeStorage;
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
                .directors(new HashSet<>())
                .genres(new HashSet<>())
                .directors(new HashSet<>())
                .build();
    }

    @Test
    void shouldCreateFilm() {
        Film newFilm = filmStorage.create(film);
        Set<ConstraintViolation<Film>> violations = validator.validate(newFilm);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldUpdateFilm() {
        filmStorage.create(film);
        filmStorage.update(
                Film.builder()
                        .id(1L)
                        .name("some newFilm")
                        .description("new description some film")
                        .duration(150)
                        .releaseDate(LocalDate.of(2005, 6, 5))
                        .mpa(new Mpa(1, "G"))
                        .directors(new HashSet<>())
                        .genres(new HashSet<>())
                        .build()
        );

        assertEquals("some newFilm", filmStorage.findById(1L).getName());
        assertEquals("new description some film", filmStorage.findById(1L).getDescription());
        Set<ConstraintViolation<Film>> violations = validator.validate(filmStorage.findById(1L));
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldGetListFilmsWithoutViolations() {
        filmStorage.create(film);
        filmStorage.create(
                Film.builder()
                        .name("some newFilm")
                        .description("new description some film")
                        .duration(150)
                        .releaseDate(LocalDate.of(2001, 3, 8))
                        .mpa(new Mpa(1, "G"))
                        .directors(new HashSet<>())
                        .genres(new HashSet<>())
                        .build()
        );

        Optional<Set<ConstraintViolation<Film>>> violationSet = filmStorage.findAll().stream()
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
        filmStorage.create(film);
        assertEquals(film, filmStorage.findById(1L));
    }

    @Test
    void shouldGetTopFilms() {
        User user = userStorage.create(
                User.builder()
                        .email("user1@gmail.com")
                        .login("alex")
                        .name("Alex")
                        .birthday(LocalDate.of(1980, 5, 25))
                        .build());

        Film film1 = filmStorage.create(film);
        Film film2 = filmStorage.create(
                Film.builder()
                        .name("some newFilm")
                        .description("new description some film")
                        .duration(150)
                        .releaseDate(LocalDate.of(2001, 3, 8))
                        .mpa(new Mpa(1, "G"))
                        .genres(new HashSet<>())
                        .directors(new HashSet<>())
                        .build());

        likesStorage.like(film2.getId(), user.getId());
        List<Film> topFilms = List.of(film2, film1);
        assertEquals(topFilms, filmStorage.findTop(2));
    }

    @Test
    void shouldGetCommonFilms() {
        User user1 = userStorage.create(
                User.builder()
                        .email("user1@gmail.com")
                        .login("alex")
                        .name("Alex")
                        .birthday(LocalDate.of(1980, 5, 25))
                        .build());

        User user2 = userStorage.create(
                User.builder()
                        .email("user2@gmail.com")
                        .login("john")
                        .name("Johny")
                        .birthday(LocalDate.of(1989, 7, 4))
                        .build());

        User user3 = userStorage.create(
                User.builder()
                        .email("user3@gmail.com")
                        .login("ivan")
                        .name("vano")
                        .birthday(LocalDate.of(1988, 8, 17))
                        .build());

        Film film1 = filmStorage.create(film);
        Film film2 = filmStorage.create(
                Film.builder()
                        .name("Avatar")
                        .description("description about Avatar")
                        .duration(180)
                        .releaseDate(LocalDate.of(2009, 12, 10))
                        .mpa(new Mpa(2, "PG"))
                        .directors(new HashSet<>())
                        .genres(new HashSet<>())
                        .build()
        );

        Film film3 = filmStorage.create(
                Film.builder()
                        .name("Kill Bill")
                        .description("description about Kill Bill")
                        .duration(130)
                        .releaseDate(LocalDate.of(2003, 10, 8))
                        .mpa(new Mpa(1, "G"))
                        .directors(new HashSet<>())
                        .genres(new HashSet<>())
                        .build()
        );

        likesStorage.like(film1.getId(), user2.getId());
        likesStorage.like(film1.getId(), user3.getId());

        likesStorage.like(film2.getId(), user1.getId());
        likesStorage.like(film2.getId(), user2.getId());

        likesStorage.like(film3.getId(), user1.getId());
        likesStorage.like(film3.getId(), user2.getId());
        likesStorage.like(film3.getId(), user3.getId());

        List<Film> topFilms = List.of(film3, film2); // Ожидаемый порядок списка общих фильмов
        assertEquals(topFilms, filmStorage.findCommon(user1.getId(), user2.getId()));
        assertEquals(topFilms, filmStorage.findCommon(user2.getId(), user1.getId()));
    }

    @Test
    void shouldThrowsInvalidCheckFilmExist() {
        filmStorage.create(film);

        final NotFoundException e = assertThrows(
                NotFoundException.class,
                () -> filmStorage.checkExist(2L)
        );
        assertEquals("Film ID = 2 does not exist", e.getMessage());
    }

    @Test
    void shouldGetMpaById() {
        assertEquals("R", mpaStorage.findById(4).getName());
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

        assertEquals(listMpa, mpaStorage.findAll());
    }

    @Test
    void shouldNotThrowsIfLikedTwice() {
        Film film1 = filmStorage.create(film);
        User user = userStorage.create(
                User.builder()
                        .email("user1@gmail.com")
                        .login("alex")
                        .name("Alex")
                        .birthday(LocalDate.of(1980, 5, 25))
                        .build());
        likesStorage.like(film1.getId(), user.getId());

        assertDoesNotThrow(() -> likesStorage.like(film1.getId(), user.getId()));
    }

    @Test
    void shouldThrowsIfUnlikedTwice() {
        Film film1 = filmStorage.create(film);
        User user = userStorage.create(
                User.builder()
                        .email("user1@gmail.com")
                        .login("alex")
                        .name("Alex")
                        .birthday(LocalDate.of(1980, 5, 25))
                        .build());

        Film film2 = filmStorage.create(
                Film.builder()
                        .name("some newFilm")
                        .description("new description some film")
                        .duration(150)
                        .releaseDate(LocalDate.of(2001, 3, 8))
                        .mpa(new Mpa(1, "G"))
                        .directors(new HashSet<>())
                        .genres(new HashSet<>())
                        .build()
        );

        film1.setDirectors(new HashSet<>());
        film2.setDirectors(new HashSet<>());
        List<Film> topFilms = List.of(film2, film1);
        likesStorage.like(film2.getId(), user.getId());
        assertEquals(topFilms, filmStorage.findTop(2, null, null));
        likesStorage.unlike(film2.getId(), user.getId());
        assertNotEquals(topFilms, filmStorage.findTop(2, null, null));
    }

    @Test
    void shouldGetGenreById() {
        assertEquals("Триллер", genreStorage.findById(4L).getName());
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

        assertEquals(listGenres, genreStorage.findAll());
    }

    @Test
    void shouldAddGenreInFilm() {
        filmStorage.create(film);
        genreStorage.add(film.getId(), 6);

        assertTrue(filmStorage.findById(film.getId())
                .getGenres()
                .contains(genreStorage.findById(6L)));
    }

    @Test
    void shouldGetFilmGenres() {
        filmStorage.create(film);
        genreStorage.add(film.getId(), 6);
        genreStorage.add(film.getId(), 2);

        Set<Genre> genres = Set.of(
                genreStorage.findById(6L),
                genreStorage.findById(2L)
        );
        assertEquals(genres, genreStorage.findFilmGenres(film.getId()));
    }

    @Test
    void shouldDeleteGenresFromFilm() {
        filmStorage.create(film);
        genreStorage.add(film.getId(), 2);
        genreStorage.add(film.getId(), 6);

        assertFalse(filmStorage.findById(film.getId())
                .getGenres()
                .isEmpty());

        genreStorage.deleteFilmGenres(film.getId());

        assertTrue(filmStorage.findById(film.getId())
                .getGenres()
                .isEmpty());
    }

    @Test
    void shouldDeleteFilmById() {
        filmStorage.create(film);
        List<Film> expectedFilms = filmStorage.findAll();
        film.setId(1L);
        film.setDirectors(new HashSet<>());
        genreStorage.add(film.getId(), 2);

        assertThat(expectedFilms).hasSize(1).contains(film);
        filmStorage.delete(film.getId());
        assertThat(filmStorage.findAll()).hasSize(0);
        assertThat(genreStorage.findFilmGenres(film.getId())).hasSize(0);
    }

    @Test
    void shouldGetEmptyListReviews() {
        assertTrue(reviewStorage.findAll(null, 10).isEmpty());
    }

    @Test
    void shouldGetEmptyListReviewsForFilm() {
        filmStorage.create(film);
        assertTrue(reviewStorage.findAll(film.getId(), 10).isEmpty());
    }

    @Test
    void shouldGetListReviewsForFilm() {
        filmStorage.create(film);

        User user = userStorage.create(
                User.builder()
                        .email("user1@gmail.com")
                        .login("alex")
                        .name("Alex")
                        .birthday(LocalDate.of(1980, 5, 25))
                        .build());

        Review review = reviewStorage.create(
                Review.builder()
                        .content("Так себе фильм.")
                        .isPositive(false)
                        .userId(user.getId())
                        .filmId(film.getId())
                        .build());

        List<Review> expected = List.of(review);
        assertEquals(expected, reviewStorage.findAll(film.getId(), 10));
    }

    @Test
    void shouldGetListReviewsForFilmRangedByUseful() {
        filmStorage.create(film);

        User user1 = userStorage.create(
                User.builder()
                        .email("user1@gmail.com")
                        .login("alex")
                        .name("Alex")
                        .birthday(LocalDate.of(1980, 5, 25))
                        .build());

        User user2 = userStorage.create(
                User.builder()
                        .email("user2@gmail.com")
                        .login("john")
                        .name("Johny")
                        .birthday(LocalDate.of(1989, 7, 4))
                        .build());

        User user3 = userStorage.create(
                User.builder()
                        .email("user3@gmail.com")
                        .login("ivan")
                        .name("vano")
                        .birthday(LocalDate.of(1988, 8, 17))
                        .build());

        Review review1 = reviewStorage.create(
                Review.builder()
                        .content("Так себе фильм.")
                        .isPositive(false)
                        .userId(user1.getId())
                        .filmId(film.getId())
                        .build());

        Review review2 = reviewStorage.create(
                Review.builder()
                        .content("Неплохой фильм,но на один раз.")
                        .isPositive(true)
                        .userId(user2.getId())
                        .filmId(film.getId())
                        .build());

        Review review3 = reviewStorage.create(
                Review.builder()
                        .content("Нормально.")
                        .isPositive(true)
                        .userId(user3.getId())
                        .filmId(film.getId())
                        .build());

        reviewDislikeStorage.add(review1.getReviewId(), user2.getId());
        reviewDislikeStorage.add(review1.getReviewId(), user3.getId());
        reviewLikeStorage.add(review2.getReviewId(), user3.getId());

        List<Review> expected = List.of(
                reviewStorage.findById(review2.getReviewId()),
                reviewStorage.findById(review3.getReviewId()),
                reviewStorage.findById(review1.getReviewId())
        );
        assertEquals(expected, reviewStorage.findAll(film.getId(), 10));
    }

    @Test
    void shouldGetListReviewsForFilmsRangedByUseful() {
        Film film1 = filmStorage.create(film);
        Film film2 = filmStorage.create(
                Film.builder()
                        .name("Avatar")
                        .description("description about Avatar")
                        .duration(180)
                        .releaseDate(LocalDate.of(2009, 12, 10))
                        .mpa(new Mpa(2, "PG"))
                        .genres(new HashSet<>())
                        .build());

        User user1 = userStorage.create(
                User.builder()
                        .email("user1@gmail.com")
                        .login("alex")
                        .name("Alex")
                        .birthday(LocalDate.of(1980, 5, 25))
                        .build());

        User user2 = userStorage.create(
                User.builder()
                        .email("user2@gmail.com")
                        .login("john")
                        .name("Johny")
                        .birthday(LocalDate.of(1989, 7, 4))
                        .build());

        User user3 = userStorage.create(
                User.builder()
                        .email("user3@gmail.com")
                        .login("ivan")
                        .name("vano")
                        .birthday(LocalDate.of(1988, 8, 17))
                        .build());

        Review review1 = reviewStorage.create(
                Review.builder()
                        .content("Так себе фильм.")
                        .isPositive(false)
                        .userId(user1.getId())
                        .filmId(film.getId())
                        .build());

        Review review2 = reviewStorage.create(
                Review.builder()
                        .content("Неплохой фильм,но на один раз.")
                        .isPositive(true)
                        .userId(user2.getId())
                        .filmId(film1.getId())
                        .build());

        Review review3 = reviewStorage.create(
                Review.builder()
                        .content("Шикарный фильм, Кэмерон могет!")
                        .isPositive(true)
                        .userId(user1.getId())
                        .filmId(film2.getId())
                        .build());

        Review review4 = reviewStorage.create(
                Review.builder()
                        .content("До сих выглядит новаторски, скорее бы вторая часть!")
                        .isPositive(true)
                        .userId(user2.getId())
                        .filmId(film2.getId())
                        .build());

        reviewDislikeStorage.add(review1.getReviewId(), user2.getId());
        reviewDislikeStorage.add(review1.getReviewId(), user3.getId());
        reviewLikeStorage.add(review2.getReviewId(), user3.getId());
        reviewLikeStorage.add(review3.getReviewId(), user2.getId());
        reviewLikeStorage.add(review4.getReviewId(), user1.getId());
        reviewLikeStorage.add(review4.getReviewId(), user3.getId());

        List<Review> expected = List.of(
                reviewStorage.findById(review4.getReviewId()),
                reviewStorage.findById(review2.getReviewId()),
                reviewStorage.findById(review3.getReviewId()),
                reviewStorage.findById(review1.getReviewId())
        );
        assertEquals(expected, reviewStorage.findAll(null, 10));
    }
}