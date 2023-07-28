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
public class UserService {
    private final UserDao userDao;
    private final FriendDao friendDao;
    private final EventDao eventDao;

    @Autowired
    public UserService(@Qualifier("userDaoImpl") UserDao userDao, FriendDao friendDao, EventDao eventDao) {
        this.userDao = userDao;
        this.friendDao = friendDao;
        this.eventDao = eventDao;
    }

    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) user.setName(user.getLogin());
        user = userDao.create(user);
        log.info("User {} has been CREATED", user);
        return user;
    }

    public User update(User user) {
        if (user.getId() == null || !userDao.checkExist(user.getId())) {
            throw new NotFoundException("Cannot update user with ID = null");
        }
        log.info("User {} has been UPDATED", user);
        return userDao.update(user);
    }

    public void addFriend(Integer userId, Integer friendId) {
        if (getFriends(userId).contains(getById(friendId))) {
            throw new AlreadyExistException(String.format(
                    "User with ID = %d is ALREADY friends with user with ID = %d", friendId, userId));
        }
        friendDao.add(userId, friendId);
        log.info("User with ID = {} ADDED user with ID = {} as a friend", userId, friendId);
        eventDao.add(userId, "FRIEND", "ADD", friendId); // добавляю событие в ленту
    }

    public void deleteFriend(Integer userId, Integer friendId) {
        if (!getFriends(userId).contains(getById(friendId))) {
            throw new NotFoundException(
                    String.format("Deleted user with ID = %d was NOT FOUND in friends", friendId));
        }
        friendDao.delete(userId, friendId);
        log.info("User with ID = {} REMOVED from friends of user with ID = {}", friendId, userId);
        eventDao.add(userId, "FRIEND", "REMOVE", friendId); // удаляю событие из ленты
    }

    public User getById(Integer id) {
        userDao.checkExist(id);
        log.info("Get a user with ID = {}", id);
        return userDao.findById(id);
    }

    public List<User> getAll() {
        return userDao.findAll();
    }

    public List<User> getFriends(Integer id) {
        userDao.checkExist(id);
        log.info("Get friends of the user with ID= {}", id);
        return friendDao.findAll(id);
    }

    public List<User> getCommonFriends(Integer userId, Integer friendId) {
        userDao.checkExist(userId);
        userDao.checkExist(friendId);
        log.info("Get common friends of users with ID = {} and ID = {}", userId, friendId);
        return friendDao.findCommon(userId, friendId);
    }

    public void delete(Integer userId) {
        userDao.checkExist(userId);
        userDao.delete(userId);
        log.info("Delete user from users with ID = {}", userId);
    }

    public Set<Film> getRecommendedFilms(Integer id, FilmService dbFilmService) {
        userDao.checkExist(id);
        log.info("Get a recommended films for user with ID = {}", id);
        UserDaoImpl userDaoImpl = (UserDaoImpl) userDao;
        return userDaoImpl.findRecommendationsFilms(id, dbFilmService);
    }

    public List<Event> getUserFeed(Integer userId) {
        userDao.checkExist(userId);
        log.info("Get feed of the user with ID= {}", userId);
        return eventDao.findUserFeed(userId);
    }
}
