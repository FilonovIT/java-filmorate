package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты валидации Filmorate")
class FilmorateApplicationTests {

	private FilmController filmController;
	private UserController userController;

	@BeforeEach
	void setUp() {
		filmController = new FilmController();
		userController = new UserController();
	}

	// ТЕСТЫ ДЛЯ ФИЛЬМОВ (Film)

	@Test
	@DisplayName("Фильм: валидные данные успешно добавляются")
	void shouldCreateValidFilm() {
		Film film = new Film();
		film.setName("Название");
		film.setDescription("Описание");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(120);

		Film created = filmController.addFilm(film);
		assertNotNull(created.getId(), "Id должен быть присвоен");
		assertEquals(1, filmController.getFilms().size());
	}

	@Test
	@DisplayName("Фильм: граница даты релиза (28.12.1895) — успех")
	void shouldAcceptMinReleaseDate() {
		Film film = new Film();
		film.setName("Старый фильм");
		film.setDescription("Описание");
		film.setReleaseDate(LocalDate.of(1895, 12, 28)); // Ровно граница
		film.setDuration(120);

		assertDoesNotThrow(() -> filmController.addFilm(film));
	}

	@Test
	@DisplayName("Фильм: дата релиза на 1 день раньше (27.12.1895) — ошибка")
	void shouldThrowExceptionForDateBeforeMin() {
		Film film = new Film();
		film.setName("Слишком старый");
		film.setDescription("Описание");
		film.setReleaseDate(LocalDate.of(1895, 12, 27)); // На день раньше
		film.setDuration(120);

		assertThrows(ValidationException.class, () -> filmController.addFilm(film));
	}

	@Test
	@DisplayName("Фильм: описание ровно 200 символов — успех")
	void shouldAccept200CharsDescription() {
		Film film = new Film();
		film.setName("Фильм");
		film.setDescription("A".repeat(200)); // Граница
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(120);

		assertDoesNotThrow(() -> filmController.addFilm(film));
	}

	@Test
	@DisplayName("Фильм: описание 201 символ — ошибка")
	void shouldThrowExceptionFor201CharsDescription() {
		Film film = new Film();
		film.setName("Фильм");
		film.setDescription("A".repeat(201)); // На 1 больше
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(120);

		assertThrows(ValidationException.class, () -> filmController.addFilm(film));
	}

	@Test
	@DisplayName("Фильм: пустое имя, нулевая продолжительность и null дата — ошибки")
	void shouldThrowExceptionsForInvalidFilmFields() {
		// Пустое имя
		Film f1 = new Film();
		f1.setName(" ");
		f1.setReleaseDate(LocalDate.of(2000, 1, 1));
		f1.setDuration(120);
		assertThrows(ValidationException.class, () -> filmController.addFilm(f1));

		// Нулевая продолжительность
		Film f2 = new Film();
		f2.setName("Фильм");
		f2.setReleaseDate(LocalDate.of(2000, 1, 1));
		f2.setDuration(0);
		assertThrows(ValidationException.class, () -> filmController.addFilm(f2));

		// Null дата
		Film f3 = new Film();
		f3.setName("Фильм");
		f3.setReleaseDate(null);
		f3.setDuration(120);
		assertThrows(ValidationException.class, () -> filmController.addFilm(f3));
	}

	@Test
	@DisplayName("Фильм: обновление без ID или с несуществующим ID — ошибка")
	void shouldThrowExceptionOnInvalidUpdate() {
		Film film = new Film();
		film.setName("Новый");
		film.setDescription("Описание");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(100);

		// Без ID (null)
		assertThrows(ValidationException.class, () -> filmController.updateFilm(film));

		// Несуществующий ID
		film.setId(999L);
		assertThrows(ValidationException.class, () -> filmController.updateFilm(film));
	}

	// ТЕСТЫ ДЛЯ ПОЛЬЗОВАТЕЛЕЙ (User)

	@Test
	@DisplayName("Пользователь: валидные данные успешно добавляются")
	void shouldCreateValidUser() {
		User user = new User();
		user.setEmail("test@mail.ru");
		user.setLogin("login");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		User created = userController.createUser(user);
		assertNotNull(created.getId(), "Id должен быть присвоен");
		assertEquals("login", created.getName(), "Имя должно подставиться из логина");
	}

	@Test
	@DisplayName("Пользователь: email без '@' и пустой логин — ошибки")
	void shouldThrowExceptionsForInvalidUserEmailAndLogin() {
		// Без @
		User u1 = new User();
		u1.setEmail("no-at-sign.ru");
		u1.setLogin("login");
		u1.setBirthday(LocalDate.of(1990, 1, 1));
		assertThrows(ValidationException.class, () -> userController.createUser(u1));

		// Пустой логин
		User u2 = new User();
		u2.setEmail("test@mail.ru");
		u2.setLogin(" ");
		u2.setBirthday(LocalDate.of(1990, 1, 1));
		assertThrows(ValidationException.class, () -> userController.createUser(u2));

		// Логин с пробелом внутри
		User u3 = new User();
		u3.setEmail("test@mail.ru");
		u3.setLogin("bad login");
		u3.setBirthday(LocalDate.of(1990, 1, 1));
		assertThrows(ValidationException.class, () -> userController.createUser(u3));
	}

	@Test
	@DisplayName("Пользователь: дата рождения в будущем и null — ошибки")
	void shouldThrowExceptionForFutureBirthday() {
		// Завтрашний день
		User u1 = new User();
		u1.setEmail("test@mail.ru");
		u1.setLogin("login");
		u1.setBirthday(LocalDate.now().plusDays(1)); // Граница +1 день
		assertThrows(ValidationException.class, () -> userController.createUser(u1));

		// Null
		User u2 = new User();
		u2.setEmail("test@mail.ru");
		u2.setLogin("login");
		u2.setBirthday(null);
		assertThrows(ValidationException.class, () -> userController.createUser(u2));
	}

	@Test
	@DisplayName("Пользователь: сегодняшняя дата рождения — успех (граница)")
	void shouldAcceptTodayBirthday() {
		User u1 = new User();
		u1.setEmail("test@mail.ru");
		u1.setLogin("login");
		u1.setBirthday(LocalDate.now()); // Ровно сегодня

		assertDoesNotThrow(() -> userController.createUser(u1));
	}

	@Test
	@DisplayName("Пользователь: обновление без ID или с несуществующим ID — ошибка")
	void shouldThrowExceptionOnInvalidUserUpdate() {
		User user = new User();
		user.setEmail("test@mail.ru");
		user.setLogin("login");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		// Без ID (null)
		assertThrows(ValidationException.class, () -> userController.updateUser(user));

		// Несуществующий ID
		user.setId(999L);
		assertThrows(ValidationException.class, () -> userController.updateUser(user));
	}
}