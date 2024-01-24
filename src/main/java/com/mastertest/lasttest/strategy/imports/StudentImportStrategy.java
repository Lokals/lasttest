package com.mastertest.lasttest.strategy.imports;

import com.mastertest.lasttest.configuration.ConversionUtils;
import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.dto.PersonDto;
import com.mastertest.lasttest.model.dto.StudentDto;
import com.mastertest.lasttest.model.dto.command.CreatePersonCommand;
import com.mastertest.lasttest.model.factory.ImportStatus;
import com.mastertest.lasttest.model.factory.StatusFile;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.service.fileprocess.ImportStatusService;
import com.mastertest.lasttest.service.fileprocess.ImportStrategy;
import com.mastertest.lasttest.validator.PersonValidator;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class StudentImportStrategy implements ImportStrategy<StudentDto> {


    private final PersonValidator personValidator;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final PersonRepository personRepository;
    private final ImportStatusService importStatusService;

    private static final Logger logger = LoggerFactory.getLogger(StudentImportStrategy.class);
    private ThreadLocal<List<StudentDto>> threadLocalBatch = ThreadLocal.withInitial(ArrayList::new);

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

    @Override
    public void addToBatch(String record) {
        List<StudentDto> batchList = threadLocalBatch.get();
        StudentDto studentDto = parseCsvToDto(record);
        validateDto(studentDto);
        batchList.add(studentDto);

    }

    @Override
    public void processBatch(ImportStatus importStatus) {
        List<StudentDto> batchList = threadLocalBatch.get();
        if (!batchList.isEmpty()) {
            logger.info("BATH SIZE!!! {}", batchList.size());
            List<Map<String, Object>> personBatch = new ArrayList<>();
            for (StudentDto student : batchList) {
                Map<String, Object> personParams = new HashMap<>();
                personParams.put("pesel", student.getPesel());
                personParams.put("first_name", student.getFirstName());
                personParams.put("last_name", student.getLastName());
                personParams.put("height", student.getHeight());
                personParams.put("weight", student.getWeight());
                personParams.put("email", student.getEmail());
                personParams.put("type", "student");
                personParams.put("version", 0L);
                personBatch.add(personParams);
            }
            String personSql = "INSERT INTO person (pesel, first_name, last_name, height, weight, email, type, version) VALUES (:pesel, :first_name, :last_name, :height, :weight, :email, :type, :version)";
            namedParameterJdbcTemplate.batchUpdate(personSql, personBatch.toArray(new Map[0]));
            String idRetrievalSql = "SELECT id, pesel FROM person WHERE pesel IN (:pesels)";
            Map<String, Long> peselToIdMap = namedParameterJdbcTemplate.query(idRetrievalSql,
                    Collections.singletonMap("pesels", batchList.stream().map(StudentDto::getPesel).collect(Collectors.toSet())),
                    rs -> {
                        Map<String, Long> map = new HashMap<>();
                        while (rs.next()) {
                            map.put(rs.getString("pesel"), rs.getLong("id"));
                        }
                        return map;
                    });


            List<Map<String, Object>> studentBatch = new ArrayList<>();
            for (StudentDto student : batchList) {
                Long personId = peselToIdMap.get(student.getPesel());
                Map<String, Object> studentParams = new HashMap<>();
                studentParams.put("id", personId);
                studentParams.put("university_name", student.getUniversityName());
                studentParams.put("year_of_study", student.getYearOfStudy());
                studentParams.put("study_field", student.getStudyField());
                studentParams.put("scholarship", student.getScholarship());
                studentBatch.add(studentParams);
            }

            String studentSql = "INSERT INTO student (id, university_name, year_of_study, study_field, scholarship) VALUES (:id, :university_name, :year_of_study, :study_field, :scholarship)";
            namedParameterJdbcTemplate.batchUpdate(studentSql, studentBatch.toArray(new Map[0]));
            importStatusService.updateImportStatus(importStatus.getId(), StatusFile.INPROGRESS, importStatusService.getRowsImportStatus(importStatus.getId()) + batchList.size());

            batchList.clear();
        }
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
        parameters.put("type", "student");
        parameters.put("university_name", studentDto.getUniversityName());
        parameters.put("year_of_study", studentDto.getYearOfStudy());
        parameters.put("study_field", studentDto.getStudyField());
        parameters.put("scholarship", studentDto.getScholarship());
        parameters.put("version", 0L);
        logger.debug("Generated parameters: {}", parameters);
        String insertPersonSql = "INSERT INTO person (pesel, first_name, last_name, height, weight, email, type, version) VALUES (:pesel, :first_name, :last_name, :height, :weight, :email, :type, :version)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(insertPersonSql, new MapSqlParameterSource(parameters), keyHolder);

//        Long personId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        Long personId = Objects.requireNonNull(keyHolder.getKey()).longValue();

        String insertStudentSql = "INSERT INTO student (id, university_name, year_of_study, study_field, scholarship) VALUES (:id, :university_name, :year_of_study, :study_field, :scholarship)";
        parameters.put("id", personId);
        namedParameterJdbcTemplate.update(insertStudentSql, parameters);
        return studentDto;
    }
}
