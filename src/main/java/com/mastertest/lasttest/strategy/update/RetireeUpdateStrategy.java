package com.mastertest.lasttest.strategy.update;


import com.mastertest.lasttest.configuration.ConversionUtils;
import com.mastertest.lasttest.model.persons.Retiree;
import com.mastertest.lasttest.model.dto.RetireeDto;
import com.mastertest.lasttest.model.dto.command.UpdatePersonCommand;
import com.mastertest.lasttest.model.dto.command.UpdateRetireeCommand;
import com.mastertest.lasttest.repository.RetireeRepository;
import com.mastertest.lasttest.service.person.UpdateStrategy;
import com.mastertest.lasttest.validator.PersonValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@RequiredArgsConstructor
@Component
public class RetireeUpdateStrategy implements UpdateStrategy<RetireeDto, UpdateRetireeCommand> {

    private final RetireeRepository retireeRepository;
    private final PersonValidator personValidator;

    @Override
    public RetireeDto updateAndValidate(UpdatePersonCommand<UpdateRetireeCommand> updatePersonCommand, String pesel) throws Exception {
        UpdateRetireeCommand retireeCommand = ConversionUtils.convertCommandToCommand(updatePersonCommand.getDetails(), UpdateRetireeCommand.class);

        Retiree retiree = retireeRepository.findById(pesel).orElseThrow(() -> new EntityNotFoundException(
                MessageFormat.format("person with pesel={0} not found", pesel)));

        if (updatePersonCommand.getFirstName() != null) {
            retiree.setFirstName(updatePersonCommand.getFirstName());
        }
        if (updatePersonCommand.getLastName() != null) {
            retiree.setLastName(updatePersonCommand.getLastName());
        }
        if (updatePersonCommand.getHeight() != null) {
            retiree.setHeight(updatePersonCommand.getHeight());
        }
        if (updatePersonCommand.getWeight() != null) {
            retiree.setWeight(updatePersonCommand.getWeight());
        }
        if (updatePersonCommand.getEmail() != null) {
            retiree.setEmail(updatePersonCommand.getEmail());
        }
        if (retireeCommand != null) {
            if (retireeCommand.getPensionAmount() != null) {
                retiree.setPensionAmount(retireeCommand.getPensionAmount());
            }
            if (retireeCommand.getYearsWorked() != null) {
                retiree.setYearsWorked(retireeCommand.getYearsWorked());
            }
        }

        retireeRepository.save(retiree);
        return RetireeDto.fromEntity(retiree);

    }
}
