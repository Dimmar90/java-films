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
import ru.yandex.practicum.filmorate.service.DBFilmService;
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
    private final DBFilmService dbFilmService;

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
        User newUser = userStorage.createUser(user);
        Set<ConstraintViolation<User>> violations = validator.validate(newUser);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldUpdateUser() {
        userStorage.createUser(user);
        userStorage.updateUser(
                User.builder()
                        .id(1)
                        .email("user1@gmail.com")
                        .login("alex")
                        .name("Alex")
                        .birthday(LocalDate.of(1980, 5, 25))
                        .build()
        );
        assertEquals("alex", userStorage.getById(1).getLogin());
        assertEquals("Alex", userStorage.getById(1).getName());
        Set<ConstraintViolation<User>> violations = validator.validate(userStorage.getById(1));
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldGetListUsersWithoutViolations() {
        userStorage.createUser(user);
        userStorage.createUser(User.builder()
                .email("user2@gmail.com")
                .login("user2")
                .name("userName")
                .birthday(LocalDate.of(1995, 7, 25))
                .build()
        );
        userStorage.createUser(User.builder()
                .email("user3@gmail.com")
                .login("user3")
                .name("userName")
                .birthday(LocalDate.of(1989, 4, 15))
                .build()
        );
        Optional<Set<ConstraintViolation<User>>> violationSet = userStorage.getUsers().stream()
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
        userStorage.createUser(user);
        assertEquals(user, userStorage.getById(1));
    }

    @Test
    void shouldThrowsInvalidCheckUserExist() {
        userStorage.createUser(user);

        final NotFoundException e = assertThrows(
                NotFoundException.class,
                () -> userStorage.checkUserExist(2)
        );
        assertEquals("User ID = 2 does not exist", e.getMessage());
    }

    @Test
    void shouldAddFriend() {
        userStorage.createUser(user);
        User newUser = userStorage.createUser(
                User.builder()
                        .email("alex@gmail.com")
                        .login("alex")
                        .name("Alex")
                        .birthday(LocalDate.of(1980, 5, 25))
                        .build()
        );
        friendStorage.addFriend(user.getId(), newUser.getId());

        assertFalse(friendStorage.getAllFriends(user.getId()).isEmpty());
        assertTrue(friendStorage.getAllFriends(newUser.getId()).isEmpty());
    }

    @Test
    void shouldDeleteFriend() {
        userStorage.createUser(user);
        User newUser = userStorage.createUser(
                User.builder()
                        .email("alex@gmail.com")
                        .login("alex")
                        .name("Alex")
                        .birthday(LocalDate.of(1980, 5, 25))
                        .build()
        );
        friendStorage.addFriend(user.getId(), newUser.getId());
        assertFalse(friendStorage.getAllFriends(user.getId()).isEmpty());

        friendStorage.deleteFriend(user.getId(), newUser.getId());
        assertTrue(friendStorage.getAllFriends(user.getId()).isEmpty());
    }

    @Test
    void shouldConfirmFriendship() {
        userStorage.createUser(user);
        User newUser = userStorage.createUser(
                User.builder()
                        .email("alex@gmail.com")
                        .login("alex")
                        .name("Alex")
                        .birthday(LocalDate.of(1980, 5, 25))
                        .build()
        );
        friendStorage.addFriend(user.getId(), newUser.getId());
        friendStorage.addFriend(newUser.getId(), user.getId());

        assertFalse(friendStorage.getAllFriends(user.getId()).isEmpty());
        assertFalse(friendStorage.getAllFriends(newUser.getId()).isEmpty());
    }

    @Test
    void shouldGetCommonFriends() {
        userStorage.createUser(user);
        User newUser1 = userStorage.createUser(
                User.builder()
                        .email("alex@gmail.com")
                        .login("alex")
                        .name("Alex")
                        .birthday(LocalDate.of(1980, 5, 25))
                        .build()
        );

        User newUser2 = userStorage.createUser(
                User.builder()
                        .email("ivan@gmail.com")
                        .login("vano")
                        .name("Ivan")
                        .birthday(LocalDate.of(1982, 12, 12))
                        .build()
        );
        friendStorage.addFriend(user.getId(), newUser1.getId());
        friendStorage.addFriend(newUser2.getId(), newUser1.getId());

        assertEquals(List.of(newUser1), friendStorage.getCommonFriends(user.getId(), newUser2.getId()));
    }

    @Test
    void shouldGetAllFriends() {
        userStorage.createUser(user);
        User newUser1 = userStorage.createUser(
                User.builder()
                        .email("alex@gmail.com")
                        .login("alex")
                        .name("Alex")
                        .birthday(LocalDate.of(1980, 5, 25))
                        .build()
        );

        User newUser2 = userStorage.createUser(
                User.builder()
                        .email("ivan@gmail.com")
                        .login("vano")
                        .name("Ivan")
                        .birthday(LocalDate.of(1982, 12, 12))
                        .build()
        );
        friendStorage.addFriend(user.getId(), newUser1.getId());
        friendStorage.addFriend(user.getId(), newUser2.getId());

        assertEquals(List.of(newUser1, newUser2), friendStorage.getAllFriends(user.getId()));
    }

    @Test
    void shouldDeleteUserById() {
        userStorage.createUser(user);
        List<User> expectedUsers = userStorage.getUsers();
        user.setId(1);

        assertThat(expectedUsers).hasSize(1).contains(user);
        userStorage.deleteUserById(user.getId());
        assertThat(userStorage.getUsers()).hasSize(0);
    }

    @Test
    void recomendatedFilmsForUser() {

        userStorage.createUser(User.builder()
                .email("user1@gmail.com")
                .login("user1")
                .name("user1 name")
                .birthday(LocalDate.of(1980, 5, 25))
                .build());

        userStorage.createUser(User.builder()
                .email("user2@gmail.com")
                .login("user2")
                .name("userName")
                .birthday(LocalDate.of(1995, 7, 25))
                .build()
        );
        userStorage.createUser(User.builder()
                .email("user3@gmail.com")
                .login("user3")
                .name("userName")
                .birthday(LocalDate.of(1989, 4, 15))
                .build()
        );
        List<User> users = userStorage.getUsers();

        Film film1 = filmStorage.createFilm(Film.builder()
                .name("some film")
                .description("description some film")
                .duration(180)
                .releaseDate(LocalDate.of(2005, 6, 5))
                .mpa(new Mpa(1, "G"))
                .genres(new HashSet<>())
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

        Film film3 = filmStorage.createFilm(
                Film.builder()
                        .name("some WONDERFULLFilm")
                        .description("NEWNEW description some film")
                        .duration(1000)
                        .releaseDate(LocalDate.of(2000, 1, 5))
                        .mpa(new Mpa(1, "G"))
                        .genres(new HashSet<>())
                        .build()
        );
        List<Film> films = filmStorage.getFilms();

        filmLikesDao.like(1, 1);

        filmLikesDao.like(1, 2);
        filmLikesDao.like(2, 2);
        filmLikesDao.like(3, 2);

        filmLikesDao.like(1, 3);
        filmLikesDao.like(2, 3);

        Set<Film> listFilms = userDaoImpl.getRecommendationsFilms(1, dbFilmService);
        List<Integer> listId = new ArrayList<>();
        for (Film film : listFilms) {
            listId.add(film.getId());
        }

        assertTrue(listId.contains(2));
        assertTrue(listId.contains(3));
        assertTrue(listId.size() == 2);
    }
}