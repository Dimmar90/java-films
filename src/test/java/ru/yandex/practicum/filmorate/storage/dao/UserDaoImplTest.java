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
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmDao;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmLikesDao;
import ru.yandex.practicum.filmorate.storage.dao.user.UserDao;
import ru.yandex.practicum.filmorate.storage.dao.user.impl.FriendDaoImpl;
import ru.yandex.practicum.filmorate.storage.dao.user.impl.UserDaoImpl;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserDaoImplTest {
    private final UserDao userStorage;
    private final FriendDaoImpl friendStorage;
    private User user;
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();
    private final FilmDao filmStorage;
    private final FilmLikesDao filmLikesDao;
    private final UserDaoImpl userDaoImpl;
    private final FilmService dbFilmService;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("user1@gmail.com")
                .login("user1")
                .name("user1 name")
                .birthday(LocalDate.of(1980, 5, 25))
                .build();
    }

    @Test
    void shouldCreateUser() {
        User newUser = userStorage.create(user);
        Set<ConstraintViolation<User>> violations = validator.validate(newUser);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldUpdateUser() {
        userStorage.create(user);
        userStorage.update(
                User.builder()
                        .id(1L)
                        .email("user1@gmail.com")
                        .login("alex")
                        .name("Alex")
                        .birthday(LocalDate.of(1980, 5, 25))
                        .build()
        );
        assertEquals("alex", userStorage.findById(1L).getLogin());
        assertEquals("Alex", userStorage.findById(1L).getName());
        Set<ConstraintViolation<User>> violations = validator.validate(userStorage.findById(1L));
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldGetListUsersWithoutViolations() {
        userStorage.create(user);
        userStorage.create(User.builder()
                .email("user2@gmail.com")
                .login("user2")
                .name("userName")
                .birthday(LocalDate.of(1995, 7, 25))
                .build()
        );
        userStorage.create(User.builder()
                .email("user3@gmail.com")
                .login("user3")
                .name("userName")
                .birthday(LocalDate.of(1989, 4, 15))
                .build()
        );
        Optional<Set<ConstraintViolation<User>>> violationSet = userStorage.findAll().stream()
                .map(user -> validator.validate(user))
                .filter(violation -> !violation.isEmpty())
                .findFirst();
        violationSet.ifPresentOrElse(
                violation -> assertTrue(violation.isEmpty()),
                () -> assertTrue(true)
        );
    }

    @Test
    void shouldGetUserById() {
        userStorage.create(user);
        assertEquals(user, userStorage.findById(1L));
    }

    @Test
    void shouldThrowsInvalidCheckUserExist() {
        userStorage.create(user);

        final NotFoundException e = assertThrows(
                NotFoundException.class,
                () -> userStorage.checkExist(2L)
        );
        assertEquals("User ID = 2 does not exist", e.getMessage());
    }

    @Test
    void shouldAddFriend() {
        userStorage.create(user);
        User newUser = userStorage.create(
                User.builder()
                        .email("alex@gmail.com")
                        .login("alex")
                        .name("Alex")
                        .birthday(LocalDate.of(1980, 5, 25))
                        .build()
        );
        friendStorage.add(user.getId(), newUser.getId());

        assertFalse(friendStorage.findAll(user.getId()).isEmpty());
        assertTrue(friendStorage.findAll(newUser.getId()).isEmpty());
    }

    @Test
    void shouldDeleteFriend() {
        userStorage.create(user);
        User newUser = userStorage.create(
                User.builder()
                        .email("alex@gmail.com")
                        .login("alex")
                        .name("Alex")
                        .birthday(LocalDate.of(1980, 5, 25))
                        .build()
        );
        friendStorage.add(user.getId(), newUser.getId());
        assertFalse(friendStorage.findAll(user.getId()).isEmpty());

        friendStorage.delete(user.getId(), newUser.getId());
        assertTrue(friendStorage.findAll(user.getId()).isEmpty());
    }

    @Test
    void shouldConfirmFriendship() {
        userStorage.create(user);
        User newUser = userStorage.create(
                User.builder()
                        .email("alex@gmail.com")
                        .login("alex")
                        .name("Alex")
                        .birthday(LocalDate.of(1980, 5, 25))
                        .build()
        );
        friendStorage.add(user.getId(), newUser.getId());
        friendStorage.add(newUser.getId(), user.getId());

        assertFalse(friendStorage.findAll(user.getId()).isEmpty());
        assertFalse(friendStorage.findAll(newUser.getId()).isEmpty());
    }

    @Test
    void shouldGetCommonFriends() {
        userStorage.create(user);
        User newUser1 = userStorage.create(
                User.builder()
                        .email("alex@gmail.com")
                        .login("alex")
                        .name("Alex")
                        .birthday(LocalDate.of(1980, 5, 25))
                        .build()
        );

        User newUser2 = userStorage.create(
                User.builder()
                        .email("ivan@gmail.com")
                        .login("vano")
                        .name("Ivan")
                        .birthday(LocalDate.of(1982, 12, 12))
                        .build()
        );
        friendStorage.add(user.getId(), newUser1.getId());
        friendStorage.add(newUser2.getId(), newUser1.getId());

        assertEquals(List.of(newUser1), friendStorage.findCommon(user.getId(), newUser2.getId()));
    }

    @Test
    void shouldGetAllFriends() {
        userStorage.create(user);
        User newUser1 = userStorage.create(
                User.builder()
                        .email("alex@gmail.com")
                        .login("alex")
                        .name("Alex")
                        .birthday(LocalDate.of(1980, 5, 25))
                        .build()
        );

        User newUser2 = userStorage.create(
                User.builder()
                        .email("ivan@gmail.com")
                        .login("vano")
                        .name("Ivan")
                        .birthday(LocalDate.of(1982, 12, 12))
                        .build()
        );
        friendStorage.add(user.getId(), newUser1.getId());
        friendStorage.add(user.getId(), newUser2.getId());

        assertEquals(List.of(newUser1, newUser2), friendStorage.findAll(user.getId()));
    }

    @Test
    void shouldDeleteUserById() {
        userStorage.create(user);
        List<User> expectedUsers = userStorage.findAll();
        user.setId(1L);

        assertThat(expectedUsers).hasSize(1).contains(user);
        userStorage.delete(user.getId());
        assertThat(userStorage.findAll()).hasSize(0);
    }

    @Test
    void recomendatedFilmsForUser() {

        userStorage.create(User.builder()
                .email("user1@gmail.com")
                .login("user1")
                .name("user1 name")
                .birthday(LocalDate.of(1980, 5, 25))
                .build());

        userStorage.create(User.builder()
                .email("user2@gmail.com")
                .login("user2")
                .name("userName")
                .birthday(LocalDate.of(1995, 7, 25))
                .build()
        );
        userStorage.create(User.builder()
                .email("user3@gmail.com")
                .login("user3")
                .name("userName")
                .birthday(LocalDate.of(1989, 4, 15))
                .build()
        );
        List<User> users = userStorage.findAll();

        Film film1 = filmStorage.create(Film.builder()
                .name("some film")
                .description("description some film")
                .duration(180)
                .releaseDate(LocalDate.of(2005, 6, 5))
                .mpa(new Mpa(1, "G"))
                .genres(new HashSet<>())
                .build());

        Film film2 = filmStorage.create(
                Film.builder()
                        .name("some newFilm")
                        .description("new description some film")
                        .duration(150)
                        .releaseDate(LocalDate.of(2001, 3, 8))
                        .mpa(new Mpa(1, "G"))
                        .genres(new HashSet<>())
                        .build()
        );

        Film film3 = filmStorage.create(
                Film.builder()
                        .name("some WONDERFULLFilm")
                        .description("NEWNEW description some film")
                        .duration(1000)
                        .releaseDate(LocalDate.of(2000, 1, 5))
                        .mpa(new Mpa(1, "G"))
                        .genres(new HashSet<>())
                        .build()
        );
        List<Film> films = filmStorage.findAll();

        filmLikesDao.like(1L, 1L);

        filmLikesDao.like(1L, 2L);
        filmLikesDao.like(2L, 2L);
        filmLikesDao.like(3L, 2L);

        filmLikesDao.like(1L, 3L);
        filmLikesDao.like(2L, 3L);

        Set<Film> listFilms = userDaoImpl.findRecommendationsFilms(1L, dbFilmService);
        List<Long> listId = new ArrayList<>();
        for (Film film : listFilms) {
            listId.add(film.getId());
        }

        assertTrue(listId.contains(2));
        assertTrue(listId.contains(3));
        assertTrue(listId.size() == 2);
    }
}