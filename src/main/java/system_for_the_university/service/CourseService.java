package system_for_the_university.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import system_for_the_university.DTO.request.CourseRequestDTO;
import system_for_the_university.DTO.response.CourseResponseDTO;
import system_for_the_university.entity.Course;
import system_for_the_university.repository.CourseRepository;
import system_for_the_university.repository.RegistrationRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
public class CourseService {
    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository, RegistrationRepository registrationRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional
    public CourseResponseDTO createCourse(CourseRequestDTO request) {
        log.info("Создание курса с именем: {} и вместимостью: {}", request.getName(), request.getCapacity());

        if (request.getStartTime().isAfter(request.getEndTime())) {
            log.info("Выброшено исключение в связи с невалидными данными");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Дата начала курса позже даты окончания");
        }

        Course course = mapToEntity(request);
        courseRepository.save(course);

        log.info("Курс успешно создан ID: {}", course.getId());

        return mapToDto(course);
    }

    public CourseResponseDTO getCourseById(Long courseId) {
        log.info("Поиск курса с ID: {}", courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Курс не найден"));
        return mapToDto(course);
    }

    public List<CourseResponseDTO> getAvailableCourses() {
        log.info("Поиск доступных курсов");
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Moscow"));

        return courseRepository.findAll().stream()
                .filter(course -> course.getCapacity() > courseRepository.countRegistrationsById(course.getId()))
                .filter(course -> now.isAfter(course.getStartTime()))
                .filter(course -> now.isBefore(course.getEndTime()))
                .map(this::mapToDto)
                .toList();
    }

    public List<CourseResponseDTO> getAllCourse() {
        log.info("Поиск всех курсов");
        return courseRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public void deleteCourse(Long courseID) {
        log.info("Удаление курса с ID: {}", courseID);
        if (!courseRepository.existsById(courseID)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Курс с таким ID не существует");
        }
        courseRepository.deleteById(courseID);
    }

    public Course mapToEntity(CourseRequestDTO request) {
        return new Course(
                request.getName(),
                request.getCapacity(),
                request.getStartTime(),
                request.getEndTime());
    }

    public CourseResponseDTO mapToDto(Course course) {
        CourseResponseDTO response = new CourseResponseDTO();
        response.setId(course.getId());
        response.setName(course.getName());
        response.setCapacity(course.getCapacity());
        response.setOccupiedSeats(course.getRegistrations().size());
        response.setStartTime(course.getStartTime());
        response.setEndTime(course.getEndTime());
        response.setTimeZone(course.getTimeZone());
        return response;
    }
}
