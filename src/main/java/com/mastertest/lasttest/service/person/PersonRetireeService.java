package com.mastertest.lasttest.service.person;

import com.mastertest.lasttest.model.Retiree;
import com.mastertest.lasttest.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;


@Service
@RequiredArgsConstructor
public class PersonRetireeService {

    private final PersonRepository personRepository;


    public void savePersonsAndERetiree(Set<Retiree> dtos) {
        personRepository.saveAll(dtos);

    }


}
