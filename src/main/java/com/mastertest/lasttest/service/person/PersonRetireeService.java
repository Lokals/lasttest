package com.mastertest.lasttest.service.person;

import com.mastertest.lasttest.model.Retiree;
import com.mastertest.lasttest.repository.PersonRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PersonRetireeService {

    private final PersonRepository personRepository;

    @Retryable(maxAttempts = 4, backoff = @Backoff(delay = 500))
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void savePersonsAndERetiree(List<Retiree> dtos) {

        personRepository.saveAll(dtos);

    }


}
