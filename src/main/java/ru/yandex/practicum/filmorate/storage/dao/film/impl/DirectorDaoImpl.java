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

@Component("directorDaoImpl")
@RequiredArgsConstructor
public class DirectorDaoImpl implements DirectorDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Director createDirector(Director director) {
        String sqlQuery = "INSERT INTO directors (name) VALUES (?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, new String[]{"director_id"});
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        director.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        String sqlQuery = "UPDATE directors SET " +
                "name = ?" +
                "WHERE director_id = ?";
        jdbcTemplate.update(sqlQuery, director.getName(), director.getId());
        return director;
    }

    @Override
    public List<Director> findAllDirectors() {
        String sqlQuery = "SELECT * FROM directors";
        return jdbcTemplate.query(sqlQuery, this::mapRowToDirector);
    }

    @Override
    public Director findDirectorById(Integer id) {
        String sqlQuery = "SELECT * FROM directors WHERE director_id = ?";
        return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToDirector, id);
    }

    @Override
    public void deleteDirector(Integer id) {
        String sqlDeleteFromDirectors = "DELETE FROM directors WHERE director_id = ?";
        String sqlDeleteFromFilmDirectors = "DELETE FROM FILM_DIRECTORS WHERE DIRECTOR_ID = ?";
        jdbcTemplate.update(sqlDeleteFromDirectors, id);
        jdbcTemplate.update(sqlDeleteFromFilmDirectors, id);
    }

    @Override
    public void addFilmToDirector(Integer directorId, Integer filmId) {
        String sqlQuery = "INSERT INTO FILM_DIRECTORS (DIRECTOR_ID,FILM_ID) VALUES (?,?)";
        jdbcTemplate.update(sqlQuery, directorId, filmId);
    }

    @Override
    public void deleteFilmFromDirector(Integer filmId) {
        String sqlQuery = "DELETE FROM FILM_DIRECTORS WHERE FILM_ID =?";
        jdbcTemplate.update(sqlQuery, filmId);
    }

    @Override
    public void addDirectorsListToFilm(Film film) {
        String sql = "SELECT d.DIRECTOR_ID, d.NAME \n" +
                "FROM FILM_DIRECTORS fd LEFT JOIN directors d ON FD.DIRECTOR_ID = d.DIRECTOR_ID \n" +
                "WHERE FD .FILM_ID =?\n" +
                "ORDER BY d.DIRECTOR_ID ";
        HashSet<Director> listOfDirectors = new HashSet<>(jdbcTemplate.query(sql, this::mapRowToDirector, film.getId()));
        film.setDirectors(listOfDirectors);
    }

    @Override
    public boolean checkDirectorExist(Integer id) {
        String sqlQuery = "SELECT director_id FROM directors WHERE director_id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (!rowSet.next()) {
            throw new NotFoundException(String.format("Director ID = %d does not exist", id));
        }
        return true;
    }

    private Director mapRowToDirector(ResultSet rs, int rowNum) throws SQLException {
        return Director.builder()
                .id(rs.getInt("director_id"))
                .name(rs.getString("name"))
                .build();
    }
}