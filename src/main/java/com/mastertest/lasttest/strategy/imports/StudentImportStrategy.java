package com.mastertest.lasttest.strategy.imports;

import com.mastertest.lasttest.configuration.ConversionUtils;
import com.mastertest.lasttest.model.persons.Person;
import com.mastertest.lasttest.model.persons.Student;
import com.mastertest.lasttest.model.dto.PersonDto;
import com.mastertest.lasttest.model.dto.StudentDto;
import com.mastertest.lasttest.model.dto.command.CreatePersonCommand;
import com.mastertest.lasttest.model.importfile.ImportStatus;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.service.fileprocess.ImportStrategy;
import com.mastertest.lasttest.validator.PersonValidator;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.*;

@RequiredArgsConstructor
@Component
public class StudentImportStrategy implements ImportStrategy<StudentDto> {


    private final PersonValidator personValidator;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final PersonRepository personRepository;

    private static final Logger logger = LoggerFactory.getLogger(StudentImportStrategy.class);
    private final List<Map<String, Object>> personBatch = new ArrayList<>();

    String PERSON_SQL = "INSERT INTO person (pesel, first_name, last_name, height, weight, email, type, university_name, year_of_study, study_field, scholarship, version) " +
            "VALUES (:pesel, :first_name, :last_name, :height, :weight, :email, :type, :university_name, :year_of_study, :study_field, :scholarship, :version) " +
            "ON DUPLICATE KEY UPDATE " +
            "first_name = VALUES(first_name), " +
            "last_name = VALUES(last_name), " +
            "height = VALUES(height), " +
            "weight = VALUES(weight), " +
            "email = VALUES(email), " +
            "type = VALUES(type), " +
            "university_name = VALUES(university_name), " +
            "year_of_study = VALUES(year_of_study), " +
            "study_field = VALUES(study_field), " +
            "scholarship = VALUES(scholarship), " +
            "version = version + 1;";


    @Override
    public void clearBatch() {
        personBatch.clear();
    }



    @Override
    public PersonDto validateAndSave(CreatePersonCommand<?> command) {
        StudentDto studentDto = ConversionUtils.convertMapToDto((Map) command.getDetails(), StudentDto.class);
        validateDto(studentDto);
        return saveStudent(studentDto);
    }


    @Override
    public void addToBatch(String record, ImportStatus importStatus) {
        Student student = parseCsvToDto(record);
        validate(student);
        processPersonToBatch(student);
    }


    @Override
    public void processBatch() {
        processBatchInternal();
        clearBatch();
    }

    private void processBatchInternal() {
        logger.info("Processing Employee batch with size: {} on thread: {}", personBatch.size(), Thread.currentThread().getName());
        namedParameterJdbcTemplate.batchUpdate(PERSON_SQL, personBatch.toArray(new Map[0]));
    }

    private void processPersonToBatch(Student student) {
        logger.debug("Processing Student batch with size: {} on thread: {}", personBatch.size(), Thread.currentThread().getName());
        if (student != null) {
            Map<String, Object> personParams = new HashMap<>();
            personParams.put("pesel", student.getPesel());
            personParams.put("first_name", student.getFirstName());
            personParams.put("last_name", student.getLastName());
            personParams.put("height", student.getHeight());
            personParams.put("weight", student.getWeight());
            personParams.put("email", student.getEmail());
            personParams.put("type", "student");
            personParams.put("version", 0L);
            personParams.put("university_name", student.getUniversityName());
            personParams.put("year_of_study", student.getYearOfStudy());
            personParams.put("study_field", student.getStudyField());
            personParams.put("scholarship", student.getScholarship());
            personBatch.add(personParams);
        }
    }


    private void validate(Student student) {
        personValidator.validate(student);
    }

    private void validateDto(StudentDto studentDto) {
        Optional<Person> personExisting = personRepository.findByPesel(studentDto.getPesel());
        if (personExisting.isPresent()) {
            logger.error("Person with pesel: {} exists", studentDto.getPesel());
            throw new EntityExistsException(MessageFormat.format("Person with pesel: {} exists", studentDto.getPesel()));
        }
        personValidator.validate(studentDto);
    }

    private Student parseCsvToDto(String csvLine) {
        logger.debug("STUDENT LIST: {}", csvLine);
        String[] fields = csvLine.split(",");

        if (fields.length < 11 || !"student".equalsIgnoreCase(fields[0])) {
            logger.error("Invalid CSV line for student: {}", csvLine);
            throw new IllegalArgumentException("Invalid CSV line for student");
        }

        Student student = new Student();
        student.setFirstName(fields[1]);
        student.setLastName(fields[2]);
        student.setPesel(fields[3]);
        student.setHeight(Double.parseDouble(fields[4]));
        student.setWeight(Double.parseDouble(fields[5]));
        student.setEmail(fields[6]);
        student.setUniversityName(fields[7]);
        student.setYearOfStudy(Integer.parseInt(fields[8]));
        student.setStudyField(fields[9]);
        student.setScholarship(Double.parseDouble(fields[10]));

        return student;
    }

    private StudentDto saveStudent(StudentDto studentDto) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("pesel", studentDto.getPesel());
        parameters.put("first_name", studentDto.getFirstName());
        parameters.put("last_name", studentDto.getLastName());
        parameters.put("height", studentDto.getHeight());
        parameters.put("weight", studentDto.getWeight());
        parameters.put("email", studentDto.getEmail());
        parameters.put("type", "student");
        parameters.put("university_name", studentDto.getUniversityName());
        parameters.put("year_of_study", studentDto.getYearOfStudy());
        parameters.put("study_field", studentDto.getStudyField());
        parameters.put("scholarship", studentDto.getScholarship());
        parameters.put("version", 0L);
        logger.debug("Generated parameters: {}", parameters);

        namedParameterJdbcTemplate.update(PERSON_SQL, new MapSqlParameterSource(parameters));


        return studentDto;
    }
}
