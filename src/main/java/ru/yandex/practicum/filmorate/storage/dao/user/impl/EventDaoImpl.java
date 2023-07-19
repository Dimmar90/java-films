package ru.yandex.practicum.filmorate.storage.dao.user.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.dao.user.EventDao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.TreeSet;

@Component
@RequiredArgsConstructor
public class EventDaoImpl implements EventDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addEvent(Integer userId, String object, Integer entityId) {  // добавляю событие в БД
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        if (object.equals("LIKE") || object.equals("REVIEW") || object.equals("FRIEND")) {
            String sqlQuery = "INSERT INTO events (timestamp, userId, eventType, entityId) VALUES (?,?,?,?)";
            jdbcTemplate.update(sqlQuery, timestamp, userId, object, entityId);
        } else if (object.equals("REMOVE") || object.equals("ADD") || object.equals("UPDATE")) {
            String sqlQuery = "INSERT INTO events (timestamp, userId, operation, entityId) VALUES (?,?,?,?)";
            jdbcTemplate.update(sqlQuery, timestamp, userId, object, entityId);
        }
    }

    @Override
    public TreeSet<Event> getEventFeed(Integer userId) {   // получаю из БД список всех событий
        String sqlQuery = "SELECT * FROM events";
        return new TreeSet<>(jdbcTemplate.query(sqlQuery, this::mapRowToEvent));
    }

    private Event mapRowToEvent(ResultSet rs, int rowNum) throws SQLException {
        return Event.builder()
                .timestamp(rs.getTimestamp("timestamp"))
                .userId(rs.getInt("userId"))
                .eventType(rs.getString("eventType"))
                .operation(rs.getString("operation"))
                .eventId(rs.getInt("eventId"))
                .entityId(rs.getInt("entityId"))
                .build();
    }
}
