package system_for_the_university;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import system_for_the_university.DTO.request.RegistrationRequestDTO;
import system_for_the_university.DTO.response.RegistrationResponseDTO;
import system_for_the_university.entity.Course;
import system_for_the_university.entity.Registration;
import system_for_the_university.entity.Student;
import system_for_the_university.repository.CourseRepository;
import system_for_the_university.repository.RegistrationRepository;
import system_for_the_university.repository.StudentRepository;
import system_for_the_university.service.RegistrationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {
    @Mock
    StudentRepository studentRepository;

    @Mock
    CourseRepository courseRepository;

    @Mock
    RegistrationRepository registrationRepository;

    @InjectMocks
    RegistrationService registrationService;

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime future = now.plusDays(1);
    LocalDateTime past = now.minusDays(1);


    @Test
    @DisplayName("Регистрация студента")
    void testRegisterStudent_Success() {
        log.info("Подготовка тестовых данных");
        Student student = new Student("Иван", "ivan@mail.ru");
        student.setId(1L);

        Course course = new Course("Math", 30, past, future);
        course.setId(1L);

        Registration registration = new Registration(student, course, LocalDateTime.now());
        registration.setId(1L);

        // Настраиваем поведение моков
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(registrationRepository.save(any(Registration.class))).thenReturn(registration);

        // Подготавливаем реквест для передачи в метод
        RegistrationRequestDTO request = new RegistrationRequestDTO(1L, 1L);

        log.info("Вызов метода registerStudentToCourse");
        RegistrationResponseDTO response = registrationService.registerStudentToCourse(request);

        log.info("Проверка результатов для валидной регистрации");
        assertEquals(1L, response.getStudentId(), "ID студента не совпадает");
        assertEquals(1L, response.getCourseId(), "ID курса не совпадает");
        assertNotNull(response.getRegistrationTime(), "Дата регистрации не установлена");

        verify(studentRepository, times(1)).findById(any());
        verify(courseRepository, times(1)).findById(any());
        verify(registrationRepository, times(1)).save(any(Registration.class));
    }

    @Test
    @DisplayName("Попытка записи на переполненный курс")
    void testRegisterStudent_CourseFull() {
        log.info("Подготовка теста: попытка записи на переполненный курс");

        Student student = new Student("Иван", "ivan@mail.ru");
        student.setId(1L);

        Course course = new Course("Math", 10, past, future); // Курс на 10 мест
        course.setId(1L);

        // Настройка моков
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.countRegistrationsById(1L)).thenReturn(10L); // Все места заняты

        RegistrationRequestDTO request = new RegistrationRequestDTO(1L, 1L);

        log.info("Проверка проброса исключений для переполненого курса");
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> registrationService.registerStudentToCourse(request));

        log.info("Проверка результатов для теста переполненго курса");
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Нет свободных мест"));

        verify(studentRepository).findById(1L);
        verify(courseRepository).findById(1L);
        verify(courseRepository).countRegistrationsById(1L);
        verify(registrationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Попытка записи вне временного окна")
    void testRegisterStudent_TimeWindowClosed() {
        log.info("Подготовка теста: попытка записи вне временного окна");

        Student student = new Student("Иван", "ivan@mail.ru");
        student.setId(1L);

        Course course = new Course("Math", 10, past.minusDays(2), past.minusDays(1));
        course.setId(1L);

        // Настройка моков
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        RegistrationRequestDTO request = new RegistrationRequestDTO(1L, 1L);

        log.info("Проверка проброса исключений");
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> registrationService.registerStudentToCourse(request));

        log.info("Проверка результатов для ненодходящего времени");
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        assertTrue(exception.getMessage().contains("Запись возможна только с " + course.getStartTime().format(formatter)
                            + " до " + course.getEndTime().format(formatter) + "(Московское время)"));

        verify(studentRepository).findById(1L);
        verify(courseRepository).findById(1L);
        verify(registrationRepository, never()).save(any());
    }
}
