package com.mastertest.lasttest.service.person;

import com.mastertest.lasttest.model.persons.Person;
import com.mastertest.lasttest.model.persons.Student;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.strategy.update.UpdateStrategyManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonServiceImplTest {
    @InjectMocks
    private PersonServiceImpl personService;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private UpdateStrategyManager updateStrategyManager;

    @Mock
    private UpdateStrategy updateStrategy;

    private static Person person;

    private static Map<String, Object> commandMap;

    @BeforeEach
    void setUp() {
        person = new Student();
        person.setFirstName("Test");
        person.setLastName("Testowy");
        person.setHeight(180.0);
        person.setWeight(50.0);
        person.setPesel("85040612346");
        person.setEmail("test.testowy@test.com");
        commandMap = Map.of(
                "firstName", "Cycek",
                "lastName", "Bicek",
                "pesel", "85040612345",
                "height", 165.0,
                "weight", 60.0,
                "email", "cycek.nowak@example.com",
                "universityName", "Uniwersytet Cyckow",
                "yearOfStudy", 2,
                "studyField", "Malarstwo",
                "scholarship", 901.0
        );
        System.out.println(person.getPesel());
        personRepository.save(person);
    }

    @Test
    void testGetPersonById_ExistingId_ReturnsPerson(){
        String  personId = "85040612346";
        System.out.println(personRepository.findAll());
        when(personRepository.findById(personId)).thenReturn(Optional.of(person));
        Person result = personService.getPersonById(personId);

        assertNotNull(result);
        assertEquals(person, result);
        verify(personRepository).findById(personId);
    }

    @Test
    void testGetPersonById_NonExistingId_ThrowsEntityNotFoundException() {
        String  personId = "99999999991";

        when(personRepository.findById(personId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> personService.getPersonById(personId));
        verify(personRepository).findById(personId);
    }

}