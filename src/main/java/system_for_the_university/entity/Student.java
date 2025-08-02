package system_for_the_university.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @NotNull
    @NotBlank
    @Size(max = 100)
    private String name;

    @Column(unique = true)
    @Email(message = "Email должен быть корректным")
    @NotNull
    @Size(max = 100)
    private String email;

    /* Добавил обратные ссылки чтобы при необходимости получить данные через объекты регистрации,
    и автоматическое удаление записей при удалении Студентов и Курсов из БД */
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Registration> registrations = new HashSet<>();

    public Student(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
