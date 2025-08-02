package system_for_the_university.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;


/* Не использовал @Data из за проблем при автоматическом создании  equal() и hashCode()
 * так как они создаются со всеми полями. А из за FetchType.LAZY при загрузке registration
 * из БД ленивые поля не подтягиваю без прямого к ним обращения. С toString() такая же ситуация*/
@Getter
@Setter
@NoArgsConstructor
@Entity
public class Registration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column
    @NotNull
    @CreationTimestamp
    private LocalDateTime registrationTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Registration that = (Registration) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Registration{" +
                "id=" + id +
                ", registrationTime=" + registrationTime +
                '}';
    }

    public Registration(Student student, Course course, LocalDateTime registrationTime) {
        this.student = student;
        this.course = course;
        this.registrationTime = registrationTime;
    }
}
