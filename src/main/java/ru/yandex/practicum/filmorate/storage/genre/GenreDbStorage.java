package ru.yandex.practicum.filmorate.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FilmGenre;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<FilmGenre> getAllGenre() {
        String sqlSelectAllGenre = "SELECT id," +
                "      name " +
                " FROM genre";
        try {
            List<FilmGenre> getAllGenre = jdbcTemplate.query(sqlSelectAllGenre,
                    filmGenreRowMapper());
            int resultCount = getAllGenre.size();

            if (resultCount < 1) {
                log.error("Возможная ошибка БД");
                throw new NotFoundException("All Genre");
            } else {
                log.debug("Жанры найдены");
                return getAllGenre;
            }
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            throw new NotFoundException("All genre");
        }
    }

    public Optional<FilmGenre> getGenresById(int idGenre) {
        try {
            String sqlSelectGenreById = "SELECT id," +
                    "      name " +
                    " FROM genre " +
                    "WHERE id = ?";

            List<FilmGenre> getGenreById = jdbcTemplate.query(sqlSelectGenreById,
                    filmGenreRowMapper(), idGenre);
            int resultCount = getGenreById.size();

            if (resultCount < 1) {
                log.debug("Жанр с таким [ID={}] не найден", idGenre);
                throw new NotFoundException("Genre by ID");
            } else {
                log.debug("Жанр [ID={}] найден", idGenre);
                return Optional.ofNullable(getGenreById.get(0));
            }
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            throw new NotFoundException("Genre by ID");
        }
    }

    private RowMapper<FilmGenre> filmGenreRowMapper() {
        return (rs, rowNum) -> new FilmGenre(
                rs.getInt("id"),
                rs.getString("name"));
    }
}
