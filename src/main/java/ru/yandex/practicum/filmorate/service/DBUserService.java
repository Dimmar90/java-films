package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.user.EventDao;
import ru.yandex.practicum.filmorate.storage.dao.user.FriendDao;
import ru.yandex.practicum.filmorate.storage.dao.user.UserDao;
import ru.yandex.practicum.filmorate.storage.dao.user.impl.UserDaoImpl;

import java.util.*;

@Service
@Slf4j
public class DBUserService {
    private final UserDao userDao;
    private final FriendDao friendDao;
    private final EventDao eventDao;

    @Autowired
    public DBUserService(@Qualifier("userDaoImpl") UserDao userDao, FriendDao friendDao, EventDao eventDao) {
        this.userDao = userDao;
        this.friendDao = friendDao;
        this.eventDao = eventDao;
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
        eventDao.addEvent(userId, "FRIEND", "ADD", friendId); // добавляю событие в ленту
    }

    public void deleteFriend(Integer userId, Integer friendId) {
        if (!getUserFriends(userId).contains(getUser(friendId))) {
            throw new NotFoundException(
                    String.format("Deleted user with ID = %d was NOT FOUND in friends", friendId));
        }
        friendDao.deleteFriend(userId, friendId);
        log.info("User with ID = {} REMOVED from friends of user with ID = {}", friendId, userId);
        eventDao.addEvent(userId, "FRIEND", "REMOVE", friendId); // удаляю событие из ленты
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

    public Set<Film> getRecommendationsFilms(Integer id, DBFilmService dbFilmService) {
        userDao.checkUserExist(id);
        log.info("Get a RecommendationsFilms for user with ID = {}", id);
        UserDaoImpl userDaoImpl = (UserDaoImpl) userDao;
        return userDaoImpl.getRecommendationsFilms(id, dbFilmService);
    }

    public List<Event> getEventFeed(Integer userId) {
        userDao.checkUserExist(userId);
        log.info("Get feed of the user with ID= {}", userId);
        return eventDao.getEventFeed(userId);
    }
}
