package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotCreatedException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.NotUpdatedException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.constants.NameLogs.DB_RUNNING;
import static ru.yandex.practicum.filmorate.constants.NameLogs.DB_SUCCESS;

@Slf4j
@Component
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Film> getAllFilms() {
        log.debug("{} на получение списка всех фильмов", DB_RUNNING);
        String sqlFindAllIdFilm = "SELECT id " +
                "  FROM film";
        try {
            List<Integer> filmsId = jdbcTemplate.queryForList(sqlFindAllIdFilm, Integer.class);
            int resultCount = filmsId.size();

            if (resultCount < 1) {
                log.debug("Список фильмов пуст");
                return Collections.emptyList();
            } else {
                return filmsId.stream()
                        .map(this::findFilmById)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
            }
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            throw new NotFoundException("All Films");
        }
    }

    public Optional<Film> findFilmById(Integer idFilm) {
        log.debug("{} на получение данных фильма с [ID={}]", DB_RUNNING, idFilm);
        String sqlFindFilm = "SELECT * " +
                "  FROM film " +
                " WHERE id = ?";
        try {
            List<Film> films = jdbcTemplate.query(sqlFindFilm, filmRowMapper(), idFilm);
            int countFilms = films.size();

            if (countFilms > 1) {
                log.error("Ожидался список из 1 фильма, а получился [COUNT={}]", countFilms);
                throw new NotFoundException(String.format("Film with ID=%d", idFilm));
            } else if (countFilms < 1) {
                log.warn("Фильм с [ID={}] не найден", idFilm);
                throw new NotFoundException(String.format("Film with ID=%d", idFilm));
            } else {
                log.debug("{} на получение списка жанров для фильма [ID={}]", DB_RUNNING, idFilm);
                String sqlGetGenreForFilm = "SELECT g.id AS id,      \n" +
                        "       g.name AS name\n" +
                        "  FROM film_genre AS fg\n" +
                        "  LEFT\n" +
                        "  JOIN genre g \n" +
                        "    ON fg.genre_id = g.id \n" +
                        " WHERE film_id = ?\n" +
                        " ORDER \n" +
                        "    BY id";
                Set<FilmGenre> filmGenres = jdbcTemplate
                        .queryForStream(sqlGetGenreForFilm, filmGenreRowMapper(), idFilm)
                        .collect(Collectors.toSet());
                int countGenres = filmGenres.size();

                if (countGenres > 0) {
                    log.debug("У фильма [COUNT={}] жанров", countGenres);
                    films.get(0).setGenres(filmGenres);
                } else {
                    log.debug("Фильму еще не присвоено жанров");
                }
            }
            return Optional.ofNullable(films.get(0));
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            throw new NotFoundException(String.format("Film with ID=%d", idFilm));
        }
    }

    public Optional<Film> addFilm(Film film) {
        log.debug("прежде чем начать, жанр у фильма: {}", film.getGenres()); // убрать перед сдачей
        log.debug("прежде чем начать, рейтинг у фильма: {}", film.getMpa());
        log.debug("{} на добавление нового фильма", DB_RUNNING);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sqlAddFilm = "INSERT INTO film (title, description, release_date, duration, rating) " +
                "     VALUES(?, ?, ?, ?, ?)";

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(sqlAddFilm, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, film.getName());
                statement.setString(2, film.getDescription());
                statement.setDate(3, Date.valueOf(film.getReleaseDate()));
                statement.setInt(4, (int) film.getDuration());
                statement.setString(5, film.getMpa().getName().toString());
                return statement;
            }, keyHolder);

            int newFilmId = keyHolder.getKey().intValue();
            log.debug("{} добавлен новый фильм [ID={}]", DB_SUCCESS, newFilmId);
            Set<FilmGenre> filmIdGenres = film.getGenres();
            if (filmIdGenres.isEmpty()) {
                log.debug("При добавлении нового фильма [ID={}] жанры не указаны", newFilmId);
            } else {
                log.debug("{} на добавление [COUNT={}] жанров", DB_RUNNING, filmIdGenres.size());
                for (FilmGenre idGenre : filmIdGenres) {
                    addGenreByFilm(newFilmId, idGenre.getId());
                }
            }
            return findFilmById(newFilmId);
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            throw new NotCreatedException("New Film");
        }
    }

    private void addGenreByFilm(int idFilm, int idGenre) {
        String sqlAddGenresForFilm = "INSERT INTO film_genre (film_id, genre_id)" +
                "VALUES(?, ?)";
        try {
            jdbcTemplate.update(sqlAddGenresForFilm, idFilm, idGenre);
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            throw new NotCreatedException("Genre for Film");
        }
    }

    public Optional<Film> updateFilm(Film film) {
        int filmId = film.getId();
        log.debug("{} на обновление фильма с [ID={}]", DB_RUNNING, filmId);

        String sqlUpdateFilm = "UPDATE film " +
                "   SET " +
                "       title = ?, " +
                "       description = ?, " +
                "       release_date = ?, " +
                "       duration = ?, " +
                "       rating = ? " +
                " WHERE id = ?";
        try {
            jdbcTemplate.update(sqlUpdateFilm,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getMpa().getId(),
                    film.getId()
            );
            log.debug("{} обновлен фильм [ID={}]", DB_SUCCESS, sqlUpdateFilm);
            return findFilmById(filmId);
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            throw new NotFoundException(String.format("Film with ID=%d", filmId));
        }
    }

    private RowMapper<Film> filmRowMapper() {
        return (rs, rowNum) -> new Film(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getDate("release_date").toLocalDate(),
                rs.getLong("duration"),
                rs.getString("rating"));
    }

    private RowMapper<FilmGenre> filmGenreRowMapper() {
        return (rs, rowNum) -> new FilmGenre(
                rs.getInt("id"),
                rs.getString("name"));
    }
}
