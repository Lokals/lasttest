package com.mastertest.lasttest.service.person;

import com.mastertest.lasttest.model.Retiree;
import com.mastertest.lasttest.repository.PersonRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PersonRetireeService {

    private final PersonRepository personRepository;
    private static final Logger logger = LoggerFactory.getLogger(PersonRetireeService.class);

    @Retryable(maxAttempts = 4, backoff = @Backoff(delay = 500))
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void savePersonsAndERetiree(List<Retiree> dtos) {
        logger.debug("RETIREE LIST SIZE: {}", dtos.size());
        personRepository.saveAll(dtos);

    }


}
