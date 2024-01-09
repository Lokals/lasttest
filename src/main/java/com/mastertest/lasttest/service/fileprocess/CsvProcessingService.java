package com.mastertest.lasttest.service.fileprocess;


import com.mastertest.lasttest.configuratio.PersonManagementProperties;
import com.mastertest.lasttest.model.Employee;
import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.Retiree;
import com.mastertest.lasttest.model.Student;
import com.mastertest.lasttest.model.factory.ImportStatus;
import com.mastertest.lasttest.model.factory.StatusFile;
import com.mastertest.lasttest.repository.PersonRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.mastertest.lasttest.service.fileprocess.CsvRecordValidator.*;
import static com.mastertest.lasttest.service.fileprocess.CsvRecordValidator.isValidYearsWorked;

@AllArgsConstructor
@Service
public class CsvProcessingService {
    private final PersonRepository personRepository;
    private final ImportStatusService importStatusService;
    private final PersonManagementProperties managementProperties;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final Logger logger = LoggerFactory.getLogger(CsvImportServiceImpl.class);


    @Transactional
    public void processRecords(Iterable<CSVRecord> records, ImportStatus importStatus) throws ParseException {
        long processedRows = 0;
        List<Person> batch = new ArrayList<>();
        int batchSize = managementProperties.getBatchSize();

        for (CSVRecord record : records) {
            try {
                if (processedRows % batchSize == 0) {
                    logger.debug("Processing batch at row number: {}", processedRows);
                    importStatusService.updateImportStatus(importStatus.getId(), StatusFile.INPROGRESS, processedRows);
                }
                Person person = createPersonFromCsv(record);
                if (person != null) {
                    batch.add(person);
                }
                processedRows++;
                if (batch.size() >= batchSize) {
                    personRepository.saveAll(batch);
                    batch.clear();
                }
            } catch (Exception e) {
                logger.error("Error processing row number {}: {}", processedRows, e.getMessage());
                importStatusService.updateImportStatus(importStatus.getId(), StatusFile.FAILED, processedRows);
            }
        }

        if (!batch.isEmpty()) {
            personRepository.saveAll(batch);
        }
        logger.info("Completed processing file: {}, total rows processed: {}", importStatus.getFilename(), processedRows);
        importStatusService.updateImportStatus(importStatus.getId(), StatusFile.COMPLETED, processedRows);
    }

    private Person createPersonFromCsv(CSVRecord record) throws ParseException {
        return switch (record.get("type").toLowerCase()) {
            case "student" -> handleStudentRecord(record);
            case "employee" -> handleEmployeeRecord(record);
            case "retiree" -> handleRetireeRecord(record);
            default -> throw new IllegalArgumentException("Unknown person type");
        };
    }


    private void populateCommonFields(Person person, CSVRecord record) {
        if (!isValidName(record.get("firstName")) ||
                !isValidName(record.get("lastName")) ||
                !isValidHeight(Double.valueOf(record.get("height"))) ||
                !isValidWeight(Double.valueOf(record.get("weight"))) ||
                !isValidEmail(record.get("email")) ||
                !isValidPesel(record.get("pesel"))) {
            throw new IllegalArgumentException("Person data are incorrect");
        }
        if (personRepository.findByPesel(record.get("pesel")).isPresent()) {
            throw new EntityExistsException(
                    MessageFormat.format("Person with provided pesel= {0} already exist in database", record.get("pesel"))

            );
        }
        person.setFirstName(record.get("firstName"));
        person.setLastName(record.get("lastName"));
        person.setPesel(record.get("pesel"));
        person.setHeight(Double.valueOf(record.get("height")));
        person.setWeight(Double.valueOf(record.get("weight")));
        person.setEmail(record.get("email"));
    }

    Student handleStudentRecord(CSVRecord record) {
        Student student = new Student();
        populateCommonFields(student, record);
        if (!isValidUniversity(record.get("universityName")) ||
                !isValidYearOfStudy(Double.valueOf(record.get("yearOfStudy"))) ||
                !isValidScholarship(Double.valueOf(record.get("scholarship"))) ||
                !isValidStudyField(record.get("studyField"))) {
            return null;
        }
        student.setUniversityName(record.get("universityName"));
        student.setYearOfStudy(Double.valueOf(record.get("yearOfStudy")).intValue());
        student.setScholarship(Double.valueOf(record.get("scholarship")));
        student.setStudyField(record.get("studyField"));
        return student;

    }

    private Employee handleEmployeeRecord(CSVRecord record) throws ParseException {
        Employee employee = new Employee();

        populateCommonFields(employee, record);
        if (!isValidEmloymentDate(record.get("employmentDate")) ||
                !isValidPosition(record.get("position")) ||
                !isValidSalaryAmount(Double.valueOf(record.get("salary")))
        ) {
            return null;
        }

        employee.setEmploymentDate((DATE_FORMAT.parse(record.get("employmentDate"))));
        employee.setPosition(record.get("position"));
        employee.setSalary(Double.valueOf(record.get("salary")));
        return employee;

    }

    private Retiree handleRetireeRecord(CSVRecord record) {
        Retiree retiree = new Retiree();
        populateCommonFields(retiree, record);
        if (!isValidPensionAmount(Double.valueOf(record.get("pensionAmount"))) ||
                !isValidYearsWorked(Double.valueOf(record.get("yearsWorked")))) {
            return null;
        }
        retiree.setPensionAmount(Double.valueOf(record.get("pensionAmount")));
        retiree.setYearsWorked(Double.valueOf(record.get("yearsWorked")).intValue());

        return retiree;
    }
}
