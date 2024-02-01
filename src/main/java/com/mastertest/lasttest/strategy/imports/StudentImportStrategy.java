package com.mastertest.lasttest.strategy.imports;

import com.mastertest.lasttest.configuration.ConversionUtils;
import com.mastertest.lasttest.configuration.PersonManagementProperties;
import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.Student;
import com.mastertest.lasttest.model.dto.PersonDto;
import com.mastertest.lasttest.model.dto.StudentDto;
import com.mastertest.lasttest.model.dto.command.CreatePersonCommand;
import com.mastertest.lasttest.model.factory.ImportStatus;
import com.mastertest.lasttest.model.factory.StatusFile;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.service.fileprocess.ImportStatusService;
import com.mastertest.lasttest.service.fileprocess.ImportStrategy;
import com.mastertest.lasttest.service.person.PersonStudentService;
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
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
@Component
public class StudentImportStrategy implements ImportStrategy<StudentDto> {

    private final PersonValidator personValidator;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final PersonRepository personRepository;
    private final ImportStatusService importStatusService;
    private final PersonStudentService personStudentService;
    private final PersonManagementProperties properties;

    private static final Logger logger = LoggerFactory.getLogger(StudentImportStrategy.class);
    //    private final CopyOnWriteArrayList<Student> batchList = new CopyOnWriteArrayList<>();
    private final Queue<Student> batchQueue = new ConcurrentLinkedQueue<>();

    @Override
    public Long getBatchSize() {
        return (long) batchQueue.size();
    }

    @Override
    public void validateParseAndSave(String record) {
        Student student = parseCsvToDto(record);
        validate(student);
//        saveStudent(studentDto);
    }

    @Override
    public PersonDto validateAndSave(CreatePersonCommand<?> command) {
        StudentDto studentDto = ConversionUtils.convertMapToDto((Map) command.getDetails(), StudentDto.class);
        validateDto(studentDto);
        return saveStudent(studentDto);
    }

    @Override
    public void addToBatch(String record) {

        Student student = parseCsvToDto(record);
        validate(student);
        batchQueue.add(student);


    }
    @Override
    public void processBatch(ImportStatus importStatus) {
        final int BATCH_SIZE = properties.getBatchSize();

        while (!batchQueue.isEmpty()) {
            List<Student> batch = new ArrayList<>();
            for (int i = 0; i < BATCH_SIZE && !batchQueue.isEmpty(); i++) {
                batch.add(batchQueue.poll());
            }

            if (!batch.isEmpty()) {
                processBatchInternal(batch, importStatus);
            }
        }
    }
    private void processBatchInternal(List<Student> batch, ImportStatus importStatus) {
        logger.debug("Processing Student batch with size: {} on thread: {}", batch.size(), Thread.currentThread().getName());

        List<Map<String, Object>> personBatch = new ArrayList<>();
        Set<String> pesels = new HashSet<>();
        for (Student student : batch) {
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
                personBatch.add(personParams);
                pesels.add(student.getPesel());
            }
        }
        String personSql = "INSERT INTO person (pesel, first_name, last_name, height, weight, email, type, version) VALUES (:pesel, :first_name, :last_name, :height, :weight, :email, :type, :version)";
        namedParameterJdbcTemplate.batchUpdate(personSql, personBatch.toArray(new Map[0]));


        Map<String, Long> peselToIdMap = retrievePersonIds(pesels);
        logger.debug("EXTRACTED PESELS: {}", peselToIdMap);
        List<Map<String, Object>> studentBatch = new ArrayList<>();
        for (Student student : batch) {
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


        importStatusService.updateImportStatus(importStatus.getId(), StatusFile.INPROGRESS, importStatusService.getRowsImportStatus(importStatus.getId()) + batch.size());
    }


    private Map<String, Long> retrievePersonIds(Set<String> pesels) {
        if (pesels.isEmpty()) {
            return Collections.emptyMap();
        }
        String idRetrievalSql = "SELECT id, pesel FROM person WHERE pesel IN (:pesels)";
        Map<String, Object> params = Collections.singletonMap("pesels", pesels);

        return namedParameterJdbcTemplate.query(idRetrievalSql, params, rs -> {
            Map<String, Long> map = new HashMap<>();
            while (rs.next()) {
                map.put(rs.getString("pesel"), rs.getLong("id"));
            }
            return map;
        });
    }

    private void validate(Student student) {
        Optional<Person> personExisting = personRepository.findByPesel(student.getPesel());
        if (personExisting.isPresent()) {
            logger.error("Person with pesel: {} exists", student.getPesel());
            throw new EntityExistsException(MessageFormat.format("Person with pesel: {} exists", student.getPesel()));
        }
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

        if (fields.length < 11 || !"student" .equalsIgnoreCase(fields[0])) {
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
        student.setType("student");
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
