package system_for_the_university.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import system_for_the_university.DTO.request.RegistrationRequestDTO;
import system_for_the_university.DTO.response.RegistrationResponseDTO;
import system_for_the_university.entity.Course;
import system_for_the_university.entity.Registration;
import system_for_the_university.entity.Student;
import system_for_the_university.repository.CourseRepository;
import system_for_the_university.repository.RegistrationRepository;
import system_for_the_university.repository.StudentRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public RegistrationService(RegistrationRepository registrationRepository,
                               StudentRepository studentRepository,
                               CourseRepository courseRepository) {
        this.registrationRepository = registrationRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public RegistrationResponseDTO registerStudentToCourse(RegistrationRequestDTO request) {
        log.info("Запись студента {} на курс {}", request.getStudentId(), request.getCourseId());

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Студент не найден"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Курс не найден"));


        if (registrationRepository.existsByStudentIdAndCourseId(request.getCourseId(), request.getStudentId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Студент уже записан на курс");
        }

        if (courseRepository.countRegistrationsById(request.getCourseId()) >= course.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Нет свободных мест для записи");
        }

        /* Преобразую время регистрации к московскому, чтобы проверить,
        что не смотря на разницу в часовых поясах студет ориентировался на московское время */
        LocalDateTime nowInMoscow = LocalDateTime.now(ZoneId.of("Europe/Moscow"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        log.info("Попытка регистрации на курс по московскому времени {}", nowInMoscow.format(formatter));
        if (nowInMoscow.isBefore(course.getStartTime()) || nowInMoscow.isAfter(course.getEndTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Запись возможна только с " + course.getStartTime().format(formatter)
                            + " до " + course.getEndTime().format(formatter) + "(Московское время)");
        }

        /*Обернул логику регистрации в try/catch тобы обработать ошибку позитивной блокировки*/
        try {
            Registration registration = new Registration(student, course, LocalDateTime.now());
            Registration savedRegistration = registrationRepository.save(registration);
            return mapToDto(savedRegistration);
        } catch (OptimisticLockingFailureException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Курс был изменён другим пользователем. Попробуйте ещё раз.");
        }
    }

    public void cancelRegistration(Long registrationId) {
        log.info("Отмена записи: {}", registrationId);
        if (!registrationRepository.existsById(registrationId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Запись не найдена");
        }
        registrationRepository.deleteById(registrationId);
    }

    public List<RegistrationResponseDTO> getAllRegistrations() {
        log.info("Список всех записей");
        return registrationRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    public RegistrationResponseDTO mapToDto(Registration registration) {
        RegistrationResponseDTO response = new RegistrationResponseDTO();
        response.setId(registration.getId());
        response.setCourseId(registration.getCourse().getId());
        response.setCourseName(registration.getCourse().getName());
        response.setStudentId(registration.getStudent().getId());
        response.setStudentName(registration.getStudent().getName());
        response.setRegistrationTime(registration.getRegistrationTime());
        return response;
    }
}
