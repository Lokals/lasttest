package com.mastertest.lasttest.controller;

import com.mastertest.lasttest.configuration.PersonManagementProperties;
import com.mastertest.lasttest.model.Employee;
import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.SearchCriteria;
import com.mastertest.lasttest.model.dto.PersonDto;
import com.mastertest.lasttest.model.dto.command.CreatePersonCommand;
import com.mastertest.lasttest.model.dto.command.UpdateEmployeeCommand;
import com.mastertest.lasttest.model.dto.command.UpdatePersonCommand;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.search.PersonSearchSpecification;
import com.mastertest.lasttest.service.fileprocess.ImportStrategy;
import com.mastertest.lasttest.service.person.PersonService;
import com.mastertest.lasttest.service.person.UpdateStrategy;
import com.mastertest.lasttest.strategy.imports.StrategyManager;

import com.mastertest.lasttest.strategy.update.UpdateStrategyManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/people")
public class PersonController {

    private final PersonRepository personRepository;
    private final PersonManagementProperties properties;
    private final PersonService personService;
    private final StrategyManager strategyManager;
    private final UpdateStrategyManager updateStrategyManager;

    private static final Logger logger = LoggerFactory.getLogger(PersonController.class);

    @GetMapping("/search")
    public Page<Person> searchPeople(@RequestParam Map<String, String> allParams, Pageable pageable) {
        Specification<Person> spec = Specification.where(null);
        if (pageable.getPageSize() > properties.getDefaultPageSize()) {
            pageable = PageRequest.of(pageable.getPageNumber(), properties.getDefaultPageSize(), pageable.getSort());
        }
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (value.contains(",")) {
                String[] range = value.split(",");
                if (range.length == 2) {
                    SearchCriteria criteriaBetween = new SearchCriteria(key, "between", range[0], range[1]);
                    spec = spec.and(new PersonSearchSpecification(criteriaBetween));
                }
            } else {
                SearchCriteria criteria = new SearchCriteria(key, ":", value, null);
                spec = spec.and(new PersonSearchSpecification(criteria));
            }
        }
        return personRepository.findAll(spec, pageable);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addPerson(@Valid @RequestBody CreatePersonCommand<?> command) {
        ImportStrategy<?> strategy = strategyManager.getStrategy(command.getType());
        if (strategy == null) {
            return ResponseEntity.badRequest().body("Unknown person type: " + command.getType());
        }

        try {
            PersonDto createdPerson = strategy.validateAndSave(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPerson);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<?> updatePerson(@PathVariable Long id, @Valid @RequestBody Map<String, Object> commandMap) {
        try {
            Person person = personService.getPersonById(id);
            UpdateStrategy updateStrategy = updateStrategyManager.getUpdateStrategy(person.getType());
            logger.debug("Selected update strategy: {}", updateStrategy.getClass().getSimpleName());
            PersonDto personDto = updateStrategy.updateAndValidate(commandMap, person);
            return ResponseEntity.ok(personDto);


//        } else{
////            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("updateStrategy not found");
//        }
        } catch (
                OptimisticLockException e) {
            logger.error("Entity edited during processing: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e);
        } catch (
                Exception e) {
            logger.error("Issue with processing request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
        }

    }
}
