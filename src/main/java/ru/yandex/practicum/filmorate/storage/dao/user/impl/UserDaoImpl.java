package ru.yandex.practicum.filmorate.storage.dao.user.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.dao.user.UserDao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component("userDaoImpl")
@RequiredArgsConstructor
@Slf4j
public class UserDaoImpl implements UserDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User create(User user) {
        String sqlQuery = "INSERT INTO users (email, login, name, birthday) VALUES (?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, (java.sql.Date.valueOf(user.getBirthday())));
            return ps;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return user;
    }

    @Override
    public User update(User user) {
        String sqlQuery = "UPDATE users SET " +
                "email = ?," +
                "login = ?," +
                "name = ?," +
                "birthday = ?" +
                "WHERE id = ?";
        jdbcTemplate.update(sqlQuery, user.getEmail(), user.getLogin(), user.getName(),
                user.getBirthday(), user.getId());
        return user;
    }

    @Override
    public List<User> findAll() {
        String sqlQuery = "SELECT * FROM users";
        return jdbcTemplate.query(sqlQuery, this::mapRowToUser);
    }

    @Override
    public User findById(Integer id) {
        String sqlQuery = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToUser, id);
    }

    @Override
    public void delete(Integer userId) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", userId);
    }

    public Set<Film> findRecommendationsFilms(Integer id, FilmService dbFilmService) {
        String sqlQuery = "SELECT FILM_ID FROM FILM_LIKES " +
                "WHERE USER_ID IN (SELECT USER_ID FROM FILM_LIKES WHERE FILM_ID IN " +
                "(SELECT u.FILM_ID FROM FILM_LIKES u WHERE u.USER_ID = ?) AND NOT USER_ID=? " +
                "GROUP BY USER_ID HAVING COUNT(USER_ID)= (SELECT MAX(max) FROM (SELECT USER_ID, COUNT(USER_ID) AS max " +
                "FROM FILM_LIKES WHERE FILM_ID IN (SELECT u.FILM_ID FROM FILM_LIKES u WHERE u.USER_ID = ?) " +
                "AND NOT USER_ID=? GROUP BY USER_ID))) " +
                "AND NOT FILM_ID IN (SELECT u.FILM_ID FROM FILM_LIKES u WHERE u.USER_ID = ?)";

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sqlQuery, id, id, id, id, id);
        Set<Film> recomendatedFilms = new HashSet<>();
        while (rowSet.next()) {
            Film film = dbFilmService.getById(rowSet.getInt("FILM_ID"));
            recomendatedFilms.add(film);
        }
        return recomendatedFilms;
    }

    @Override
    public boolean checkExist(Integer id) throws NotFoundException {
        String sqlQuery = "SELECT id FROM users WHERE id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (!rowSet.next()) {
            throw new NotFoundException(String.format("User ID = %d does not exist", id));
        }
        return true;
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getInt("id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();
    }
}
