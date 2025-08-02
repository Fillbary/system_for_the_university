package system_for_the_university.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import system_for_the_university.DTO.request.RegistrationRequestDTO;
import system_for_the_university.DTO.response.RegistrationResponseDTO;
import system_for_the_university.service.RegistrationService;

@RestController
@RequestMapping("/api/registrations")
@AllArgsConstructor
public class RegistrationController {
    private final RegistrationService registrationService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponseDTO registerStudent(@RequestBody RegistrationRequestDTO request) {
        return registrationService.registerStudentToCourse(request);
    }
}
