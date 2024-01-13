package com.mastertest.lasttest.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mastertest.lasttest.configuration.ConversionUtils;
import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.dto.PersonDto;
import com.mastertest.lasttest.model.dto.RetireeDto;
import com.mastertest.lasttest.model.dto.StudentDto;
import com.mastertest.lasttest.model.dto.command.CreatePersonCommand;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.service.fileprocess.ImportStrategy;
import com.mastertest.lasttest.validator.PersonValidator;
import jakarta.persistence.EntityExistsException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class StudentImportStrategy implements ImportStrategy<StudentDto> {


    private final PersonValidator personValidator;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final PersonRepository personRepository;
    private static final Logger logger = LoggerFactory.getLogger(StudentImportStrategy.class);

    @Override
    public void validateParseAndSave(String record) {
        StudentDto studentDto = parseCsvToDto(record);
        validateDto(studentDto);
        saveStudent(studentDto);
    }

    @Override
    public PersonDto validateAndSave(CreatePersonCommand<?> command) {
        StudentDto studentDto = ConversionUtils.convertMapToDto((Map) command.getDetails(), StudentDto.class);
        validateDto(studentDto);
        return saveStudent(studentDto);
    }


    private void validateDto(StudentDto studentDto) {
        Optional<Person> personExisting = personRepository.findByPesel(studentDto.getPesel());

        if (personExisting.isPresent()) {
            logger.error("Person with pesel: {} exists", studentDto.getPesel());
            throw new EntityExistsException(MessageFormat.format("Person with pesel: {} exists", studentDto.getPesel()));
        }
        personValidator.validate(studentDto);
    }

    private StudentDto parseCsvToDto(String csvLine) {
        String[] fields = csvLine.split(",");
        if (fields.length < 11 || !"student" .equalsIgnoreCase(fields[0])) {
            logger.error("Invalid CSV line for student: {}", csvLine);
            throw new IllegalArgumentException("Invalid CSV line for student");
        }
        return StudentDto.builder()
                .firstName(fields[1])
                .lastName(fields[2])
                .pesel(fields[3])
                .height(Double.parseDouble(fields[4]))
                .weight(Double.parseDouble(fields[5]))
                .email(fields[6])
                .universityName(fields[7])
                .yearOfStudy(Integer.parseInt(fields[8]))
                .studyField(fields[9])
                .scholarship(Double.parseDouble(fields[10]))
                .build();
    }

    private StudentDto saveStudent(StudentDto studentDto) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("pesel", studentDto.getPesel());
        parameters.put("first_name", studentDto.getFirstName());
        parameters.put("last_name", studentDto.getLastName());
        parameters.put("height", studentDto.getHeight());
        parameters.put("weight", studentDto.getWeight());
        parameters.put("email", studentDto.getEmail());
        parameters.put("dtype", "Student");
        parameters.put("university_name", studentDto.getUniversityName());
        parameters.put("year_of_study", studentDto.getYearOfStudy());
        parameters.put("study_field", studentDto.getStudyField());
        parameters.put("scholarship", studentDto.getScholarship());
        parameters.put("version", 0L);
        logger.debug("Generated parameters: {}", parameters);
        String insertPersonSql = "INSERT INTO person (pesel, first_name, last_name, height, weight, email, dtype, version) VALUES (:pesel, :first_name, :last_name, :height, :weight, :email, :dtype, :version)";
        namedParameterJdbcTemplate.update(insertPersonSql, parameters);

        Long personId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        String insertStudentSql = "INSERT INTO student (id, university_name, year_of_study, study_field, scholarship) VALUES (:id, :university_name, :year_of_study, :study_field, :scholarship)";
        parameters.put("id", personId);
        namedParameterJdbcTemplate.update(insertStudentSql, parameters);
        return studentDto;
    }
}
