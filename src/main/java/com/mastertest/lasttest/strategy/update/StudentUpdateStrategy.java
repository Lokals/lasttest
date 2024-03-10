package com.mastertest.lasttest.strategy.update;

import com.mastertest.lasttest.configuration.ConversionUtils;
import com.mastertest.lasttest.model.persons.Student;
import com.mastertest.lasttest.model.dto.StudentDto;
import com.mastertest.lasttest.model.dto.command.UpdatePersonCommand;
import com.mastertest.lasttest.model.dto.command.UpdateStudentCommand;
import com.mastertest.lasttest.repository.StudentRepository;
import com.mastertest.lasttest.service.person.UpdateStrategy;
import com.mastertest.lasttest.validator.PersonValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@RequiredArgsConstructor
@Component
public class StudentUpdateStrategy implements UpdateStrategy<StudentDto, UpdateStudentCommand> {

    private final StudentRepository studentRepository;
    private final PersonValidator validator;


    @Override
    public StudentDto updateAndValidate(UpdatePersonCommand<UpdateStudentCommand> updatePersonCommand, String pesel) {
        UpdateStudentCommand studentCommand = ConversionUtils.convertCommandToCommand(updatePersonCommand.getDetails(), UpdateStudentCommand.class);

        Student student = studentRepository.findById(pesel).orElseThrow(() -> new EntityNotFoundException(
                MessageFormat.format("person with pesel={0} not found", pesel)));
        if (updatePersonCommand.getFirstName() != null) {
            student.setFirstName(updatePersonCommand.getFirstName());
        }
        if (updatePersonCommand.getLastName() != null) {
            student.setLastName(updatePersonCommand.getLastName());
        }
        if (updatePersonCommand.getHeight() != null) {
            student.setHeight(updatePersonCommand.getHeight());
        }
        if (updatePersonCommand.getWeight() != null) {
            student.setWeight(updatePersonCommand.getWeight());
        }
        if (updatePersonCommand.getEmail() != null) {
            student.setEmail(updatePersonCommand.getEmail());
        }
        if (studentCommand != null) {
            validator.validate(studentCommand);
            if (studentCommand.getUniversityName() != null) {
                student.setUniversityName(studentCommand.getUniversityName());
            }
            if (studentCommand.getYearOfStudy() != null) {
                student.setYearOfStudy(studentCommand.getYearOfStudy());
            }
            if (studentCommand.getStudyField() != null) {
                student.setStudyField(studentCommand.getStudyField());
            }
            if (studentCommand.getScholarship() != null) {
                student.setScholarship(studentCommand.getScholarship());
            }
        }
        studentRepository.save(student);
        return StudentDto.fromEntity(student);
    }
}
