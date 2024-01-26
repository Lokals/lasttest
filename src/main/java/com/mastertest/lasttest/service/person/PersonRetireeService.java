package com.mastertest.lasttest.service.person;

import com.mastertest.lasttest.model.Retiree;
import com.mastertest.lasttest.model.dto.RetireeDto;
import com.mastertest.lasttest.repository.PersonRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PersonRetireeService {

    private final PersonRepository personRepository;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void savePersonsAndERetiree(List<RetireeDto> dtos) {
        List<Retiree> retiree = dtos.stream()
                .map(this::convertToRetireeEntity)
                .collect(Collectors.toList());

        personRepository.saveAll(retiree);

    }

    private Retiree convertToRetireeEntity(RetireeDto dto) {
        Retiree retiree = new Retiree();
        retiree.setPesel(dto.getPesel());
        retiree.setType("retiree");
        retiree.setFirstName(dto.getFirstName());
        retiree.setLastName(dto.getLastName());
        retiree.setHeight(dto.getHeight());
        retiree.setWeight(dto.getWeight());
        retiree.setEmail(dto.getEmail());
        retiree.setPensionAmount(dto.getPensionAmount());
        retiree.setYearsWorked(dto.getYearsWorked());
        return retiree;
    }


}
