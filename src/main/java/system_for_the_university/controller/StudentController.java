package system_for_the_university.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import system_for_the_university.service.RegistrationService;


@RestController
@RequestMapping("/api/students")
@AllArgsConstructor
public class StudentController {
    private final RegistrationService registrationService;

}
