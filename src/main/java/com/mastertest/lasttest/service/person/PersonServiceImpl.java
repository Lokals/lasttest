package com.mastertest.lasttest.service.person;

import com.mastertest.lasttest.model.persons.Person;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.strategy.update.UpdateStrategyManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {
    private static final Logger logger = LoggerFactory.getLogger(PersonServiceImpl.class);
    private final UpdateStrategyManager updateStrategyManager;
    private final PersonRepository personRepository;


    @Override
    public Person getPersonById(String pesel) {
        logger.info("Retrieving person by pesel: {}", pesel);
        return personRepository.findByPesel(pesel)
                .orElseThrow(() -> {
                    logger.error("Person with pesel={} not found", pesel);
                    return new EntityNotFoundException(
                            MessageFormat.format("person with pesel={0} not found", pesel));
                });
    }


    @Override
    public String getPersonType(String pesel) {
        return personRepository.findPersonTypeByPesel(pesel).orElseThrow(() -> {
            logger.error("Person with pesel={} not found", pesel);
            return new EntityNotFoundException(
                    MessageFormat.format("person with pesel={0} not found", pesel));
        });
    }
}
