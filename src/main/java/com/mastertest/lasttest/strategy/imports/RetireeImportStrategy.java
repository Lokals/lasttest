package com.mastertest.lasttest.strategy.imports;

import com.mastertest.lasttest.configuration.ConversionUtils;
import com.mastertest.lasttest.model.dto.PersonDto;
import com.mastertest.lasttest.model.dto.RetireeDto;
import com.mastertest.lasttest.model.dto.command.CreatePersonCommand;
import com.mastertest.lasttest.model.importfile.ImportStatus;
import com.mastertest.lasttest.model.persons.Person;
import com.mastertest.lasttest.model.persons.Retiree;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.service.fileprocess.ImportStrategy;
import com.mastertest.lasttest.validator.PersonValidator;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;

@RequiredArgsConstructor
@Component
public class RetireeImportStrategy implements ImportStrategy<RetireeDto> {


    private final PersonValidator validator;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final PersonRepository personRepository;
    private final ConversionUtils conversionUtils;
    private static final Logger logger = LoggerFactory.getLogger(RetireeImportStrategy.class);
    List<Map<String, Object>> personBatch = new ArrayList<>();
    String PERSON_SQL = "INSERT INTO person (pesel, first_name, last_name, height, weight, email, type, version, pension_amount, years_worked) " +
            "VALUES (:pesel, :first_name, :last_name, :height, :weight, :email, :type, :version, :pension_amount, :years_worked) " +
            "ON DUPLICATE KEY UPDATE " +
            "first_name = VALUES(first_name), " +
            "last_name = VALUES(last_name), " +
            "height = VALUES(height), " +
            "weight = VALUES(weight), " +
            "email = VALUES(email), " +
            "type = VALUES(type), " +
            "version = version + 1, " +
            "pension_amount = VALUES(pension_amount), " +
            "years_worked = VALUES(years_worked);";


    @Override
    public void clearBatch() {
        personBatch.clear();

    }

    @Override
    public PersonDto validateAndSave(CreatePersonCommand<?> command) {
        RetireeDto retireeDto = conversionUtils.convertMapToDto((Map) command.getDetails(), RetireeDto.class);
        validateDto(retireeDto);
        return saveRetiree(retireeDto);
    }


    @Override
    public void addToBatch(String record, ImportStatus importStatus) throws ParseException {
        Retiree retiree = parseCsvToDto(record);
        validate(retiree);
        processPersonToBatch(retiree);

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


    private void processPersonToBatch(Retiree retiree) {
        if (retiree != null) {
            Map<String, Object> personParams = new HashMap<>();
            personParams.put("pesel", retiree.getPesel());
            personParams.put("first_name", retiree.getFirstName());
            personParams.put("last_name", retiree.getLastName());
            personParams.put("height", retiree.getHeight());
            personParams.put("weight", retiree.getWeight());
            personParams.put("email", retiree.getEmail());
            personParams.put("type", "retiree");
            personParams.put("version", 0L);
            personParams.put("pension_amount", retiree.getPensionAmount());
            personParams.put("years_worked", retiree.getYearsWorked());
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
        retiree.setPensionAmount(Double.parseDouble(fields[7]));
        retiree.setYearsWorked(Integer.parseInt(fields[8]));
        return retiree;
    }

    private void validate(Retiree retiree) {
        validator.validate(retiree);
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

        namedParameterJdbcTemplate.update(PERSON_SQL, parameters);


        return retireeDto;
    }
}
