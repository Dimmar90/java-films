package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.user.FriendDao;
import ru.yandex.practicum.filmorate.storage.dao.user.UserDao;

import java.util.*;

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

    public Set<Film> getRecommendationsFilms(Integer id, DBFilmService dbFilmService) {
        userDao.checkUserExist(id);
        log.info("Get a RecommendationsFilms for user with ID = {}", id);
        Set<Film> recommendedFilms = new HashSet<>();   // список рекомендованных фильмов
        List<Film> listFilms = dbFilmService.getFilms();// список всех фильмов

        List<Film> listFilmsUser = new ArrayList<>();// список всех фильмов userа c id
        for (Film film : listFilms) {
            if (film.getLikes() != null && film.getLikes().size() > 0 && film.getLikes().contains(id)) {
                listFilmsUser.add(film);
            }
        }
        if (listFilmsUser.size() == 0) return recommendedFilms;

        HashMap<Integer, List<Film>> usersWithListsFilms = new HashMap<>();  // список всех userов кроме user id и списки их фильмов
        for (Film film : listFilms) {
            if (film.getLikes() == null || film.getLikes().size() == 0) continue;
            for (Integer like : film.getLikes()) {
                if (like == id) continue;
                if (usersWithListsFilms.containsKey(like)) {
                    usersWithListsFilms.get(like).add(film);
                } else {
                    List<Film> films = new ArrayList<>();
                    films.add(film);
                    usersWithListsFilms.put(like, films);
                }
            }
        }

        HashMap<Integer, List<Film>> usersWithMaxAlignLike = new HashMap<>();  // список всех userов c фильми с максимальным совпадением лайков с userом

        int countOld = 0;

        for (Map.Entry<Integer, List<Film>> integerListEntry : usersWithListsFilms.entrySet()) {
            int countNew = 0;
            for (Film film : listFilmsUser) {
                if (integerListEntry.getValue().contains(film)) {
                    countNew++;
                }
            }
            if (countNew != 0 && countNew >= countOld) {
                countOld = countNew;
                usersWithMaxAlignLike.put(integerListEntry.getKey(), integerListEntry.getValue());
            } else {
                continue;
            }
        }

        if (usersWithMaxAlignLike.size() > 0) {
            for (Map.Entry<Integer, List<Film>> integerListEntry : usersWithMaxAlignLike.entrySet()) {
                integerListEntry.getValue().removeAll(listFilmsUser);
                recommendedFilms.addAll(integerListEntry.getValue());
            }

        }
        return recommendedFilms;
    }
}
