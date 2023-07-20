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
public class DBDirectorService {
    private final DirectorDao directorDao;

    @Autowired
    public DBDirectorService(DirectorDao directorDao) {
        this.directorDao = directorDao;
    }

    public Director create(Director director) {
        Director newDirector = directorDao.createDirector(director);
        log.info("{} has been CREATED", director);
        return newDirector;
    }

    public Director update(Director director) {
        if (director.getId() == null || !directorDao.checkDirectorExist(director.getId())) {
            throw new NotFoundException("Cannot update director with ID = null");
        }
        log.info("Director {} has been UPDATED", director);
        return directorDao.updateDirector(director);
    }

    public List<Director> getDirectors() {
        return directorDao.findAllDirectors();
    }

    public Director getDirector(Integer id) {
        directorDao.checkDirectorExist(id);
        log.info("Get a director with ID = {}", id);
        return directorDao.findDirectorById(id);
    }

    public void deleteDirector(Integer id) {
        directorDao.checkDirectorExist(id);
        log.info("Deleted director with ID = {}", id);
        directorDao.deleteDirector(id);
    }
}
