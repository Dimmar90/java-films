package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.dao.film.DirectorDao;

import java.util.List;


@Service
@Slf4j
public class DirectorService {
    private final DirectorDao directorDao;

    @Autowired
    public DirectorService(DirectorDao directorDao) {
        this.directorDao = directorDao;
    }

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

    public Director getById(Integer id) {
        log.info("Get a director with ID = {}", id);
        return directorDao.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Director ID = %d does not exist", id)));
    }

    public void deleteDirector(Integer id) {
        directorDao.checkExist(id);
        log.info("Deleted director with ID = {}", id);
        directorDao.delete(id);
    }
}
