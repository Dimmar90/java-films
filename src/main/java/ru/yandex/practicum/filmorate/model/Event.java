package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Event {
    private Long timestamp;
    private Integer userId;     // id пользователя
    private String eventType;   // одно из значениий LIKE, REVIEW или FRIEND
    private String operation;   // одно из значениий REMOVE, ADD, UPDATE
    private Integer eventId;    // pimary key - id произошедшего события
    private Integer entityId;   // id друга пользователя или id фильма
}
