package com.mastertest.lasttest.strategy.imports;

import com.mastertest.lasttest.configuration.ConversionUtils;
import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.dto.EmployeeDto;
import com.mastertest.lasttest.model.dto.PersonDto;
import com.mastertest.lasttest.model.dto.command.CreatePersonCommand;
import com.mastertest.lasttest.model.factory.ImportStatus;
import com.mastertest.lasttest.model.factory.StatusFile;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.service.fileprocess.ImportStatusService;
import com.mastertest.lasttest.service.fileprocess.ImportStrategy;
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
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class EmployeeImportStrategy implements ImportStrategy<EmployeeDto> {

    private final PersonValidator validator;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final PersonRepository personRepository;
    private final ImportStatusService importStatusService;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeImportStrategy.class);
    private static final DateParser DATE_PARSER = FastDateFormat.getInstance("yyyy-MM-dd");
    private ThreadLocal<List<EmployeeDto>> threadLocalBatch = ThreadLocal.withInitial(ArrayList::new);


    @Override
    public void validateParseAndSave(String record) throws ParseException {
        EmployeeDto employeeDto = parseCsvToDto(record);
        validateDto(employeeDto);
        saveEmployee(employeeDto);
    }

    @Override
    public PersonDto validateAndSave(CreatePersonCommand<?> command) {
        EmployeeDto employeeDto = ConversionUtils.convertMapToDto((Map) command.getDetails(), EmployeeDto.class);
        validateDto(employeeDto);
        return saveEmployee(employeeDto);
    }

    @Override
    public void addToBatch(String record) throws ParseException {
        List<EmployeeDto> batch = threadLocalBatch.get();
        EmployeeDto employeeDto = parseCsvToDto(record);
        validateDto(employeeDto);
        batch.add(employeeDto);
    }


    @Override
    public void processBatch(ImportStatus importStatus) {
        List<EmployeeDto> batchList = threadLocalBatch.get();
            if (!batchList.isEmpty()) {
                List<Map<String, Object>> personBatch = new ArrayList<>();
                for (EmployeeDto employee : batchList) {
                    Map<String, Object> personParams = new HashMap<>();
                    personParams.put("pesel", employee.getPesel());
                    personParams.put("first_name", employee.getFirstName());
                    personParams.put("last_name", employee.getLastName());
                    personParams.put("height", employee.getHeight());
                    personParams.put("weight", employee.getWeight());
                    personParams.put("email", employee.getEmail());
                    personParams.put("type", "employee");
                    personParams.put("version", 0L);
                    personBatch.add(personParams);
                }
                String personSql = "INSERT INTO person (pesel, first_name, last_name, height, weight, email, type, version) VALUES (:pesel, :first_name, :last_name, :height, :weight, :email, :type, :version)";
                namedParameterJdbcTemplate.batchUpdate(personSql, personBatch.toArray(new Map[0]));

                String idRetrievalSql = "SELECT id, pesel FROM person WHERE pesel IN (:pesels)";
                Map<String, Long> peselToIdMap = namedParameterJdbcTemplate.query(idRetrievalSql,
                        Collections.singletonMap("pesels", batchList.stream().map(EmployeeDto::getPesel).collect(Collectors.toSet())),
                        rs -> {
                            Map<String, Long> map = new HashMap<>();
                            while (rs.next()) {
                                map.put(rs.getString("pesel"), rs.getLong("id"));
                            }
                            return map;
                        });


                List<Map<String, Object>> employeeBatch = new ArrayList<>();
                for (EmployeeDto employee : batchList) {
                    Long personId = peselToIdMap.get(employee.getPesel());
                    Map<String, Object> employeeParams = new HashMap<>();
                    employeeParams.put("id", personId);
                    employeeParams.put("employment_date", employee.getEmploymentDate());
                    employeeParams.put("position", employee.getPosition());
                    employeeParams.put("salary", employee.getSalary());
                    employeeBatch.add(employeeParams);
                }

                String sql = "INSERT INTO employee (id, employment_date, position, salary) VALUES (:id, :employment_date, :position, :salary)";
                namedParameterJdbcTemplate.batchUpdate(sql, employeeBatch.toArray(new Map[0]));

                importStatusService.updateImportStatus(importStatus.getId(), StatusFile.INPROGRESS, importStatusService.getRowsImportStatus(importStatus.getId()) + batchList.size());
                batchList.clear();
            }
        }



    private void validateDto(EmployeeDto employeeDto) {
        Optional<Person> personExisting = personRepository.findByPesel(employeeDto.getPesel());
        if (personExisting.isPresent()) {
            logger.error("Person with pesel: {} exists", employeeDto.getPesel());
            throw new EntityExistsException(MessageFormat.format("Person with pesel: {} exists", employeeDto.getPesel()));
        }
        validator.validate(employeeDto);
    }

    private EmployeeDto parseCsvToDto(String csvLine) throws ParseException {
        String[] fields = csvLine.split(",");
        if (fields.length < 10 || !"employee" .equalsIgnoreCase(fields[0])) {
            logger.error("Invalid CSV line for employee: {}", csvLine);
            throw new IllegalArgumentException("Invalid CSV line for employee");
        }
        return EmployeeDto.builder()
                .firstName(fields[1])
                .lastName(fields[2])
                .pesel(fields[3])
                .height(Double.parseDouble(fields[4]))
                .weight(Double.parseDouble(fields[5]))
                .email(fields[6])
                .employmentDate(DATE_PARSER.parse(fields[7]))
                .position(fields[8])
                .salary(Double.parseDouble(fields[9]))
                .build();
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

        Long personId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        String insertEmployeeSql = "INSERT INTO employee (id, employment_date, position, salary) VALUES (:id, :employment_date, :position, :salary)";
        parameters.put("id", personId);
        namedParameterJdbcTemplate.update(insertEmployeeSql, parameters);
        return employeeDto;
    }
}
