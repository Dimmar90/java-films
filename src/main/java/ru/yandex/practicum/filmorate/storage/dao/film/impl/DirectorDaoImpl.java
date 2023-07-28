package ru.yandex.practicum.filmorate.storage.dao.film.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.dao.film.DirectorDao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component()
@RequiredArgsConstructor
public class DirectorDaoImpl implements DirectorDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Director create(Director director) {
        String sqlQuery = "INSERT INTO directors (name) VALUES (?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, new String[]{"id"});
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        director.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return director;
    }

    @Override
    public Director update(Director director) {
        String sqlQuery = "UPDATE directors SET name = ? WHERE id = ?";
        jdbcTemplate.update(sqlQuery, director.getName(), director.getId());
        return director;
    }

    @Override
    public List<Director> findAll() {
        String sqlQuery = "SELECT * FROM directors";
        return jdbcTemplate.query(sqlQuery, this::mapRowToDirector);
    }

    @Override
    public Optional<Director> findById(Long id) {
        if (checkExist(id)) {
            String sqlQuery = "SELECT * FROM directors WHERE id = ?";
            return Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery, this::mapRowToDirector, id));
        }
        return Optional.empty();
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM directors WHERE id = ?", id);
    }

    @Override
    public void addToFilm(Long directorId, Long filmId) {
        String sqlQuery = "INSERT INTO film_directors (director_id,film_id) VALUES (?,?)";
        jdbcTemplate.update(sqlQuery, directorId, filmId);
    }

    @Override
    public void deleteFromFilm(Long filmId) {
        String sqlQuery = "DELETE FROM film_directors WHERE film_id =?";
        jdbcTemplate.update(sqlQuery, filmId);
    }

    @Override
    public void addDirectorsToFilm(Film film) {
        String sql = "SELECT d.id, d.name " +
                "FROM film_directors fd LEFT JOIN directors d ON fd.director_id = d.id " +
                "WHERE fd.film_id = ? " +
                "ORDER BY d.id";
        HashSet<Director> listOfDirectors = new HashSet<>(jdbcTemplate.query(sql, this::mapRowToDirector, film.getId()));
        film.setDirectors(listOfDirectors);
    }

    @Override
    public boolean checkExist(Long id) throws NotFoundException {
        String sqlQuery = "SELECT id FROM directors WHERE id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (!rowSet.next()) {
            throw new NotFoundException(String.format("Director ID = %d does not exist", id));
        }
        return true;
    }

    private Director mapRowToDirector(ResultSet rs, int rowNum) throws SQLException {
        return Director.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .build();
    }
}