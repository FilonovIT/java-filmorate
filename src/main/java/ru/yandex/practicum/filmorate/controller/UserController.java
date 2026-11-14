package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserController.class);

    @GetMapping
    public Collection<User> getUsers() {
        log.info("Запрошен весь список пользователей!");
        return users.values();
    }

    private void validateUser(User user) {
        String email = user.getEmail();
        if (email == null || email.isBlank()) {
            log.warn("Валидация не пройдена: -> {}", user.getEmail());
            throw new ValidationException("Электронная почта не может быть пустой!");
        }

        if (!email.contains("@")) {
            log.warn("Валидация не пройдена: -> {}", user.getEmail());
            throw new ValidationException("Почта должна содержать символ @!");
        }

        String login = user.getLogin();
        if (login == null || login.isBlank()) {
            log.warn("Валидация не пройдена: -> {}", user.getLogin());
            throw new ValidationException("Логин не может быть пустым!");
        }
        if (login.contains(" ")) {
            log.warn("Валидация не пройдена: -> {}", user.getLogin());
            throw new ValidationException("Логин не может быть пустым!");
        }


        LocalDate birthday = user.getBirthday();
        if (birthday == null) {
            log.warn("Валидация не пройдена: -> дата рождения должна присутствовать!");
            throw new ValidationException("Дата рождения должна присутствовать!");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Валидация не пройдена: -> {}", birthday);
            throw new ValidationException("Дата рождения не может быть в будущем!");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(login);
        }
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        validateUser(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User newUser) {
        Long id = newUser.getId();
        if (id == null) {
            log.warn("Валидация не пройдена, не передан id!");
            throw new ValidationException("Id должен быть передан!");
        }

        if (!users.containsKey(id)) {
            log.warn("Пользователь с id={} не найден — обновление невозможно", id);
            throw new ValidationException("Пользователь с Id " + id + " не найден!");
        }

        validateUser(newUser);

        users.put(id, newUser);
        log.info("Пользователь с {} обновлен!", id);

        return newUser;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return currentMaxId + 1;
    }
}