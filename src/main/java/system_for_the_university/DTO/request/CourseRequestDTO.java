package system_for_the_university.DTO.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CourseRequestDTO {
    @NotBlank
    private String name;

    @Min(1)
    private Integer capacity;

    @Future
    private LocalDateTime startTime;

    @Future
    private LocalDateTime endTime;

    private String timeZone;
}
