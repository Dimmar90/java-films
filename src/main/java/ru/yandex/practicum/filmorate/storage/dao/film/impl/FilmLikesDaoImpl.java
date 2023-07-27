package ru.yandex.practicum.filmorate.storage.dao.film.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmLikesDao;

@Component
@RequiredArgsConstructor
public class FilmLikesDaoImpl implements FilmLikesDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void like(Integer id, Integer userId) {
        String sql = "MERGE INTO film_likes KEY (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, id, userId);
    }

    @Override
    public void unlike(Integer id, Integer userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, id, userId);
    }
}
