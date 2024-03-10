package com.mastertest.lasttest.controller;

import com.mastertest.lasttest.common.UnknownEntityTypeException;
import com.mastertest.lasttest.configuration.PersonManagementProperties;
import com.mastertest.lasttest.model.persons.Person;
import com.mastertest.lasttest.model.dto.PersonDto;
import com.mastertest.lasttest.model.dto.command.CreatePersonCommand;
import com.mastertest.lasttest.model.dto.command.UpdatePersonCommand;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.search.SpecificationGenerator;
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
    private final SpecificationGenerator specificationGenerator;

    private static final Logger logger = LoggerFactory.getLogger(PersonController.class);

    @GetMapping("/search")
    public Page<Person> searchPeople(@RequestParam Map<String, String> allParams, Pageable pageable) {

        if (pageable.getPageSize() > properties.getDefaultPageSize()) {
            pageable = PageRequest.of(pageable.getPageNumber(), properties.getDefaultPageSize(), pageable.getSort());
        }
        Specification<Person> specification = specificationGenerator.getSpecification(allParams);
        return personRepository.findAll(specification, pageable);
    }


    @PostMapping("/add")
    public ResponseEntity<?> addPerson(@Valid @RequestBody CreatePersonCommand<?> command) {
        ImportStrategy<?> strategy = strategyManager.getStrategy(command.getType());
        if (strategy == null) {
            throw new UnknownEntityTypeException("Unknown person type: " + command.getType());
        }
        try {
            PersonDto createdPerson = strategy.validateAndSave(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPerson);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{employeePesel}/update")
    public ResponseEntity<?> updatePerson(@PathVariable String employeePesel, @Valid @RequestBody UpdatePersonCommand<?> commandMap) {
        try {
            String type = personService.getPersonType(employeePesel);
            UpdateStrategy updateStrategy = updateStrategyManager.getUpdateStrategy(type);
            if (updateStrategy == null) {
                throw new UnknownEntityTypeException("Unknown person type: " + type);
            }
            PersonDto personDto = updateStrategy.updateAndValidate(commandMap, employeePesel);
            return ResponseEntity.ok(personDto);
        } catch (OptimisticLockException e) {
            logger.error("Entity edited during processing: {}", e.getMessage());
            throw new OptimisticLockException("Entity edited during processing");
        } catch (
                Exception e) {
            logger.error("Issue with processing request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }
}
