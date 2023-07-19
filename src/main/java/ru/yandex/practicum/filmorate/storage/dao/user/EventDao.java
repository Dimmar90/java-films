package ru.yandex.practicum.filmorate.storage.dao.user;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.TreeSet;

public interface EventDao {
    void addEvent(Integer userId, String object, Integer entityId);

    TreeSet<Event> getEventFeed(Integer userId);
}