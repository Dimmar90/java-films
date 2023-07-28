package ru.yandex.practicum.filmorate.storage.dao.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserDao {

    User create(User user);

    User update(User user);

    List<User> findAll();

    User findById(Long id);

    boolean checkExist(Long id);

    void delete(Long userId);
}
