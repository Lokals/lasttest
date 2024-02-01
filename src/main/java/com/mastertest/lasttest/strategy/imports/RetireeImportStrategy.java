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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    //    private List<Retiree> batchList = Collections.synchronizedList(new ArrayList<>());
//    private final CopyOnWriteArrayList<Retiree> batchList = new CopyOnWriteArrayList<>();
    private final Queue<Retiree> batchQueue = new ConcurrentLinkedQueue<>();
    private final PersonManagementProperties properties;


    @Override
    public Long getBatchSize() {
        return (long) batchQueue.size();

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
    public void addToBatch(String record) throws ParseException {
        Retiree retiree = parseCsvToDto(record);
        validate(retiree);
        batchQueue.add(retiree);

    }

    @Override
    public void processBatch(ImportStatus importStatus) {
        while (!batchQueue.isEmpty()) {
            List<Retiree> batch = new ArrayList<>();
            for (int i = 0; i < properties.getBatchSize() && !batchQueue.isEmpty(); i++) {
                batch.add(batchQueue.poll());
            }

            if (!batch.isEmpty()) {
                processBatchInternal(batch, importStatus);
            }
        }
    }

    private void processBatchInternal(List<Retiree> batch, ImportStatus importStatus) {
        logger.debug("Processing Retiree batch with size: {} on thread: {}", batch.size(), Thread.currentThread().getName());

        List<Map<String, Object>> personBatch = new ArrayList<>();
        Set<String> pesels = new HashSet<>();
        for (Retiree retiree : batch) {
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
                personBatch.add(personParams);
                pesels.add(retiree.getPesel());
            }
        }

        String personSql = "INSERT INTO person (pesel, first_name, last_name, height, weight, email, type, version) VALUES (:pesel, :first_name, :last_name, :height, :weight, :email, :type, :version)";
        namedParameterJdbcTemplate.batchUpdate(personSql, personBatch.toArray(new Map[0]));


        Map<String, Long> peselToIdMap = retrievePersonIds(pesels);
        logger.debug("EXTRACTED PESELS: {}", peselToIdMap);

        List<Map<String, Object>> retireeBatch = new ArrayList<>();
        for (Retiree retiree : batch) {
            Long personId = peselToIdMap.get(retiree.getPesel());
            Map<String, Object> params = new HashMap<>();
            params.put("id", personId);
            params.put("pension_amount", retiree.getPensionAmount());
            params.put("years_worked", retiree.getYearsWorked());
            retireeBatch.add(params);
        }
        String sql = "INSERT INTO retiree (id, pension_amount, years_worked) VALUES (:id, :pension_amount, :years_worked)";
        namedParameterJdbcTemplate.batchUpdate(sql, retireeBatch.toArray(new Map[0]));
        importStatusService.updateImportStatus(importStatus.getId(), StatusFile.INPROGRESS, importStatusService.getRowsImportStatus(importStatus.getId()) + batch.size());


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



    private Retiree parseCsvToDto(String csvLine) throws ParseException {
        logger.debug("RETIREE LIST: {}", csvLine);
        String[] fields = csvLine.split(",");
        if (fields.length < 9 || !"retiree" .equalsIgnoreCase(fields[0])) {
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
