package com.mastertest.lasttest.service.person;

import com.mastertest.lasttest.model.*;
import com.mastertest.lasttest.model.dto.PersonDto;
import com.mastertest.lasttest.model.dto.command.CreatePersonCommand;
import com.mastertest.lasttest.model.dto.command.UpdatePersonCommand;
import com.mastertest.lasttest.repository.PersonRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {
    private static final Logger logger = LoggerFactory.getLogger(PersonServiceImpl.class);

    private final PersonRepository personRepository;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    @Transactional
    public PersonDto save(CreatePersonCommand command) {
        logger.info("Saving new person with pesel: {}", command.getPesel());

        validateCommand(command);
        Person person = createPersonFromCommand(command);
        personRepository.save(person);
        logger.debug("Person saved with id: {}", person.getId());

        return PersonDto.fromEntity(person);
    }

    private void validateCommand(CreatePersonCommand command) {
        if (command == null) {
            logger.error("CreatePersonCommand cannot be null");

            throw new IllegalArgumentException("CreatePersonCommand cannot be null");
        }
        if (personRepository.findByPesel(command.getPesel()).isPresent()) {
            logger.error("Person with provided pesel={} already exists", command.getPesel());

            throw new IllegalArgumentException(
                    MessageFormat.format("person with provided pesel={0} already exists", command.getPesel()));
        }
    }

    private Person createPersonFromCommand(CreatePersonCommand command) {
        return switch (command.getType().toLowerCase()) {
            case "student" -> createStudent(command);
            case "employee" -> createEmployee(command);
            case "retiree" -> createRetiree(command);
            default -> throw new IllegalArgumentException("Unknown person type");
        };
    }

    private Student createStudent(CreatePersonCommand command) {
        Student student = new Student();
        populateCommonFields(student, command);
        command.getAdditionalAttribute("universityName", String.class)
                .ifPresent(student::setUniversityName);
        command.getAdditionalAttribute("yearOfStudy", Integer.class)
                .ifPresent(student::setYearOfStudy);
        command.getAdditionalAttribute("studyField", String.class)
                .ifPresent(student::setStudyField);
        command.getAdditionalAttribute("scholarship", Double.class)
                .ifPresent(student::setScholarship);
        return student;
    }

    private Employee createEmployee(CreatePersonCommand command) {
        Employee employee = new Employee();
        populateCommonFields(employee, command);
        Optional<String> employmentDateString = command.getAdditionalAttribute("employmentDate", String.class);
        employmentDateString.ifPresent(dateStr -> {
            try {
                Date employmentDate = DATE_FORMAT.parse(dateStr);
                employee.setEmploymentDate(employmentDate);
            } catch (ParseException e) {
                logger.error("Parsing employee employment date error", e);
            }
        });
        command.getAdditionalAttribute("position", String.class)
                .ifPresent(employee::setPosition);
        command.getAdditionalAttribute("salary", Double.class)
                .ifPresent(employee::setSalary);
        return employee;
    }

    private Retiree createRetiree(CreatePersonCommand command) {
        Retiree retiree = new Retiree();
        populateCommonFields(retiree, command);
        command.getAdditionalAttribute("pensionAmount", Double.class)
                .ifPresent(retiree::setPensionAmount);
        command.getAdditionalAttribute("yearsWorked", Integer.class)
                .ifPresent(retiree::setYearsWorked);
        return retiree;
    }

    private void populateCommonFields(Person person, CreatePersonCommand command) {
        person.setFirstName(command.getFirstName());
        person.setLastName(command.getLastName());
        person.setPesel(command.getPesel());
        person.setHeight(command.getHeight());
        person.setWeight(command.getWeight());
        person.setEmail(command.getEmail());
    }

    @Override
    public PersonDto findById(Long id) {
        Person person = getPersonById(id);
        return PersonDto.fromEntity(person);
    }

    @Override
    public Person getPersonById(Long personId) {
        logger.info("Retrieving person by id: {}", personId);
        return personRepository.findById(personId)
                .orElseThrow(() -> {
                    logger.error("Person with id={} not found", personId);
                    return new EntityNotFoundException(
                            MessageFormat.format("person with id={0} not found", personId));
                });
    }

    @Override
    public PersonDto update(Long id, UpdatePersonCommand command) {
        logger.info("Updating person with id: {}", id);
        Person person = getPersonById(id);
        if (command.getFirstName() != null && !command.getFirstName().equals(person.getFirstName())) {
            person.setFirstName(command.getFirstName());
        }
        if (command.getLastName() != null && !command.getLastName().equals(person.getLastName())) {
            person.setLastName(command.getLastName());
        }
        if (command.getPesel() != null && !command.getPesel().equals(person.getPesel())) {
            person.setPesel(command.getPesel());
        }
        if (command.getHeight() != null && !command.getHeight().equals(person.getHeight())) {
            person.setHeight(command.getHeight());
        }
        if (command.getEmail() != null && !command.getEmail().equals(person.getEmail())) {
            person.setEmail(command.getEmail());
        }
        if (command.getWeight() != null && !command.getWeight().equals(person.getWeight())) {
            person.setWeight(command.getWeight());
        }
        if (person instanceof Student) {
            updateStudentFields((Student) person, command);
        } else if (person instanceof Employee) {
            updateEmployeeFields((Employee) person, command);
        } else if (person instanceof Retiree) {
            updateRetireeFields((Retiree) person, command);
        }
        logger.debug("Person updated with id: {}", id);
        personRepository.save(person);
        return PersonDto.fromEntity(person);
    }

    private void updateRetireeFields(Retiree retiree, UpdatePersonCommand command) {
        command.getAdditionalAttributes("pensionAmount", Double.class)
                .ifPresent(retiree::setPensionAmount);
        command.getAdditionalAttributes("yearsWorked", Integer.class)
                .ifPresent(retiree::setYearsWorked);
    }

    private void updateEmployeeFields(Employee employee, UpdatePersonCommand command) {
        Optional<String> employmentDateString =
                command.getAdditionalAttributes("employmentDate", String.class);
        employmentDateString.ifPresent(dateStr -> {
            try {
                Date employmentDate = DATE_FORMAT.parse(dateStr);
                employee.setEmploymentDate(employmentDate);
            } catch (ParseException e) {
                logger.error("Parsing employee employment date error", e);
            }
        });
        command.getAdditionalAttributes("position", String.class)
                .ifPresent(employee::setPosition);
        command.getAdditionalAttributes("salary", Double.class)
                .ifPresent(employee::setSalary);

    }

    private void updateStudentFields(Student student, UpdatePersonCommand command) {
        command.getAdditionalAttributes("universityName", String.class)
                .ifPresent(student::setUniversityName);
        command.getAdditionalAttributes("yearOfStudy", Integer.class)
                .ifPresent(student::setYearOfStudy);
        command.getAdditionalAttributes("studyField", String.class)
                .ifPresent(student::setStudyField);
        command.getAdditionalAttributes("scholarship", Double.class)
                .ifPresent(student::setScholarship);
    }

}
