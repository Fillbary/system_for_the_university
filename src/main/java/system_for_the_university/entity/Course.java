package system_for_the_university.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @NotNull
    @Size(min = 1, max = 100)
    private String name;

    @Column
    @NotNull
    @Min(1)
    private Integer capacity;

    @Column
    @NotNull
    private LocalDateTime startTime;

    @Column
    @NotNull
    private LocalDateTime endTime;

    @Column
    @NotNull
    private String timeZone = "Europe/Moscow";

    /* Добавил обратные ссылки чтобы при необходимости получить данные через объекты регистрации,
    и автоматическое удаление записей при удалении Студентов и Курсов из БД */
    @Column
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Registration> registrations = new HashSet<>();

    /* Прочитал что доступ к регистрации для нескольких пользователей лучше реализовывать через
    позитивную блокировку, для этого добавил переменную с аннотацией @Version*/
    @Version
    private Integer version;

    public Course(String name, Integer capacity, LocalDateTime startTime, LocalDateTime endTime) {
        this.name = name;
        this.capacity = capacity;
        this.startTime = startTime;
        this.endTime = endTime;

        if (this.endTime.isBefore(this.startTime)) {
            throw new IllegalArgumentException("Время окончания должно быть позже времени начала");
        }
    }
}
