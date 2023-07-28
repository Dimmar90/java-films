package ru.yandex.practicum.filmorate.storage.dao.user;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface EventDao {
    void add(Long userId, String eventType, String operation, Long entityId);

    List<Event> findUserFeed(Long userId);
}