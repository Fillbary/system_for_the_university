package system_for_the_university.DTO.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegistrationRequestDTO {
    @NotNull
    private Long studentId;

    @NotNull
    private Long courseId;
}
