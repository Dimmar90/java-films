package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * электронная почта не может быть пустой и должна содержать символ @;
 * логин не может быть пустым и содержать пробелы;
 * имя для отображения может быть пустым — в таком случае будет использован логин;
 * дата рождения не может быть в будущем.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    private Long id;
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email must be completed")
    private String email;
    @NotBlank(message = "Login must be completed")
    private String login;
    @NotNull(message = "Name must be completed")
    private String name;
    @NotNull(message = "The birthday can't be empty")
    @PastOrPresent(message = "Birthdate cannot be in the future")
    private LocalDate birthday;
    @Setter(AccessLevel.NONE)
    private Set<Integer> friends = new HashSet<>();
}
