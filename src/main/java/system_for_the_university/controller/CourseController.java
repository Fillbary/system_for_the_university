package system_for_the_university.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import system_for_the_university.DTO.request.CourseRequestDTO;
import system_for_the_university.DTO.response.CourseResponseDTO;
import system_for_the_university.service.CourseService;
import system_for_the_university.service.RegistrationService;

import java.util.List;

@RestController
@RequestMapping("/api/courses") // Базовый путь для всех методов контроллера
@AllArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final RegistrationService registrationService;

    @GetMapping("/available")
    public List<CourseResponseDTO> getAvailableCourses() {
        return courseService.getAvailableCourses();
    }

    @GetMapping("/all")
    public List<CourseResponseDTO> getAllCourse() {
        return courseService.getAllCourse();
    }

    @GetMapping("/{id}")
    public CourseResponseDTO getCourseById(@RequestBody @PathVariable Long id) {
        return courseService.getCourseById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponseDTO createCourse(@RequestBody CourseRequestDTO request) {
        return courseService.createCourse(request);
    }
}
