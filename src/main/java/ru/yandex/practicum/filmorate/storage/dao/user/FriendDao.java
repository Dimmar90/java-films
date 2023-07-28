package ru.yandex.practicum.filmorate.storage.dao.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FriendDao {
    void add(Integer id, Integer friendId);

    void delete(Integer id, Integer friendId);

    List<User> findCommon(Integer id, Integer otherId);

    List<User> findAll(Integer id);
}
