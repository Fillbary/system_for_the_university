package system_for_the_university.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import system_for_the_university.DTO.response.StudentResponseDTO;
import system_for_the_university.entity.Student;
import system_for_the_university.repository.StudentRepository;

import java.util.List;

@Slf4j
@Service
public class StudentService {
    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // Добавил эту аннотацию для целостности данных в рамках ACID в дальнейшем добавил во всех операциях создания, удаления
    @Transactional
    public Student createStudent(String name, String email) {
        log.info("Создание студента с именем: {} и email: {}", name, email);

        if (studentRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email уже занят");
        }
        Student student = new Student(name, email);
        studentRepository.save(student);

        log.info("Студент успешно создан ID: {}", student.getId());
        return student;
    }

    public Student getStudentById(Long studentId) {
        log.info("Поиск студента с ID: {}", studentId);
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Студент с таким ID не найден"));
    }

    public List<Student> getAllStudents() {
        log.info("Получение списка всех студентов");
        return studentRepository.findAll();
    }

    @Transactional
    public void deleteStudent(Long studentId) {
        log.info("Удаление студента с ID: {}", studentId);
        if (!studentRepository.existsById(studentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Студент с таким ID не найден");
        }
        studentRepository.deleteById(studentId);
    }

    public StudentResponseDTO response(Student student) {
        StudentResponseDTO response = new StudentResponseDTO();
        response.setId(student.getId());
        response.setName(student.getName());
        response.setEmail(student.getEmail());

        return response;
    }
}
