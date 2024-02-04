package com.mastertest.lasttest.strategy.imports;

import com.mastertest.lasttest.configuration.ConversionUtils;
import com.mastertest.lasttest.configuration.PersonManagementProperties;
import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.Retiree;
import com.mastertest.lasttest.model.dto.PersonDto;
import com.mastertest.lasttest.model.dto.RetireeDto;
import com.mastertest.lasttest.model.dto.command.CreatePersonCommand;
import com.mastertest.lasttest.model.factory.ImportStatus;
import com.mastertest.lasttest.model.factory.StatusFile;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.service.fileprocess.ImportStatusService;
import com.mastertest.lasttest.service.fileprocess.ImportStrategy;
import com.mastertest.lasttest.service.person.PersonRetireeService;
import com.mastertest.lasttest.validator.PersonValidator;
import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
@Component
public class RetireeImportStrategy implements ImportStrategy<RetireeDto> {


    private final PersonValidator validator;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final PersonRepository personRepository;
    private final ImportStatusService importStatusService;
    private final PersonRetireeService personRetireeService;
    private static final Logger logger = LoggerFactory.getLogger(RetireeImportStrategy.class);
    private final PersonManagementProperties properties;
    private final CopyOnWriteArrayList<Map<String, Object>> personBatch = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Retiree> personRepoBatch = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Map<String, Object>> retireeBatch = new CopyOnWriteArrayList<>();

    String RETIREE_SQL = "INSERT INTO retiree (pesel, pension_amount, years_worked) VALUES (:pesel, :pension_amount, :years_worked)";
    String PERSON_SQL = "INSERT INTO person (pesel, first_name, last_name, height, weight, email, type, version) VALUES (:pesel, :first_name, :last_name, :height, :weight, :email, :type, :version)";

    @Override
    public Long getBatchSize() {
        return (long) personBatch.size();

    }

    @Override
    public void clearBatch() {
        personBatch.clear();
        personRepoBatch.clear();
        retireeBatch.clear();
    }

    @Override
    public void validateParseAndSave(String record) throws ParseException {
        Retiree retiree = parseCsvToDto(record);
        validate(retiree);
//        saveRetiree(retireeDto);
    }

    @Override
    public PersonDto validateAndSave(CreatePersonCommand<?> command) {
        RetireeDto retireeDto = ConversionUtils.convertMapToDto((Map) command.getDetails(), RetireeDto.class);
        validateDto(retireeDto);
        return saveRetiree(retireeDto);
    }

    @Override
    public void addToBatch(String record, ImportStatus importStatus) throws ParseException {
        Retiree retiree = parseCsvToDto(record);
        validate(retiree);
        personRepoBatch.add(retiree);
//        processPersonToBatch(retiree);
        if (getBatchSize() >= properties.getBatchSize()){
            processBatch();
            importStatusService.updateImportStatus(importStatus.getId(), StatusFile.INPROGRESS, importStatusService.getRowsImportStatus(importStatus.getId()) + getBatchSize());

        }
    }

    @Retryable(maxAttempts = 4, backoff = @Backoff(delay = 500))
    @Transactional
    @Override
    public void processBatch() {
//        processBatchInternal();
        trigger();
        clearBatch();
    }

    private void trigger(){
        personRetireeService.savePersonsAndERetiree(personRepoBatch);
    }

    private void processBatchInternal() {
        logger.debug("Processing Employee batch with size: {} on thread: {}", personBatch.size(), Thread.currentThread().getName());
        namedParameterJdbcTemplate.batchUpdate(PERSON_SQL, personBatch.toArray(new Map[0]));
        namedParameterJdbcTemplate.batchUpdate(RETIREE_SQL, retireeBatch.toArray(new Map[0]));

//        importStatusService.updateImportStatus(importStatus.getId(), StatusFile.INPROGRESS, importStatusService.getRowsImportStatus(importStatus.getId()) + personBatch.size());
    }

