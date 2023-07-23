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
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmDao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component("filmDaoImpl")
@RequiredArgsConstructor
public class FilmDaoImpl implements FilmDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film createFilm(Film film) {
        String sqlQuery = "INSERT INTO films (name, description, duration, releaseDate, mpa_id) VALUES (?,?,?,?,?)";
        Integer mpaId = film.getMpa().getId();
        KeyHolder id = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setInt(3, film.getDuration());
            ps.setDate(4, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(5, mpaId);
            return ps;
        }, id);

        film.setId(Objects.requireNonNull(id.getKey()).intValue());
        String mpaName = "SELECT mpa_id, name FROM mpa WHERE mpa_id = ?";
        Mpa mpa = jdbcTemplate.queryForObject(mpaName, this::mapRowToMpa, mpaId);
        film.setMpa(mpa);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        Integer mpaId = film.getMpa().getId();
        String sqlQuery = "UPDATE films SET " +
                "name = ?," +
                "description = ?," +
                "duration = ?," +
                "releaseDate = ?," +
                "mpa_id = ?" +
                "WHERE id = ?";
        jdbcTemplate.update(sqlQuery, film.getName(), film.getDescription(), film.getDuration(), film.getReleaseDate(),
                mpaId, film.getId());

        String mpaName = "SELECT mpa_id, name FROM mpa WHERE mpa_id = ?";
        Mpa mpa = jdbcTemplate.queryForObject(mpaName, this::mapRowToMpa, mpaId);
        film.setMpa(mpa);
        return film;
    }

    @Override
    public List<Film> getFilms() {
        String sqlQuery = "SELECT * FROM films";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm);
    }

    @Override
    public Film getById(Integer id) {
        String sqlQuery = "SELECT * FROM films WHERE id = ?";
        return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToFilm, id);
    }

    public void deleteFilmById(Integer filmId) {
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", filmId);
    }

    @Override
    public List<Film> getTopFilms(Integer count, Integer genreId, Integer year) {
        List<String> params = new ArrayList<>();

        String sqlQuery = "SELECT f.*, m.name AS mpa_name FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN film_likes AS lk ON f.id = lk.film_id " +
                "LEFT JOIN film_genres AS fg ON f.id = fg.film_id %s " +
                "GROUP BY f.id ORDER BY COUNT(lk.user_id) DESC LIMIT ?";

        if (genreId != null) params.add(String.format("genre_id = %s", genreId));
        if (year != null) params.add(String.format("year(releaseDate) = %s", year));

        String sqlParams = !params.isEmpty() ? "WHERE ".concat(String.join(" AND ", params)) : "";

        return jdbcTemplate.query(String.format(sqlQuery, sqlParams), this::mapRowToFilm, count);
    }

    @Override
    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        String sqlQuery = "SELECT f.* FROM films f " +
                "JOIN film_likes fl1 ON fl1.film_id = f.id " +
                "JOIN film_likes fl2 ON fl2.film_id = f.id " +
                "WHERE fl1.user_id = ? AND fl2.user_id = ? " +
                "GROUP BY f.id " +
                "ORDER BY (SELECT COUNT(user_id) FROM film_likes WHERE film_id = f.id) DESC";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm, userId, friendId);
    }

    @Override
    public List<Film> findDirectorsFilmsSortedByRate(Integer directorId) {

        String sql = "SELECT f.id, f.name, f.description, f.duration, f.releaseDate, f.mpa_id, COUNT(fl.user_id) AS rate " +
                "FROM film_directors fd LEFT JOIN films f ON fd.film_id = f.id LEFT JOIN film_likes fl ON fd.film_id = fl.film_id " +
                "WHERE fd.director_id = ? " +
                "GROUP BY fd.film_id " +
                "ORDER BY rate DESC";
        return jdbcTemplate.query(sql, this::mapRowToFilm, directorId);
    }

    @Override
    public List<Film> findDirectorsFilmsSortedByYears(Integer directorId) {
        String sql = "SELECT f.id, f.name, f.description, f.duration, f.releaseDate, f.mpa_id " +
                "FROM film_directors fd LEFT JOIN films f ON fd.film_id = f.id " +
                "WHERE fd.director_id =? " +
                "ORDER BY EXTRACT (YEAR FROM f.releaseDate)";
        return jdbcTemplate.query(sql, this::mapRowToFilm, directorId);
    }

    @Override
    public boolean checkFilmExist(Integer id) {
        String sqlQuery = "SELECT id FROM films WHERE id = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (!rowSet.next()) {
            throw new NotFoundException(String.format("Film ID = %d does not exist", id));
        }
        return true;
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        int mpaId = rs.getInt("mpa_id");
        String mpaName = "SELECT mpa_id, name FROM mpa WHERE mpa_id = ?";
        Mpa mpa = jdbcTemplate.queryForObject(mpaName, this::mapRowToMpa, mpaId);
        int filmId = rs.getInt("id");
        String sql = "SELECT genre_id, name FROM genres WHERE genre_id IN" +
                "(SELECT genre_id FROM film_genres WHERE film_id = ?)";
        Set<Genre> genres = new HashSet<>(jdbcTemplate.query(sql, this::mapRowToGenre, filmId));

        String directorSql = "SELECT d.DIRECTOR_ID, d.NAME \n" +
                "FROM FILM_DIRECTORS fd LEFT JOIN DIRECTORS d ON FD.DIRECTOR_ID = d.DIRECTOR_ID \n" +
                "WHERE FD .FILM_ID =?\n" +
                "ORDER BY d.DIRECTOR_ID ";
        Set<Director> directors = new HashSet<>(jdbcTemplate.query(directorSql, this::mapRowToDirector, filmId));

        return Film.builder()
                .id(filmId)
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .duration(rs.getInt("duration"))
                .releaseDate(rs.getDate("releaseDate").toLocalDate())
                .mpa(mpa)
                .genres(genres)
                .directors(directors)
                .build();
    }

    private Mpa mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        return new Mpa(rs.getInt("mpa_id"), rs.getString("name"));
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getInt("genre_id"), rs.getString("name"));
    }

    private Director mapRowToDirector(ResultSet rs, int rowNum) throws SQLException {
        return new Director(rs.getInt("director_id"), rs.getString("name"));
    }

    @Override
    public List<Film> search(String keyWord, String whereSearch) {
        String[] s = whereSearch.split(",");
        if(s.length == 2) {
            return searchByDirAndTitle(keyWord);
        } else {
            if(s[0].equals("director")) {
                return searchByDirector(keyWord);
            } else {
                return searchByTitle(keyWord);
            }
        }
    }

    private List<Film> searchByDirector(String keyWord) {
        String newSql = "SELECT f.*, m.name AS mpa_name FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN film_likes AS lk ON f.id = lk.film_id " +
                "LEFT JOIN film_directors AS fd on f.id = fd.film_id " +
                "LEFT JOIN directors AS d on fd.director_id = d.director_id " +
                "WHERE lower(d.name) LIKE lower(?)" +
                "GROUP BY f.id ORDER BY COUNT(lk.user_id) DESC";
        String keyWordForSql = "%" + keyWord + "%";
        return jdbcTemplate.query(newSql, new Object[]{keyWordForSql}, this::mapRowToFilm);
    }

    private List<Film> searchByTitle(String keyWord) {
        String newSql = "SELECT f.*, m.name AS mpa_name FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN film_likes AS lk ON f.id = lk.film_id " +
                "WHERE lower(f.name) LIKE lower(?)" +
                "GROUP BY f.id ORDER BY COUNT(lk.user_id) DESC";
        String keyWordForSql = "%" + keyWord + "%";
        return jdbcTemplate.query(newSql, new Object[]{keyWordForSql}, this::mapRowToFilm);
    }

    private List<Film> searchByDirAndTitle(String keyWord) {
        String newSql = "SELECT f.*, m.name AS mpa_name, d.name FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN film_likes AS lk ON f.id = lk.film_id " +
                "LEFT JOIN film_directors AS fd on f.id = fd.film_id " +
                "LEFT JOIN directors AS d on fd.director_id = d.director_id " +
                "WHERE lower(f.name) LIKE lower(?) or " +
                "lower(d.name) LIKE lower(?) " +
                "GROUP BY f.id ORDER BY COUNT(lk.user_id) DESC";
        String keyWordForSql = "%" + keyWord + "%";
        return jdbcTemplate.query(newSql, this::mapRowToFilm, keyWordForSql, keyWordForSql);
    }
}