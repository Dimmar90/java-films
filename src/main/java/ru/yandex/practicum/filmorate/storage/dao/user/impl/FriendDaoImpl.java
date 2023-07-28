package ru.yandex.practicum.filmorate.storage.dao.user.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.user.FriendDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FriendDaoImpl implements FriendDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void add(Long id, Long friendId) {
        String sqlQuery = "MERGE INTO friendship (user_id, friend_user_id) VALUES (?,?)";
        jdbcTemplate.update(sqlQuery, id, friendId);
    }

    @Override
    public void delete(Long id, Long friendId) {
        String sqlQuery = "DELETE FROM friendship WHERE user_id = ? AND friend_user_id = ?";
        jdbcTemplate.update(sqlQuery, id, friendId);
    }

    @Override
    public List<User> findCommon(Long id, Long otherId) { //Проверить
        String sqlQuery = "SELECT * FROM users u " +
                "WHERE u.id IN (SELECT friend_user_id FROM friendship fs WHERE fs.user_id = ?) " +
                "AND u.id IN (SELECT friend_user_id FROM friendship fs WHERE fs.user_id = ?)";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, id, otherId);
        return getUsersFromRowSet(rs).stream().distinct().collect(Collectors.toList());
    }

    @Override
    public List<User> findAll(Long id) {
        String sqlQuery = "SELECT * FROM users WHERE id IN " +
                "(SELECT friend_user_id FROM friendship WHERE user_id = ?)";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, id);
        return getUsersFromRowSet(rs);
    }

    private List<User> getUsersFromRowSet(SqlRowSet rs) {
        List<User> users = new ArrayList<>();
        while (rs.next()) {
            users.add(
                    User.builder()
                            .id(rs.getLong("id"))
                            .email(rs.getString("email"))
                            .login(rs.getString("login"))
                            .name(rs.getString("name"))
                            .birthday(Objects.requireNonNull(rs.getDate("birthday")).toLocalDate())
                            .build()
            );
        }
        return users;
    }
}
