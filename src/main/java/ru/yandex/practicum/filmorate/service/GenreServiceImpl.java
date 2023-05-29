package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.constants.NameLogs.SERVICE_FROM_DB;
import static ru.yandex.practicum.filmorate.constants.NameLogs.SERVICE_IN_DB;

@Slf4j
@Service
public class GenreServiceImpl implements GenreService {
    private final GenreStorage genreStorage;

    @Autowired
    public GenreServiceImpl(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public List<FilmGenre> getAllGenre() {
        log.debug("{} на получение всех жанров", SERVICE_IN_DB);
        List<FilmGenre> listFilmGenre = genreStorage.getAllGenre();
        if (listFilmGenre.isEmpty()) {
            log.error("{} получен пустой список жанров", SERVICE_FROM_DB);
        } else {
            log.info("{} успешно список из [COUNT={}] жанров.", SERVICE_FROM_DB, listFilmGenre.size());
        }
        return listFilmGenre;
    }

    public FilmGenre getGenresById(int idGenre) {
        log.debug("{} на получение жанра [ID={}]", SERVICE_IN_DB, idGenre);
        Optional<FilmGenre> filmGenre = genreStorage.getGenresById(idGenre);
        if (filmGenre.isEmpty()) {
            log.error("{} жанр не найден [ID={}]", SERVICE_FROM_DB, idGenre);
            throw new NotFoundException(String.format("Genre with ID=%d", idGenre));
        } else {
            log.info("{} успешно жанр [ID={}]", SERVICE_FROM_DB, idGenre);
        }
        return filmGenre.get();
    }
}
