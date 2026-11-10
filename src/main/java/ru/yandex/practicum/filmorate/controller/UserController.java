package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    private void validateUser(User user) {
        String email = user.getEmail();
        if (email == null || email.isBlank()) {
            throw new ValidationException("Электронная почта не может быть пустой!");
        }

        if (!email.contains("@")) {
            throw new ValidationException("Почта должна содержать символ @!");
        }

        String login = user.getLogin();
        if (login == null || login.isBlank()) {
            throw new ValidationException("Логин не может быть пустым!");
        }
        if (login.contains(" ")) {
            throw new ValidationException("Логин не может быть пустым!");
        }

        Instant now = Instant.now();
        Instant birthday = user.getBirthday();
        if (birthday == null) {
            throw new ValidationException("Дата рождения должна присутствовать!");
        }
        if (user.getBirthday().isAfter(now)) {
            throw new ValidationException("Дата рождения не может быть в будущем!");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(login);
        }
    }

    @PostMapping("/add-user")
    public User createUser(@RequestBody User user) {
        validateUser(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping("/update-user")
    public User updateUser(@RequestBody User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException("Id должен быть указан!");
        }

        validateUser(newUser);

        if (!users.containsKey(newUser.getId())) {
            throw new ValidationException("Пользователь с Id " +  newUser.getId() + " не найден!");
        }

        users.put(newUser.getId(), newUser);
        return newUser;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}