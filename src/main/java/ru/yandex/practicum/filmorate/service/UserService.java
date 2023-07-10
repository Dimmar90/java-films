package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) user.setName(user.getLogin());
        log.info("User {} has been CREATED", user);
        return userStorage.createUser(user);
    }

    public User update(User user) {
        if (user.getId() == null || userStorage.getById(user.getId()) == null) {
            throw new NotFoundException("Cannot update user with ID = null");
        }
        log.info("User {} has been UPDATED", user);
        return userStorage.updateUser(user);
    }

    public void addFriend(Integer userId, Integer friendId) {
        User user = getUser(userId);
        if (user.getFriends().contains(friendId)) {
            throw new AlreadyExistException(String.format(
                    "User with ID = %d is ALREADY friends with user with ID = %d", friendId, userId));
        }

        User friend = getUser(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("User with ID = {} ADDED user with ID = {} as a friend", userId, friendId);
    }

    public void deleteFriend(Integer userId, Integer friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);

        if (!user.getFriends().contains(friendId)) {
            throw new NotFoundException(
                    String.format("Deleted user with ID = %d was NOT FOUND in friends", friendId));
        }

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("User with ID = {} REMOVED from friends of user with ID = {}", friendId, userId);
    }

    public User getUser(Integer id) {
        if (userStorage.getById(id) == null) {
            throw new NotFoundException(String.format("User ID = %d does not exist", id));
        }
        log.info("Get a user with ID = {}", id);
        return userStorage.getById(id);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public List<User> getUserFriends(Integer id) {
        if (userStorage.getById(id) == null) {
            throw new NotFoundException(String.format("User ID = %d does not exist", id));
        }
        log.info("Get friends of the user with ID= {}", id);
        return userStorage.getUserFriends(id);
    }

    public List<User> getCommonFriends(Integer userId, Integer friendId) {
        List<User> friends = getUserFriends(userId);
        friends.retainAll(getUserFriends(friendId));
        log.info("Get common friends of users with ID = {} and ID = {}", userId, friendId);
        return friends;
    }
}
