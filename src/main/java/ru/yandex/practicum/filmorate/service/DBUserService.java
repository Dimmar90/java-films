package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.user.FriendDao;
import ru.yandex.practicum.filmorate.storage.dao.user.UserDao;

import java.util.List;

@Service
@Slf4j
public class DBUserService {
    private final UserDao userDao;
    private final FriendDao friendDao;

    @Autowired
    public DBUserService(@Qualifier("userDaoImpl") UserDao userDao, FriendDao friendDao) {
        this.userDao = userDao;
        this.friendDao = friendDao;
    }

    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) user.setName(user.getLogin());
        user = userDao.createUser(user);
        log.info("User {} has been CREATED", user);
        return user;
    }

    public User update(User user) {
        if (user.getId() == null || !userDao.checkUserExist(user.getId())) {
            throw new NotFoundException("Cannot update user with ID = null");
        }
        log.info("User {} has been UPDATED", user);
        return userDao.updateUser(user);
    }

    public void addFriend(Integer userId, Integer friendId) {
        if (getUserFriends(userId).contains(getUser(friendId))) {
            throw new AlreadyExistException(String.format(
                    "User with ID = %d is ALREADY friends with user with ID = %d", friendId, userId));
        }
        friendDao.addFriend(userId, friendId);
        log.info("User with ID = {} ADDED user with ID = {} as a friend", userId, friendId);
    }

    public void deleteFriend(Integer userId, Integer friendId) {
        if (!getUserFriends(userId).contains(getUser(friendId))) {
            throw new NotFoundException(
                    String.format("Deleted user with ID = %d was NOT FOUND in friends", friendId));
        }
        friendDao.deleteFriend(userId, friendId);
        log.info("User with ID = {} REMOVED from friends of user with ID = {}", friendId, userId);
    }

    public User getUser(Integer id) {
        userDao.checkUserExist(id);
        log.info("Get a user with ID = {}", id);
        return userDao.getById(id);
    }

    public List<User> getUsers() {
        return userDao.getUsers();
    }

    public List<User> getUserFriends(Integer id) {
        userDao.checkUserExist(id);
        log.info("Get friends of the user with ID= {}", id);
        return friendDao.getAllFriends(id);
    }

    public List<User> getCommonFriends(Integer userId, Integer friendId) {
        userDao.checkUserExist(userId);
        userDao.checkUserExist(friendId);
        log.info("Get common friends of users with ID = {} and ID = {}", userId, friendId);
        return friendDao.getCommonFriends(userId, friendId);
    }

    public void deleteUserById(Integer userId) {
        userDao.checkUserExist(userId);
        userDao.deleteUserById(userId);
        log.info("Delete user from users with ID = {}", userId);
    }
}