    private void processPersonToBatch(Retiree retiree) {
        if (retiree != null) {
            Map<String, Object> personParams = new HashMap<>();
            Map<String, Object> retireeParams = new HashMap<>();
            personParams.put("pesel", retiree.getPesel());
            personParams.put("first_name", retiree.getFirstName());
            personParams.put("last_name", retiree.getLastName());
            personParams.put("height", retiree.getHeight());
            personParams.put("weight", retiree.getWeight());
            personParams.put("email", retiree.getEmail());
            personParams.put("type", "retiree");
            personParams.put("version", 0L);
            retireeParams.put("pesel", retiree.getPesel());
            retireeParams.put("pension_amount", retiree.getPensionAmount());
            retireeParams.put("years_worked", retiree.getYearsWorked());
            retireeBatch.add(retireeParams);
            personBatch.add(personParams);
        }
     }


    private Retiree parseCsvToDto(String csvLine) throws ParseException {
        logger.debug("RETIREE LIST: {}", csvLine);
        String[] fields = csvLine.split(",");
        if (fields.length < 9 || !"retiree".equalsIgnoreCase(fields[0])) {
            logger.error("Invalid CSV line for retiree: {}", csvLine);
            throw new IllegalArgumentException("Invalid CSV line for retiree");
        }
        logger.debug("RETIREE LIST: {}", fields);
        Retiree retiree = new Retiree();
        retiree.setFirstName(fields[1]);
        retiree.setLastName(fields[2]);
        retiree.setPesel(fields[3]);
        retiree.setHeight(Double.parseDouble(fields[4]));
        retiree.setWeight(Double.parseDouble(fields[5]));
        retiree.setEmail(fields[6]);
        retiree.setType("retiree");
        retiree.setPensionAmount(Double.parseDouble(fields[7]));
        retiree.setYearsWorked(Integer.parseInt(fields[8]));
        return retiree;
    }

    private void validate(Retiree retireeDto) {
        Optional<Person> personExisting = personRepository.findByPesel(retireeDto.getPesel());
        if (personExisting.isPresent()) {
            logger.error("Person with pesel: {} exists", retireeDto.getPesel());
            throw new EntityExistsException(MessageFormat.format("Person with pesel: {} exists", retireeDto.getPesel()));
        }
        validator.validate(retireeDto);
    }


    private void validateDto(RetireeDto retireeDto) {
        Optional<Person> personExisting = personRepository.findByPesel(retireeDto.getPesel());
        if (personExisting.isPresent()) {
            logger.error("Person with pesel: {} exists", retireeDto.getPesel());
            throw new EntityExistsException(MessageFormat.format("Person with pesel: {} exists", retireeDto.getPesel()));
        }
        validator.validate(retireeDto);
    }

    private RetireeDto saveRetiree(RetireeDto retireeDto) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("pesel", retireeDto.getPesel());
        parameters.put("first_name", retireeDto.getFirstName());
        parameters.put("last_name", retireeDto.getLastName());
        parameters.put("height", retireeDto.getHeight());
        parameters.put("weight", retireeDto.getWeight());
        parameters.put("email", retireeDto.getEmail());
        parameters.put("type", "retiree");
        parameters.put("years_worked", retireeDto.getYearsWorked());
        parameters.put("pension_amount", retireeDto.getPensionAmount());
        parameters.put("version", 0L);

        String insertPersonSql = "INSERT INTO person (pesel, first_name, last_name, height, weight, email, type, version) VALUES (:pesel, :first_name, :last_name, :height, :weight, :email, :type, :version)";
        namedParameterJdbcTemplate.update(insertPersonSql, parameters);

        Long personId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        String insertRetireeSql = "INSERT INTO retiree (id, years_worked, pension_amount) VALUES (:id, :years_worked, :pension_amount)";
        parameters.put("id", personId);
        namedParameterJdbcTemplate.update(insertRetireeSql, parameters);
        return retireeDto;
    }
}
