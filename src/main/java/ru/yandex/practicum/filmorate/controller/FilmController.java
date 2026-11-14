package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    final int lenghtDescription = 200;
    final LocalDate dateRelease = LocalDate.of(1895, 12, 28);
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getFilms() {
        log.info("Запрошен весь список фильмов!");
        return films.values();
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Валидация не пройдена: -> {}", film.getName());
            throw new ValidationException("Имя фильма не может быть пустым!");
        }

        String description = film.getDescription();
        if (description != null && description.length() > lenghtDescription) {
            log.warn("Валидация не пройдена: -> {}", description);
            throw new ValidationException("Максимальная длина описания равна 200 символов!");
        }

        LocalDate releaseDate = film.getReleaseDate();
        if (releaseDate == null || releaseDate.isBefore(dateRelease)) {
            log.warn("Валидация не пройдена: -> {}", releaseDate);
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() <= 0) {
            log.warn("Валидация не пройдена: -> {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

    }

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        validateFilm(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
        Long id = newFilm.getId();

        if (id == null) {
            log.warn("Валидация не пройдена, не передан id!");
            throw new ValidationException("Id должен быть передан!");
        }

        if (!films.containsKey(id)) {
            log.warn("Фильм с id={} не найден — обновление невозможно", id);
            throw new ValidationException("Фильм с Id " + id + " не найден!");
        }

        validateFilm(newFilm);

        films.put(id, newFilm);
        log.info("Фильм с {} обновлен!", id);

        return newFilm;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return currentMaxId + 1;
    }
}