package com.mastertest.lasttest.strategy.imports;

import com.mastertest.lasttest.configuration.ConversionUtils;
import com.mastertest.lasttest.model.Employee;
import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.dto.EmployeeDto;
import com.mastertest.lasttest.model.dto.PersonDto;
import com.mastertest.lasttest.model.dto.command.CreatePersonCommand;
import com.mastertest.lasttest.model.factory.ImportStatus;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.service.fileprocess.ImportStatusService;
import com.mastertest.lasttest.service.fileprocess.ImportStrategy;
import com.mastertest.lasttest.service.person.PersonEmployeeService;
import com.mastertest.lasttest.validator.PersonValidator;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateParser;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

@RequiredArgsConstructor
@Component
public class EmployeeImportStrategy implements ImportStrategy<EmployeeDto> {

    private final PersonValidator validator;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final PersonRepository personRepository;
    private final ImportStatusService importStatusService;
    private final PersonEmployeeService employeeService;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeImportStrategy.class);
    private static final DateParser DATE_PARSER = FastDateFormat.getInstance("yyyy-MM-dd");
    private final CopyOnWriteArrayList<Map<String, Object>> personBatch = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Map<String, Object>> employeeBatch = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArraySet<Employee> personRepoBatch = new CopyOnWriteArraySet<>();

    private final String PERSON_SQL = "INSERT INTO person (pesel, first_name, last_name, height, weight, email, type, version) VALUES (:pesel, :first_name, :last_name, :height, :weight, :email, :type, :version)";
    private final String EMPLOYEE_SQL = "INSERT INTO employee (pesel, employment_date, position, salary) VALUES (:pesel, :employment_date, :position, :salary)";


    @Override
    public Long getBatchSize() {
        return (long) personRepoBatch.size();
    }

    @Override
    public void clearBatch() {
        personBatch.clear();
        personRepoBatch.clear();
        employeeBatch.clear();

    }

    @Override
    public void validateParseAndSave(String record) throws ParseException {
        Employee employeeDto = parseCsvToDto(record);
        validate(employeeDto);
    }

    @Override
    public PersonDto validateAndSave(CreatePersonCommand<?> command) {
        EmployeeDto employeeDto = ConversionUtils.convertMapToDto((Map) command.getDetails(), EmployeeDto.class);
        validateDto(employeeDto);
        return saveEmployee(employeeDto);
    }

    @Override
    public void addToBatch(String record, ImportStatus importStatus) throws ParseException {
        Employee employee = parseCsvToDto(record);
        validate(employee);
        personRepoBatch.add(employee);

    }

    @Override
    public void processBatch() {
        logger.debug("Processing Employee batch with size: {} on thread: {}", personRepoBatch.size(), Thread.currentThread().getName());
        employeeService.savePersonsAndEmployee(personRepoBatch);
        clearBatch();
    }

    private void processPersonToBatch(Employee employee) {
        if (employee != null) {
            Map<String, Object> personParams = new HashMap<>();
            Map<String, Object> employeeParams = new HashMap<>();
            personParams.put("pesel", employee.getPesel());
            personParams.put("first_name", employee.getFirstName());
            personParams.put("last_name", employee.getLastName());
            personParams.put("height", employee.getHeight());
            personParams.put("weight", employee.getWeight());
            personParams.put("email", employee.getEmail());
            personParams.put("type", "employee");
            personParams.put("version", 0L);
            employeeParams.put("pesel", employee.getPesel());
            employeeParams.put("employment_date", employee.getEmploymentDate());
            employeeParams.put("position", employee.getPosition());
            employeeParams.put("salary", employee.getSalary());
            employeeBatch.add(employeeParams);
            personBatch.add(personParams);
        }
    }

    private void processBatchInternal() {
        logger.debug("Processing Employee batch with size: {} on thread: {}", personBatch.size(), Thread.currentThread().getName());
        namedParameterJdbcTemplate.batchUpdate(PERSON_SQL, personBatch.toArray(new Map[0]));

        namedParameterJdbcTemplate.batchUpdate(EMPLOYEE_SQL, employeeBatch.toArray(new Map[0]));

    }


    private void validate(Employee employee) {
        Optional<Person> personExisting = personRepository.findByPesel(employee.getPesel());
        if (personExisting.isPresent()) {
            logger.error("Person with pesel: {} exists", employee.getPesel());
            throw new EntityExistsException(MessageFormat.format("Person with pesel: {} exists", employee.getPesel()));
        }
        validator.validate(employee);
    }

    private void validateDto(EmployeeDto employee) {
        Optional<Person> personExisting = personRepository.findByPesel(employee.getPesel());
        if (personExisting.isPresent()) {
            logger.error("Person with pesel: {} exists", employee.getPesel());
            throw new EntityExistsException(MessageFormat.format("Person with pesel: {} exists", employee.getPesel()));
        }
        validator.validate(employee);
    }

    private Employee parseCsvToDto(String csvLine) throws ParseException {
        logger.debug("EMPLOYEE LIST: {}", csvLine);
        String[] fields = csvLine.split(",");
        if (fields.length < 10 || !"employee".equalsIgnoreCase(fields[0])) {
            logger.error("Invalid CSV line for employee: {}", csvLine);
            throw new IllegalArgumentException("Invalid CSV line for employee");
        }
        logger.debug("EMPLOYEE LIST: {}", fields);
        Employee employee = new Employee();
        employee.setFirstName(fields[1]);
        employee.setLastName(fields[2]);
        employee.setPesel(fields[3]);
        employee.setHeight(Double.parseDouble(fields[4]));
        employee.setWeight(Double.parseDouble(fields[5]));
        employee.setEmail(fields[6]);
        employee.setType("employee");
        employee.setEmploymentDate(DATE_PARSER.parse(fields[7]));
        employee.setPosition(fields[8]);
        employee.setSalary(Double.parseDouble(fields[9]));
        return employee;
    }

    private EmployeeDto saveEmployee(EmployeeDto employeeDto) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("pesel", employeeDto.getPesel());
        parameters.put("first_name", employeeDto.getFirstName());
        parameters.put("last_name", employeeDto.getLastName());
        parameters.put("height", employeeDto.getHeight());
        parameters.put("weight", employeeDto.getWeight());
        parameters.put("email", employeeDto.getEmail());
        parameters.put("type", "employee");
        parameters.put("employment_date", employeeDto.getEmploymentDate());
        parameters.put("position", employeeDto.getPosition());
        parameters.put("salary", employeeDto.getSalary());
        parameters.put("version", 0L);

        String insertPersonSql = "INSERT INTO person (pesel, first_name, last_name, height, weight, email, type, version) VALUES (:pesel, :first_name, :last_name, :height, :weight, :email, :type, :version)";
        namedParameterJdbcTemplate.update(insertPersonSql, parameters);

        String insertEmployeeSql = "INSERT INTO employee (pesel, employment_date, position, salary) VALUES (:pesel, :employment_date, :position, :salary)";
        namedParameterJdbcTemplate.update(insertEmployeeSql, parameters);
        return employeeDto;
    }
}
