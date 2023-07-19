package ru.yandex.practicum.filmorate.storage.dao.user;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface EventDao {
    void addEvent(Integer userId, String eventType, String operation, Integer entityId);

    List<Event> getEventFeed(Integer userId);
}