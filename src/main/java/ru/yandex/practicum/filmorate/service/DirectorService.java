package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.dao.film.DirectorDao;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectorService {
    private final DirectorDao directorDao;

    public Director create(Director director) {
        Director newDirector = directorDao.create(director);
        log.info("{} has been CREATED", director);
        return newDirector;
    }

    public Director update(Director director) {
        if (director.getId() == null || !directorDao.checkExist(director.getId())) {
            throw new NotFoundException("Cannot update director with ID = null");
        }
        log.info("Director {} has been UPDATED", director);
        return directorDao.update(director);
    }

    public List<Director> getAll() {
        return directorDao.findAll();
    }

    public Director getById(Long id) {
        log.info("Get a director with ID = {}", id);
        return directorDao.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Director ID = %d does not exist", id)));
    }

    public void deleteDirector(Long id) {
        directorDao.checkExist(id);
        log.info("Deleted director with ID = {}", id);
        directorDao.delete(id);
    }
}
