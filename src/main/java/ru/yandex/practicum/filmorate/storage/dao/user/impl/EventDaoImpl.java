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
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EventDaoImpl implements EventDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addEvent(Integer userId, String eventType, String operation, Integer entityId) {  // добавляю событие в БД
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        String sqlQuery = "INSERT INTO events (timestamp, userId, eventType, operation, entityId) VALUES (?,?,?,?,?)";
        jdbcTemplate.update(sqlQuery, timestamp, userId, eventType, operation, entityId);
    }

    @Override
    public List<Event> getEventFeed(Integer userId) {   // получаю из БД список всех событий
        String sqlQuery = "SELECT * FROM events WHERE userId=?";
        List<Event> events = jdbcTemplate.query(sqlQuery, this::mapRowToEvent, userId);
        return new ArrayList<>(jdbcTemplate.query(sqlQuery, this::mapRowToEvent, userId));
    }

    private Event mapRowToEvent(ResultSet rs, int rowNum) throws SQLException {
        return Event.builder()
                .timestamp(rs.getTimestamp("timestamp").toInstant().toEpochMilli())
                .userId(rs.getInt("userId"))
                .eventType(rs.getString("eventType"))
                .operation(rs.getString("operation"))
                .eventId(rs.getInt("eventId"))
                .entityId(rs.getInt("entityId"))
                .build();
    }
}
