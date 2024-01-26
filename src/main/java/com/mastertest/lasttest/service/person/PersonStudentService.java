package com.mastertest.lasttest.service.person;


import com.mastertest.lasttest.model.Student;
import com.mastertest.lasttest.model.dto.StudentDto;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonStudentService {

    private final PersonRepository personRepository;
    private final StudentRepository studentRepository;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void savePersonsAndStudents(List<StudentDto> studentDtos) {
        List<Student> students = studentDtos.stream()
                .map(this::convertToStudentEntity)
                .collect(Collectors.toList());

        personRepository.saveAll(students);

    }

    private Student convertToStudentEntity(StudentDto dto) {
        Student student = new Student();
        student.setPesel(dto.getPesel());
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setType("student");
        student.setHeight(dto.getHeight());
        student.setWeight(dto.getWeight());
        student.setEmail(dto.getEmail());
        student.setUniversityName(dto.getUniversityName());
        student.setYearOfStudy(dto.getYearOfStudy());
        student.setStudyField(dto.getStudyField());
        student.setScholarship(dto.getScholarship());
        return student;
    }
}
