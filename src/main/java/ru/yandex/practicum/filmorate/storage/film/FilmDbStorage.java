package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmLikes;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
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
        try {
            List<Integer> filmsId = getAllIdFilms();
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

    public Optional<Film> findFilmById(int idFilm) {
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
                String sqlGetGenreForFilm = "SELECT g.id AS id, " +
                        "       g.name AS name " +
                        "  FROM film_genre AS fg " +
                        "  LEFT " +
                        "  JOIN genre g " +
                        "    ON fg.genre_id = g.id " +
                        " WHERE film_id = ? " +
                        " ORDER " +
                        "    BY id";
                Set<FilmGenre> filmGenres = jdbcTemplate.queryForStream(sqlGetGenreForFilm,
                        filmGenreRowMapper(), idFilm).collect(Collectors.toSet());
                ;
                int countGenres = filmGenres.size();
                Film foundFilm = films.get(0);

                if (countGenres < 1) {
                    log.debug("Фильму еще не присвоено жанров");
                } else {
                    log.debug("У фильма [COUNT={}] жанров", countGenres);
                    foundFilm.setGenres(filmGenres.stream()
                            .sorted(Comparator.comparingInt(FilmGenre::getId))
                            .collect(Collectors.toCollection(LinkedHashSet::new)));

                }
                return Optional.ofNullable(foundFilm);
            }

        } catch (DataAccessException e) {
            log.error(e.getMessage());
            throw new NotFoundException(String.format("Film with ID=%d", idFilm));
        }
    }

    public Optional<Film> addFilm(Film film) {
        log.debug("{} на добавление нового фильма", DB_RUNNING);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sqlAddFilm = "INSERT INTO film (title, description, release_date, duration, rating) " +
                " VALUES(?, ?, ?, ?, ?)";

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
                UpdateGenresByListFilm(filmIdGenres, newFilmId);
            }
            return findFilmById(newFilmId);
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            throw new NotCreatedException("New Film");
        }
    }

    public Optional<Film> updateFilm(Film film, Set<FilmGenre> genres) {
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
                    film.getMpa().getName().toString(),
                    film.getId()
            );

            if (film.getGenres().equals(genres)) {
                log.debug("{} обновлен фильм [ID={}], новых данных для обновления жанров нет",
                        DB_SUCCESS, filmId);
            } else {
                log.debug("{} на удаление [COUNT={}] жанров", DB_RUNNING, genres.size());
                genres.forEach(filmGenre -> removeGenreByFilm(filmId, filmGenre.getId()));

                UpdateGenresByListFilm(film.getGenres(), filmId);
                log.debug("{} обновлен фильм [ID={}] и жанры", DB_SUCCESS, filmId);
            }
            return findFilmById(filmId);
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            throw new NotFoundException(String.format("Film with ID=%d", filmId));
        }
    }

    public FilmLikes addLikeByUserId(FilmLikes film, int userId) {
        int idFilm = film.getFilm().getId();
        Set<Integer> listLikes = getAllLikesByFilm(idFilm);

        if (listLikes.contains(userId)) {
            log.error("У фильма [ID={}] уже есть лайк [ID={}]", idFilm, userId);
            throw new AlreadyExistsException(String.format("Like by user ID=%d", userId));
        }
        try {
            log.debug("{} на добавление лайка фильму [ID={}] пользователем [ID={}]", DB_RUNNING, idFilm, userId);
            String sqlInsertLike = "INSERT INTO film_likes (film_id, person_id) " +
                    " VALUES(?, ?)";
            jdbcTemplate.update(sqlInsertLike, idFilm, userId);
            film.setLikes(getAllLikesByFilm(idFilm));
            return film;

        } catch (DataAccessException e) {
            log.error(e.getMessage());
            log.error("Ошибка БД. Фильму [ID={}] пользователь [ID={}] лайк не поставил", idFilm, userId);
            throw new NotCreatedException(String.format("New like for film ID=%d", idFilm));
        }
    }

    public FilmLikes removeLikeByUserId(FilmLikes film, int userId) {
        int idFilm = film.getFilm().getId();
        Set<Integer> listLikes = getAllLikesByFilm(idFilm);

        if (listLikes.contains(userId)) {
            try {
                log.debug("{} на удаление лайка фильму [ID={}] пользователем [ID={}]", DB_RUNNING, idFilm, userId);
                String sqlRemoveLike = "DELETE FROM film_likes " +
                        " WHERE film_id = ? " +
                        "   AND person_id = ?";
                jdbcTemplate.update(sqlRemoveLike, idFilm, userId);
                film.setLikes(getAllLikesByFilm(idFilm));
                return film;
            } catch (DataAccessException e) {
                log.error(e.getMessage());
                log.error("Ошибка БД. Фильму [ID={}] пользователь [ID={}] лайк не поставил", idFilm, userId);
                throw new NotRemovedException(String.format("Like for film ID=%d", idFilm));
            }
        } else {
            log.error("У фильма [ID={}] уже удален лайк [ID={}]", idFilm, userId);
            throw new AlreadyExistsException(String.format("Like by user ID=%d removed", userId));
        }
    }

    private List<Integer> getAllIdFilms() {
        log.debug("{} на получение [ID] всех фильмов", DB_RUNNING);
        String sqlFindAllIdFilm = "SELECT id " +
                "  FROM film";
        return jdbcTemplate.queryForList(sqlFindAllIdFilm, Integer.class);
    }

    public List<Film> getPopularFilms(int count) {
        log.debug("{} на получение списка [COUNT={}] популярных фильмов", DB_RUNNING, count);
        List<Integer> filmsId = getAllIdFilms();
        int resultCount = filmsId.size();
        log.debug("найдено {} айдишников", resultCount);
        if (resultCount > 0) {
            log.debug("Найдено [COUNT={}] популярных фильмов, БД формирует запрос", resultCount);
            List<Film> film = filmsId.stream()
                    .map(this::findFilmById)
                    .filter(Optional::isPresent)
                    .map(Optional::get).collect(Collectors.toList());

            Set<FilmLikes> filmLikes = film.stream()
                    .map(FilmLikes::new).collect(Collectors.toSet());
            filmLikes.forEach(f -> f.setLikes(getAllLikesByFilm(f.getFilm().getId())));

            return filmLikes.stream()
                    .sorted(Comparator.comparingInt(FilmLikes::getCountLikes).reversed())
                    .collect(Collectors.toCollection(LinkedHashSet::new)).stream()
                    .limit(count)
                    .map(FilmLikes::getFilm)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private Set<Integer> getAllLikesByFilm(int idFilm) {
        log.debug("{} на получение списка лайков фильма [ID={}]", DB_RUNNING, idFilm);
        String sqlSelectIdUserByLiked = "SELECT person_id " +
                "  FROM film_likes " +
                " WHERE film_id = ?";
        try {
            List<Integer> getQueryInteger = jdbcTemplate.queryForList(sqlSelectIdUserByLiked,
                    Integer.class, idFilm);
            int resultCount = getQueryInteger.size();

            if (resultCount < 1) {
                log.debug("Лайков фильму [ID={}] еще никто не поставил", idFilm);
                return Collections.emptySet();
            } else {
                log.debug("У фильма [ID={}] найдено [COUNT={}] лайков", idFilm, resultCount);
                return getQueryInteger.stream().distinct().collect(Collectors.toSet());
            }
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            throw new UnknownException("Get Likes for Film");
        }
    }

    private void UpdateGenresByListFilm(Set<FilmGenre> filmIdGenres, int idFilm) {
        log.debug("{} на добавление [COUNT={}] жанров", DB_RUNNING, filmIdGenres.size());
        filmIdGenres.forEach(filmGenre -> addGenreByFilm(idFilm, filmGenre.getId()));
    }

    private void addGenreByFilm(int idFilm, int idGenre) {
        log.debug("{} на добавление нового жанра [ID={}] к фильму [ID={}]", DB_RUNNING, idGenre, idFilm);
        String sqlAddGenresForFilm = "INSERT INTO film_genre (film_id, genre_id) " +
                " VALUES(?, ?) ";
        try {
            jdbcTemplate.update(sqlAddGenresForFilm, idFilm, idGenre);
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            throw new NotCreatedException("Genre for Film");
        }
    }

    private void removeGenreByFilm(int idFilm, int idGenre) {
        log.debug("{} на удаление жанра [ID={}] из фильма [ID={}]", DB_RUNNING, idGenre, idFilm);
        String sqlRemoveGenreByFilm = "DELETE FROM film_genre " +
                " WHERE film_id = ? " +
                "   AND genre_id= ? ";
        try {
            jdbcTemplate.update(sqlRemoveGenreByFilm, idFilm, idGenre);
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            throw new NotRemovedException("Genre for Film");
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
