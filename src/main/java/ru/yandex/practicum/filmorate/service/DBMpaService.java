package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.dao.film.MpaDao;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DBMpaService {
    private final MpaDao mpaDao;

    public Mpa getMpa(Integer id) throws NotFoundException {
        log.info("Get a MPA with ID = {}", id);
        return mpaDao.getMpa(id);
    }

    public List<Mpa> getAllMpa() {
        log.info("Get all MPA");
        return mpaDao.getAllMpa();
    }
}
