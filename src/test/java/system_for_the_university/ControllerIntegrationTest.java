package system_for_the_university;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import system_for_the_university.DTO.request.RegistrationRequestDTO;
import system_for_the_university.DTO.response.RegistrationResponseDTO;
import system_for_the_university.entity.Course;
import system_for_the_university.entity.Student;
import system_for_the_university.repository.CourseRepository;
import system_for_the_university.repository.StudentRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ControllerIntegrationTest {

    // HTTP клиент для тестирования REST-эндпоинтов
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    private final LocalDateTime now = LocalDateTime.now();

    // Перед каждым тестом очищаем репозиторий и заполняем заново
    @BeforeEach
    void setUp() {
        courseRepository.deleteAll();
        studentRepository.deleteAll();

        Course activeCourse1 = new Course("Math", 30,
                now.minusDays(1), now.plusDays(7));
        Course activeCourse2 = new Course("Physic", 25,
                now.minusHours(2), now.plusDays(5));
        Course inactiveCourse = new Course("History", 20,
                now.minusDays(10), now.minusDays(5));

        courseRepository.saveAll(List.of(activeCourse1, activeCourse2, inactiveCourse));

        Student student = new Student("Иван Иванов", "ivan@example.com");

        studentRepository.save(student);
    }


    @Test
    @DisplayName("GET /api/courses/available возвращает список активных курсов")
    void testGetAvailableCourses_ReturnList() {
        log.info("Отправка запроса по эндпоинту /api/courses/available");
        // Отправка гет запроса по адресу "/api/courses/available" в ответ ожидается массив из курсов
        ResponseEntity<Course[]> response = restTemplate.getForEntity("/api/courses/available", Course[].class);

        log.info("Проверка статуса ответа");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Ответ не соответсвует ожидаемому");
        log.info("Проверка тела ответа");
        Course[] courses = response.getBody();
        assertNotNull(courses);
        assertEquals(2, courses.length, "Длинна массива несовпадает с ожидаемой");

        log.info("Проверка наличия курсов");
        assertTrue(Arrays.stream(courses).anyMatch(c -> c.getName().equals("Math")));
        assertTrue(Arrays.stream(courses).anyMatch(c -> c.getName().equals("Physic")));
        assertFalse(Arrays.stream(courses).anyMatch(c -> c.getName().equals("History")));

        log.info("Проверка полей курса");
        Course mathCourse = courses[0];
        assertEquals(30, mathCourse.getCapacity());
        assertTrue(mathCourse.getStartTime().isBefore(now));
        assertTrue(mathCourse.getEndTime().isAfter(now));
    }

    @Test
    @DisplayName("GET /api/courses/all возвращает список всех курсов")
    void testGetAllCourses_ReturnList() {
        log.info("Отправка запроса по эндпоинту /api/courses/all");
        // Отправка гет запроса по адресу "/api/courses/all" в ответ ожидается массив из курсов
        ResponseEntity<Course[]> response = restTemplate.getForEntity("/api/courses/all", Course[].class);

        log.info("Проверка статуса ответа");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Ответ не соответсвует ожидаемому");
        log.info("Проверка тела ответа");
        Course[] courses = response.getBody();
        assertNotNull(courses);
        assertEquals(3, courses.length, "Длинна массива несовпадает с ожидаемой");

        log.info("Проверка наличия курсов");
        assertTrue(Arrays.stream(courses).anyMatch(c -> c.getName().equals("Math")));
        assertTrue(Arrays.stream(courses).anyMatch(c -> c.getName().equals("Physic")));
        assertTrue(Arrays.stream(courses).anyMatch(c -> c.getName().equals("History")));
    }

    @Test
    @DisplayName("GET /api/courses/{id} возвращает первый курс из списка")
    void testGetFirstCourse() {
        log.info("Отправка запроса поэндпоинту /api/courses/{id}");
        Course geoCourse = courseRepository.save(new Course("Geometry", 30, now.minusDays(1), now.plusDays(1)));
        Long geoCourseId = geoCourse.getId();

        // Отправка гет запроса по адресу "/api/courses/id" в ответ ожидается первый в списке курс
        ResponseEntity<Course> response = restTemplate.getForEntity("/api/courses/{id}", Course.class, geoCourseId);

        log.info("Проверка статуса ответа");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Ответ не соответсвует ожидаемому");
        log.info("Проверка тела ответа");
        Course course = response.getBody();
        assertNotNull(course);

        log.info("Проверка полей курса");
        assertEquals("Geometry", course.getName());
        assertEquals(30, course.getCapacity());
        assertTrue(course.getStartTime().isBefore(now));
        assertTrue(course.getEndTime().isAfter(now));
    }

    @Test
    @DisplayName("POST /api/registrations возвращает 201 Created")
    void testRegisterStudent_Success() {
        // Получаем существующие данные из setUp()
        List<Course> courses = courseRepository.findAll();
        Course mathCourse = courses.get(0);

        List<Student> students = studentRepository.findAll();
        Student student = students.get(0); // Берём первого (и единственного) студента

        // Создаем DTO для запроса
        RegistrationRequestDTO request = new RegistrationRequestDTO(
                student.getId(),
                mathCourse.getId()
        );

        // Выполнение POST-запроса с телом
        ResponseEntity<RegistrationResponseDTO> response = restTemplate.postForEntity(
                "/api/registrations",
                request,
                RegistrationResponseDTO.class
        );

        // Проверки
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
