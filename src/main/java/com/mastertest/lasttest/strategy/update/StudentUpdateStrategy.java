package com.mastertest.lasttest.strategy.update;

import com.mastertest.lasttest.configuration.ConversionUtils;
import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.Student;
import com.mastertest.lasttest.model.dto.PersonDto;
import com.mastertest.lasttest.model.dto.StudentDto;
import com.mastertest.lasttest.model.dto.command.UpdatePersonCommand;
import com.mastertest.lasttest.model.dto.command.UpdateStudentCommand;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.service.person.UpdateStrategy;
import com.mastertest.lasttest.validator.PersonValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class StudentUpdateStrategy implements UpdateStrategy<UpdateStudentCommand> {

    private final PersonRepository personRepository;
    private final PersonValidator personValidator;


    @Override
    public PersonDto updateAndValidate(Map<String, Object> updateCommand, Person person) {

        Student student = (Student) person;
        UpdateStudentCommand studentCommand = ConversionUtils.convertMapToCommand(updateCommand, UpdateStudentCommand.class);

        updateCommonFields(studentCommand, student);

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
        return convertToPersonDto(student);
    }

    private void updateCommonFields(UpdatePersonCommand updateCommand, Student student) {
        if (updateCommand.getFirstName() != null){
            student.setFirstName(updateCommand.getFirstName());
        }
        if (updateCommand.getLastName() != null){
            student.setLastName(updateCommand.getLastName());
        }
        if (updateCommand.getPesel() != null){
            student.setPesel(updateCommand.getPesel());
        }
        if (updateCommand.getHeight() != null){
            student.setHeight(updateCommand.getHeight());
        }
        if (updateCommand.getWeight() != null){
            student.setWeight(updateCommand.getWeight());
        }
        if (updateCommand.getEmail() != null){
            student.setEmail(updateCommand.getEmail());
        }
    }

    private PersonDto convertToPersonDto(Student student) {
        StudentDto studentDto = StudentDto.fromEntity(student);
        personValidator.validate(studentDto);
        personRepository.save(student);
        return studentDto;
    }
}
