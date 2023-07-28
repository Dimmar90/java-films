package ru.yandex.practicum.filmorate.storage.dao.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FriendDao {
    void add(Long id, Long friendId);

    void delete(Long id, Long friendId);

    List<User> findCommon(Long id, Long otherId);

    List<User> findAll(Long id);
}
