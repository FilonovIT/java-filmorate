package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@DisplayName("FilmController: валидация фильма")
class FilmorateApplicationTests {

	private FilmController controller;
	private Method validateFilm;

	private static final Instant MIN_RELEASE = LocalDate.of(1895, 12, 28)
			.atStartOfDay(ZoneOffset.UTC).toInstant();
	private static final Instant BEFORE_MIN_RELEASE = LocalDate.of(1895, 12, 27)
			.atStartOfDay(ZoneOffset.UTC).toInstant();

	@BeforeEach
	void setUp() throws NoSuchMethodException {
		controller = new FilmController();
		validateFilm = FilmController.class.getDeclaredMethod("validateFilm", Film.class);
		validateFilm.setAccessible(true);
	}

	private void invokeValidate(Film film) throws Exception {
		validateFilm.invoke(controller, film);
	}

	private Film validFilm() {
		Film film = new Film();
		film.setName("Valid Name");
		film.setDescription("Valid description");
		film.setReleaseDate(MIN_RELEASE);   // ровно граница — должно проходить
		film.setDuration(1);                // минимально допустимое
		return film;
	}

	@Nested
	@DisplayName("Поле name")
	class NameValidation {

		@Test
		@DisplayName("null → ValidationException")
		void nullName() {
			Film film = validFilm();
			film.setName(null);
			assertThatThrownBy(() -> invokeValidate(film))
					.isInstanceOf(ValidationException.class)
					.hasMessageContaining("Имя фильма");
		}

		@Test
		@DisplayName("пустая строка → ValidationException")
		void emptyName() {
			Film film = validFilm();
			film.setName("");
			assertThatThrownBy(() -> invokeValidate(film))
					.isInstanceOf(ValidationException.class);
		}

		@Test
		@DisplayName("только пробелы (\"   \") → ValidationException")
		void blankName() {
			Film film = validFilm();
			film.setName("   ");
			assertThatThrownBy(() -> invokeValidate(film))
					.isInstanceOf(ValidationException.class);
		}

		@Test
		@DisplayName("один символ — валидно")
		void singleCharName() {
			Film film = validFilm();
			film.setName("A");
			assertThatCode(() -> invokeValidate(film)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("имя с пробелами внутри — валидно")
		void nameWithSpaces() {
			Film film = validFilm();
			film.setName("The Great Film");
			assertThatCode(() -> invokeValidate(film)).doesNotThrowAnyException();
		}
	}

	@Nested
	@DisplayName("Поле description")
	class DescriptionValidation {

		@Test
		@DisplayName("null описание — валидно (проверка только по длине)")
		void nullDescription() {
			Film film = validFilm();
			film.setDescription(null);
			assertThatCode(() -> invokeValidate(film)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("пустое описание — валидно")
		void emptyDescription() {
			Film film = validFilm();
			film.setDescription("");
			assertThatCode(() -> invokeValidate(film)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("ровно 200 символов — валидно (граница)")
		void exactly200Chars() {
			Film film = validFilm();
			film.setDescription("a".repeat(200));
			assertThatCode(() -> invokeValidate(film)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("201 символ → ValidationException")
		void oneCharOver200() {
			Film film = validFilm();
			film.setDescription("a".repeat(201));
			assertThatThrownBy(() -> invokeValidate(film))
					.isInstanceOf(ValidationException.class)
					.hasMessageContaining("200");
		}
	}

	@Nested
	@DisplayName("Поле releaseDate")
	class ReleaseDateValidation {

		@Test
		@DisplayName("null дата → ValidationException")
		void nullReleaseDate() {
			Film film = validFilm();
			film.setReleaseDate(null);
			assertThatThrownBy(() -> invokeValidate(film))
					.isInstanceOf(ValidationException.class)
					.hasMessageContaining("28 декабря 1895");
		}

		@Test
		@DisplayName("27.12.1895 (на день раньше границы) → ValidationException")
		void oneDayBeforeMin() {
			Film film = validFilm();
			film.setReleaseDate(BEFORE_MIN_RELEASE);
			assertThatThrownBy(() -> invokeValidate(film))
					.isInstanceOf(ValidationException.class);
		}

		@Test
		@DisplayName("28.12.1895 00:00 UTC — ровно граница, валидно")
		void exactlyMinDate() {
			Film film = validFilm();
			film.setReleaseDate(MIN_RELEASE);
			assertThatCode(() -> invokeValidate(film)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("28.12.1895 + 1 секунда — валидно")
		void justAfterMinDate() {
			Film film = validFilm();
			film.setReleaseDate(MIN_RELEASE.plusSeconds(1));
			assertThatCode(() -> invokeValidate(film)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("сегодняшняя дата — валидно")
		void today() {
			Film film = validFilm();
			film.setReleaseDate(Instant.now());
			assertThatCode(() -> invokeValidate(film)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("будущая дата — валидно (ограничения сверху нет)")
		void futureDate() {
			Film film = validFilm();
			film.setReleaseDate(Instant.now().plusSeconds(86_400L * 365));
			assertThatCode(() -> invokeValidate(film)).doesNotThrowAnyException();
		}
	}

	@Nested
	@DisplayName("Поле duration")
	class DurationValidation {

		@Test
		@DisplayName("0 → ValidationException")
		void zeroDuration() {
			Film film = validFilm();
			film.setDuration(0);
			assertThatThrownBy(() -> invokeValidate(film))
					.isInstanceOf(ValidationException.class)
					.hasMessageContaining("положительным");
		}

		@Test
		@DisplayName("-1 → ValidationException")
		void negativeDuration() {
			Film film = validFilm();
			film.setDuration(-1);
			assertThatThrownBy(() -> invokeValidate(film))
					.isInstanceOf(ValidationException.class);
		}

		@Test
		@DisplayName("Integer.MIN_VALUE → ValidationException")
		void minIntDuration() {
			Film film = validFilm();
			film.setDuration(Integer.MIN_VALUE);
			assertThatThrownBy(() -> invokeValidate(film))
					.isInstanceOf(ValidationException.class);
		}

		@Test
		@DisplayName("1 — минимальное валидное значение")
		void oneMinute() {
			Film film = validFilm();
			film.setDuration(1);
			assertThatCode(() -> invokeValidate(film)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("Integer.MAX_VALUE — валидно")
		void maxIntDuration() {
			Film film = validFilm();
			film.setDuration(Integer.MAX_VALUE);
			assertThatCode(() -> invokeValidate(film)).doesNotThrowAnyException();
		}
	}

	@Nested
	@DisplayName("Полностью валидный фильм")
	class HappyPath {

		@Test
		@DisplayName("все поля в границах — валидно")
		void fullyValid() {
			assertThatCode(() -> invokeValidate(validFilm()))
					.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("addFilm назначает id и сохраняет фильм")
		void addFilmAssignsId() {
			Film film = validFilm();
			Film saved = controller.addFilm(film);

			assertThat(saved.getId()).isNotNull().isPositive();
			assertThat(controller.getFilms()).contains(saved);
		}

		@Test
		@DisplayName("addFilm двум фильмам выдаёт разные id")
		void addFilmIncrementsId() {
			Film a = controller.addFilm(validFilm());
			Film b = controller.addFilm(validFilm());
			assertThat(a.getId()).isNotEqualTo(b.getId());
		}
	}
}