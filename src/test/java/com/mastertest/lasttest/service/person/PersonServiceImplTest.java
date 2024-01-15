package com.mastertest.lasttest.service.person;

import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.strategy.update.UpdateStrategyManager;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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

    private Person person;
    private Map<String, Object> commandMap;

    @BeforeEach
    void setUp() {
        person = new Person();
        commandMap = Map.of();
        when(updateStrategyManager.getUpdateStrategy(anyString())).thenReturn(updateStrategy);
        when(personRepository.findById(anyLong())).thenReturn(Optional.of(person));
    }
}