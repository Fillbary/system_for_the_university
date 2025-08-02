package system_for_the_university;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import system_for_the_university.DTO.request.CourseRequestDTO;
import system_for_the_university.DTO.response.CourseResponseDTO;
import system_for_the_university.entity.Course;
import system_for_the_university.repository.CourseRepository;
import system_for_the_university.repository.RegistrationRepository;
import system_for_the_university.service.CourseService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {
    // Мокирую репозиторий чтобы втестах не обращаться к настоящей БД
    @Mock
    private CourseRepository courseRepository;

    // Внедряю связь с моком репозитория так как в проверяемые методы в сервисе взаимодейстуют с репозиторием
    @InjectMocks
    private CourseService courseService;

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime future = now.plusDays(1);
    LocalDateTime past = now.minusDays(1);

    @BeforeAll
    static void initAll() {
        log.info("Инициализация тестового класса CourseServiceTest");
    }

    @AfterAll
    static void tearDownAll() {
        log.info("Все тесты завершены");
    }

    @Test
    @DisplayName("Создание курса с валидными датами")
    void testCreateCourseWithLocalDateTime() {
        log.info("Запуск теста: создание курса с валидными датами");

        CourseRequestDTO request = new CourseRequestDTO(
        "Math", 30, now, future, "Europe/Moscow");

        log.debug("Создание тестового курса");
        Course mockCourse = new Course("Math", 30, now, future);
        mockCourse.setId(1L);

        log.debug("Настройка мока");
        // Задаю правила работы моего мока указывая,
        // что при сохранении объекта в репу будет возвращаться ранее созданный обект
        when(courseRepository.save(any(Course.class))).thenReturn(mockCourse);

        log.debug("Вызов метода");
        // Вызываю метод создания курса с заданными параметрами
        CourseResponseDTO result = courseService.createCourse(request);

        log.info("Проверка результатов");
        // Проверяю поля, чтобы убедиться в том, что после вызова метода поля не модифицируются и равны мок объекту
        assertEquals("Math", result.getName(), "Название курса не совпадает");
        assertEquals(30, result.getCapacity(), "Вместимость не совпадает");
        assertEquals(now, result.getStartTime(), "Время начала регистрации не совпадает");
        assertEquals(future, result.getEndTime(), "Время окончания регистрации не совпадает");

        // Проверяю, что метод вызывался только один раз
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    @DisplayName("опытка создания курса с невалидными данными")
    void testCreateCourse_InvalidTimeWindow() {
        log.info("Запуск теста с невалидными данными");
        CourseRequestDTO request = new CourseRequestDTO(
            "Math", 30, future, now, "Europe/Moscow");

        log.info("Проверка проброса исключения при попытке создать невалидный курс");
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            courseService.createCourse(request);
        });

        log.debug("Проверка сообщения об ошибке");
        // Проверяем что выпадающее сообщение ошибки соответствует
        assertTrue(exception.getMessage().contains("Дата начала курса позже даты окончания"),
                "Сообщение об ошибке не соответствует ожидаемому");
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        // Проверяем что у репозитория не вызывалось никаких методов
        verify(courseRepository, never()).save(any());
    }

    @Test
    void testGetAvailableCourses_Filtering() {
        log.info("Запуск теста на проверку вывода отфильтрованных курсов");


        log.debug("Создание тестовых курсов");
        // Создаю 3 вида курсов и только 1 с валидными данными для проверки вывода по запросу
        Course validCourse = new Course("Math", 30, past, future);
        validCourse.setId(1L);
        Course overregCourse = new Course("Physics", 10, now, future);
        overregCourse.setId(2L);
        Course nonStartRegCourse = new Course("Physics", 25, past.minusDays(2), past.minusDays(1));
        nonStartRegCourse.setId(3L);

        log.debug("Настройка мока для списка курсов");
        // Мокаю чтобы потом вернуть заготовленный список
        when(courseRepository.findAll()).thenReturn(List.of(validCourse, overregCourse, nonStartRegCourse));

        log.debug("Настройка мока для подсчета регистраций");
        // Мокирую для подсчета регистраций на каждом курсе
        when(courseRepository.countRegistrationsById(validCourse.getId())).thenReturn(10L);
        when(courseRepository.countRegistrationsById(overregCourse.getId())).thenReturn(10L);
        when(courseRepository.countRegistrationsById(nonStartRegCourse.getId())).thenReturn(10L);

        log.info("Вызов метода возвращающего список доступных курсов");
        // Вызываем метод возвращающий доступные курсы
        List<CourseResponseDTO> resultCourses = courseService.getAvailableCourses();

        log.info("Проверка результатов фильтрации");
        // Проверяем, что вернулся 1 курс
        assertEquals(1, resultCourses.size(), "Размер списка не совпадает с количеством доступных курсов");
        // Проверяем что это тот курс который нам нужен
        assertEquals(resultCourses.get(0).getName(), validCourse.getName(), "Возвращен неверный курс");
        // Проверяем что у полученного курса есть свободные места
        assertTrue(resultCourses.get(0).getCapacity() >
                        courseRepository.countRegistrationsById(resultCourses.get(0).getId()),
                "У полученного курса нет свобоных мест");
        // Проверяем что курс доступен для записи
        assertTrue(now.isAfter(resultCourses.get(0).getStartTime()));
        assertTrue(now.isBefore(resultCourses.get(0).getEndTime()));
    }
}
