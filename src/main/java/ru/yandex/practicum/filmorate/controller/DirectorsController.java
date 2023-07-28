package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/directors")
@Slf4j
@RequiredArgsConstructor
public class DirectorsController {
    private final DirectorService dbDirectorService;

    @PostMapping
    public Director create(@Valid @RequestBody Director director) {
        return dbDirectorService.create(director);
    }

    @PutMapping
    public Director update(@Valid @RequestBody Director director) {
        return dbDirectorService.update(director);
    }

    @GetMapping
    public List<Director> getDirectors() {
        return dbDirectorService.getAll();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable("id") Long directorId) {
        return dbDirectorService.getById(directorId);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable("id") Long directorId) {
        dbDirectorService.deleteDirector(directorId);
    }
}
