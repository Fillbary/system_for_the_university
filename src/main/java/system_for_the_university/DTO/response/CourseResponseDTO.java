package system_for_the_university.DTO.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CourseResponseDTO {
    private Long id;
    private String name;
    private Integer capacity;
    private Integer occupiedSeats; // Количество записавшихся
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String timeZone;
}
