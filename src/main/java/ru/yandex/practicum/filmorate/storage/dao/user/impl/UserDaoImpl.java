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

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
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
    public User findById(Long id) {
        String sqlQuery = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToUser, id);
    }

    @Override
    public void delete(Long userId) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", userId);
    }

    public Set<Film> findRecommendationsFilms(Long id, FilmService dbFilmService) {

        String sqlQuery = "SELECT DISTINCT fl.film_id FROM film_likes fl " +
                "JOIN (SELECT a.user_id, COUNT(a.user_id) AS likes_count FROM film_likes AS a " +
                "JOIN film_likes AS b  ON  a.film_id=b.film_id WHERE b.user_id = ? AND  a.user_id<>? GROUP BY a.user_id) " +
                "UserLikesCount ON fl.user_id = UserLikesCount.user_id " +
                "JOIN (SELECT COUNT(a.user_id) AS max_likes FROM film_likes AS a " +
                "JOIN film_likes AS b  ON  a.film_id=b.film_id WHERE b.user_id = ? AND  a.user_id<>? " +
                "GROUP BY a.user_id ORDER BY COUNT(a.user_id) DESC LIMIT 1) MaxLikesCount ON 1 = 1 " +
                "LEFT JOIN film_likes fl2 ON fl.film_id = fl2.film_id AND fl2.user_id = ? " +
                "WHERE fl.user_id <> ? AND UserLikesCount.likes_count = MaxLikesCount.max_likes AND fl2.user_id IS NULL";

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sqlQuery, id, id, id, id, id, id);
        Set<Film> recomendatedFilms = new HashSet<>();
        while (rowSet.next()) {
            Film film = dbFilmService.getById(rowSet.getLong("film_id"));
            recomendatedFilms.add(film);
        }
        return recomendatedFilms;
    }

    @Override
    public boolean checkExist(Long id) throws NotFoundException {
        String sqlQuery = "SELECT id FROM users WHERE id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (!rowSet.next()) {
            throw new NotFoundException(String.format("User ID = %d does not exist", id));
        }
        return true;
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();
    }
}
