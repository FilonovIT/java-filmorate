package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    final static Instant DATE_RELEASE = LocalDate.of(1895, 12, 28)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant();
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Имя фильма не может быть пустым!");
        }

        String description = film.getDescription();
        if (description != null && description.length() > 200) {
            throw new ValidationException("Максимальная длина описания равна 200 символов!");
        }

        Instant releaseDate = film.getReleaseDate();
        if (releaseDate == null || releaseDate.isBefore(DATE_RELEASE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    @PostMapping("/add-film")
    public Film addFilm(@RequestBody Film film) {
        validateFilm(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping("/update-film")
    public Film updateFilm(@RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан!");
        }

        validateFilm(newFilm);

        if (!films.containsKey(newFilm.getId())) {
            throw new ValidationException("Пользователь с Id " +  newFilm.getId() + " не найден!");
        }

        films.put(newFilm.getId(), newFilm);
        return newFilm;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }


}
