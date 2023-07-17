package ru.yandex.practicum.filmorate.storage.dao.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FriendDao {
    void addFriend(Integer id, Integer friendId);

    void deleteFriend(Integer id, Integer friendId);

    List<User> getCommonFriends(Integer id, Integer otherId);

    List<User> getAllFriends(Integer id);
}
