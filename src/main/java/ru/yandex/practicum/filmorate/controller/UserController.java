package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    /*private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    Set<ConstraintViolation<User>> violations;*/
    private final HashMap<Integer, User> users = new HashMap<>();
    private Integer userId = 1;

    @PostMapping //("/post")
    public User create(@Valid @RequestBody User user) {
        /*violations = validator.validate(user);
        if (!violations.isEmpty()) return user;*/

        user.setId(userId++);
        if (user.getName() == null || user.getName().isBlank()) user.setName(user.getLogin());
        users.put(user.getId(), user);
        log.info("User {} has been CREATED", user);
        return user;
    }

    @PutMapping //("/update")
    public User update(@Valid @RequestBody User user) {
        //violations = validator.validate(user);
        if (user.getId() == null || !users.containsKey(user.getId())) {
            throw new ValidationException("Выполните post-запрос или введите корректный ID пользователя");
        }
        //if (!violations.isEmpty()) return user;

        users.put(user.getId(), user);
        log.info("User {} has been UPDATED", user);
        return user;
    }

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }
}
