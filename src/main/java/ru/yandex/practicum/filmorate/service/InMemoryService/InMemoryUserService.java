package ru.yandex.practicum.filmorate.service.InMemoryService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InMemoryUserService {
    private final UserStorage userStorage;

    public InMemoryUserService(@Qualifier("inMemoryUserStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) user.setName(user.getLogin());
        log.info("User {} has been CREATED", user);
        return userStorage.create(user);
    }

    public User update(User user) {
        if (user.getId() == null || userStorage.findById(user.getId()) == null) {
            throw new NotFoundException("Cannot update user with ID = null");
        }
        log.info("User {} has been UPDATED", user);
        return userStorage.update(user);
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
        if (userStorage.findById(id) == null) {
            throw new NotFoundException(String.format("User ID = %d does not exist", id));
        }
        log.info("Get a user with ID = {}", id);
        return userStorage.findById(id);
    }

    public List<User> getUsers() {
        return userStorage.findAll();
    }

    public List<User> getUserFriends(Integer id) {
        if (userStorage.findById(id) == null) {
            throw new NotFoundException(String.format("User ID = %d does not exist", id));
        }
        log.info("Get friends of the user with ID= {}", id);
        return getUser(id).getFriends()
                .stream()
                .map(this::getUser)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Integer userId, Integer friendId) {
        List<User> friends = getUserFriends(userId);
        friends.retainAll(getUserFriends(friendId));
        log.info("Get common friends of users with ID = {} and ID = {}", userId, friendId);
        return friends;
    }
}
