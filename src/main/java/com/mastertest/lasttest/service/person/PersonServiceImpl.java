package com.mastertest.lasttest.service.person;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mastertest.lasttest.model.*;
import com.mastertest.lasttest.model.dto.PersonDto;
import com.mastertest.lasttest.model.dto.command.CreatePersonCommand;
import com.mastertest.lasttest.model.dto.command.UpdatePersonCommand;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.strategy.update.UpdateStrategyManager;
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
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {
    private static final Logger logger = LoggerFactory.getLogger(PersonServiceImpl.class);
    private final UpdateStrategyManager updateStrategyManager;
    private final PersonRepository personRepository;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");


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

    @Transactional
    @Override
    public PersonDto updatePerson(Long id, Map<String, Object> commandMap) throws ParseException, JsonProcessingException {
        Person person = getPersonById(id);
        UpdateStrategy updateStrategy = updateStrategyManager.getUpdateStrategy(person.getType());
        return updateStrategy.updateAndValidate(commandMap, person);
    }
}
