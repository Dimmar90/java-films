package ru.yandex.practicum.filmorate.storage.dao.user;

import ru.yandex.practicum.filmorate.storage.UserStorage;

public interface UserDao extends UserStorage {
    boolean checkUserExist(Integer id);

    void deleteUserById(Integer userId);
}
