package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FilmEnumRatingMPA;
import ru.yandex.practicum.filmorate.model.FilmRatingMPA;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RatingMPAServiceIml implements RatingMPAService {

    public List<FilmRatingMPA> getAllMPA() {
        Set<Integer> countRatingMPA = FilmEnumRatingMPA.getIntMPA().keySet();
        log.debug("Сервис выполняет формирование списка рейтинга");
        List<FilmRatingMPA> listMPA = countRatingMPA.stream().map(FilmRatingMPA::new).collect(Collectors.toList());
        if (listMPA.isEmpty()) {
            log.error("Получен пустой список рейтингов");
        } else {
            log.info("Успешно список из [COUNT={}] рейтингов.", listMPA.size());
        }
        return listMPA;
    }

    public FilmRatingMPA getMPAById(int idMPA) {
        log.debug("Сервис выполняет получение рейтинга [ID={}]", idMPA);
        FilmRatingMPA createdRating = new FilmRatingMPA(idMPA);
        if (createdRating.getId() == idMPA && createdRating.getName() != null) {
            log.info("Сервис вернул успешно рейтинг [ID={}]", idMPA);
        } else {
            log.error("Рейтинг не найден [ID={}]", idMPA);
            throw new NotFoundException(String.format("MPA rating with ID=%d", idMPA));
        }
        return createdRating;
    }
}
