package com.mastertest.lasttest.controller;


import com.mastertest.lasttest.configuration.PersonManagementProperties;
import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.dto.EmployeePositionDto;
import com.mastertest.lasttest.model.dto.PersonDto;
import com.mastertest.lasttest.model.dto.PersonSearchCriteria;
import com.mastertest.lasttest.model.dto.command.CreatePersonCommand;
import com.mastertest.lasttest.model.dto.command.UpdateEmployeePositionCommand;
import com.mastertest.lasttest.model.dto.command.UpdatePersonCommand;
import com.mastertest.lasttest.model.factory.ImportStatus;
import com.mastertest.lasttest.model.factory.StatusFile;
import com.mastertest.lasttest.repository.ImportStatusRepository;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.service.employee.EmployeePositionService;
import com.mastertest.lasttest.service.fileprocess.CsvImportService;
import com.mastertest.lasttest.service.fileprocess.ImportStatusService;
import com.mastertest.lasttest.service.fileprocess.ImportStrategy;
//import com.mastertest.lasttest.service.person.PersonService;
import com.mastertest.lasttest.strategy.StrategyManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/people")
public class PersonController {

    private final PersonRepository personRepository;
    private final PersonManagementProperties properties;
    private final EmployeePositionService employeePositionService;
    private final CsvImportService csvImportService;
    private final ImportStatusService importStatusService;
    private final ImportStatusRepository importStatusRepository;
    private final StrategyManager strategyManager;

    @GetMapping("/search")
    public Page<Person> searchPeople(PersonSearchCriteria criteria, Pageable pageable) {
        if (pageable.getPageSize() > properties.getDefaultPageSize()) {
            pageable = PageRequest.of(pageable.getPageNumber(), properties.getDefaultPageSize(), pageable.getSort());
        }
        return personRepository.searchByCriteria(criteria, pageable);
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

//    @PostMapping("/update/{id}")
//    public ResponseEntity<PersonDto> updatePerson(@PathVariable Long id, @Valid @RequestBody UpdatePersonCommand command) {
//        try {
//            PersonDto updatedPerson = personService.update(id, command);
//            return ResponseEntity.ok(updatedPerson);
//        } catch (OptimisticLockException e) {
//            return ResponseEntity.status(HttpStatus.CONFLICT).build();
//        }
//    }

    @PostMapping("/employees/{employeeId}/positions")
    public ResponseEntity<EmployeePositionDto> updateEmployeePosition(@PathVariable Long employeeId,
                                                                      @Valid
                                                                      @RequestBody UpdateEmployeePositionCommand command) {
        EmployeePositionDto updatedPosition = employeePositionService.updatePositionToEmployee(employeeId, command);
        return ResponseEntity.ok(updatedPosition);
    }


    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (!importStatusRepository.findByStatuses(List.of(StatusFile.INPROGRESS, StatusFile.PENDING)).isEmpty()) {
            return ResponseEntity.badRequest().body("Already of some import is during processing");
        }
        ImportStatus importStatus = importStatusService.createNewImportStatus(file.getOriginalFilename());
        csvImportService.importCsv(file, importStatus);
        return ResponseEntity.ok(importStatus.getId());
    }

    @GetMapping("/importstatus/{id}")
    public ResponseEntity<ImportStatus> getImportStatus(@PathVariable Long id) {
        ImportStatus status = importStatusService.getImportStatus(id);
        return ResponseEntity.ok(status);
    }
}
