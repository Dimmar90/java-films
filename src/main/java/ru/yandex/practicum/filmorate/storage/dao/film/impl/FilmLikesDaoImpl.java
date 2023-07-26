package ru.yandex.practicum.filmorate.storage.dao.film.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmLikesDao;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FilmLikesDaoImpl implements FilmLikesDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void like(Integer id, Integer userId) {
        if (cheсkLikeByUser(id, userId)) {
            String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
            jdbcTemplate.update(sql, id, userId);
        }
    }

    @Override
    public void unlike(Integer id, Integer userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, id, userId);
    }

    private boolean cheсkLikeByUser(Integer id, Integer userId) {
        String sql = "select film_id from film_likes where user_id = ?";
        List<Integer> allLikedFilms = jdbcTemplate.query(sql, new Object[]{userId},
                (rs, rowNum) -> rs.getInt("film_id"));
        return !allLikedFilms.contains(id);
    }
}
