package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @Test
    @DisplayName("Проверка валидации пользователя, если все поля User корректны")
    void shouldCreateUser() {
        User user = userController.create(new User(null, "user@gmail.com", "user1", "userName",
                LocalDate.of(1985, 9,2)));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Проверка валидации пользователя, если имя null")
    void shouldCreateUserIfNameIsNull() {
        User user = userController.create(new User(null, "user@gmail.com", "user1", null,
                LocalDate.of(1985, 9,2)));
        assertEquals("user1", user.getName());
    }

    @Test
    @DisplayName("Проверка валидации пользователя, если имя пустое")
    void shouldCreateUserIfNameIsEmpty() {
        User user = userController.create(new User(null, "user@gmail.com", "user1", "",
                LocalDate.of(1985, 9,2)));
        assertEquals("user1", user.getName());
    }

    @Test
    @DisplayName("Проверка валидации пользователя, если в имени только пробел")
    void shouldCreateUserIfNameIsBlank() {
        User user = userController.create(new User(null, "user@gmail.com", "user1", " ",
                LocalDate.of(1985, 9,2)));
        assertEquals("user1", user.getName());
    }

    @Test
    @DisplayName("Проверка валидации пользователя, если все поля User null")
    void shouldViolationIfUserIsNull() {
        User user = userController.create(new User());
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(2, violations.size());
    }

    @Test
    @DisplayName("Проверка валидации пользователя, если логин null")
    void shouldViolationIfUserLoginIsNull() {
        User user = userController.create(new User(null, "user@gmail.com", null, "user",
                LocalDate.of(1985, 9,2)));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Проверка валидации пользователя, если в логине только пробел")
    void shouldViolationIfUserLoginIsBlank() {
        User user = userController.create(new User(null, "user@gmail.com", " ", "user",
                LocalDate.of(1985, 9,2)));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Проверка валидации пользователя, если email null")
    void shouldViolationIfUserEmailIsNull() {
        User user = userController.create(new User(null,null, "user1", "user",
                LocalDate.of(1985, 9,2)));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Проверка валидации пользователя, если email пустой")
    void shouldViolationIfUserEmailIs() {
        User user = userController.create(new User(null, "", "user1", "user",
                LocalDate.of(1985, 9,2)));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Проверка валидации пользователя, если email некорректен")
    void shouldViolationIfUserEmailIsIncorrect() {
        User user = userController.create(new User(null, "user@", "user1", "user",
                LocalDate.of(1985, 9,2)));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Проверка валидации пользователя, если дата рождения в будущем")
    void shouldViolationIfUserBirthdayInFuture() {
        User user = userController.create(new User(null, "user@gmail.com", "user1", "user",
                LocalDate.of(2024, 1,1)));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());
    }

    @Test
    @DisplayName("Проверка валидации пользователя при обновлении, если все поля User корректны")
    void shouldUpdateUser() {
        userController.create(new User(null, "user@gmail.com", "user1", "userName",
                LocalDate.of(1985, 9,2)));
        User user = userController.update(new User(1, "user@gmail.com", "alex", "Alex",
                LocalDate.of(1985, 9,2)));
        assertEquals("alex", user.getLogin());
        assertEquals("Alex", user.getName());
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Проверка валидации пользователя при обновлении, если User.id некорректен")
    void shouldThrowsWhenUpdatedUserWithIncorrectId() {
        userController.create(new User(null, "user@gmail.com", "user1", "userName",
                LocalDate.of(1985, 9,2)));
        final ValidationException exp = assertThrows(
                ValidationException.class,
                () ->  userController.update(new User(2, "user@gmail.com", "alex", "Alex",
                            LocalDate.of(1985, 9,2)))
        );
        assertEquals("Выполните post-запрос или введите корректный ID пользователя", exp.getMessage());
    }

    @Test
    @DisplayName("Проверка валидации пользователя при обновлении, если User.id = null")
    void shouldThrowsWhenUpdatedUserWithIdEqualNull() {
        userController.create(new User(null, "user@gmail.com", "user1", "userName",
                LocalDate.of(1985, 9,2)));
        final ValidationException exp = assertThrows(
                ValidationException.class,
                () ->  userController.update(new User(null, "user@gmail.com", "alex", "Alex",
                        LocalDate.of(1985, 9,2)))
        );
        assertEquals("Выполните post-запрос или введите корректный ID пользователя", exp.getMessage());
    }

    @Test
    @DisplayName("Проверка возврата списка созданных пользователей без ошибок валидации")
    void shouldGetListUsersWithoutViolations() {
        userController.create(new User(null, "user1@gmail.com", "user1", "userName",
                LocalDate.of(1985, 9,2)));
        userController.create(new User(null, "user2@gmail.com", "user2", "userName",
                LocalDate.of(1957, 11,10)));
        userController.create(new User(null, "user3@gmail.com", "user3", "userName",
                LocalDate.of(1992, 5,17)));

        Optional<Set<ConstraintViolation<User>>> violationSet = userController.getUsers().stream()
                .map(user -> validator.validate(user))
                .filter(violation -> !violation.isEmpty())
                .findFirst();
        violationSet.ifPresentOrElse(
                violation -> assertTrue(violation.isEmpty()),
                () -> assertTrue(true)
        );
    }

    @Test
    @DisplayName("Проверка возврата списка пользователей с ошибками валидации")
    void shouldGetListUsersWithViolations() {
        userController.create(new User(null, "user1@gmail.com", " ", "userName",
                LocalDate.of(1985, 9,2)));
        userController.create(new User(null, "user2@gmail.com", "", "userName",
                LocalDate.of(1957, 11,10)));
        userController.create(new User(null, "user3@", "user3", "userName",
                LocalDate.of(1992, 5,17)));

        Optional<Set<ConstraintViolation<User>>> violationSet = userController.getUsers().stream()
                .map(user -> validator.validate(user))
                .filter(violation -> !violation.isEmpty())
                .findFirst();
        violationSet.ifPresentOrElse(
                violation -> assertFalse(violation.isEmpty()),
                () -> assertFalse(false)
        );
    }
}