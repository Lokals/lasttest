package com.mastertest.lasttest.strategy.update;


import com.mastertest.lasttest.configuration.ConversionUtils;
import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.Retiree;
import com.mastertest.lasttest.model.dto.PersonDto;
import com.mastertest.lasttest.model.dto.RetireeDto;
import com.mastertest.lasttest.model.dto.command.UpdatePersonCommand;
import com.mastertest.lasttest.model.dto.command.UpdateRetireeCommand;
import com.mastertest.lasttest.repository.PersonRepository;
import com.mastertest.lasttest.service.person.UpdateStrategy;
import com.mastertest.lasttest.validator.PersonValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class RetireeUpdateStrategy implements UpdateStrategy<UpdateRetireeCommand> {

    private final PersonRepository personRepository;
    private final PersonValidator personValidator;

    @Override
    public PersonDto updateAndValidate(Map<String, Object> updateCommand, Person person) {

        Retiree retiree = (Retiree) person;
        UpdateRetireeCommand retireeCommand = ConversionUtils.convertMapToCommand(updateCommand, UpdateRetireeCommand.class);

        updateCommonFields(retireeCommand, retiree);

        if (retireeCommand.getYearsWorked() != null) {
            retiree.setYearsWorked (retireeCommand.getYearsWorked());
        }
        if (retireeCommand.getPensionAmount() != null) {
            retiree.setPensionAmount(retireeCommand.getPensionAmount());
        }

        return convertToPersonDto(retiree);
    }


    private void updateCommonFields(UpdatePersonCommand updateCommand, Retiree retiree) {
        if (updateCommand.getFirstName() != null){
            retiree.setFirstName(updateCommand.getFirstName());
        }
        if (updateCommand.getLastName() != null){
            retiree.setLastName(updateCommand.getLastName());
        }
        if (updateCommand.getHeight() != null){
            retiree.setHeight(updateCommand.getHeight());
        }
        if (updateCommand.getWeight() != null){
            retiree.setWeight(updateCommand.getWeight());
        }
        if (updateCommand.getEmail() != null){
            retiree.setEmail(updateCommand.getEmail());
        }
    }

    private PersonDto convertToPersonDto(Retiree retiree) {
        RetireeDto retireeDto = RetireeDto.fromEntity(retiree);
        personValidator.validate(retireeDto);
        personRepository.save(retiree);
        return retireeDto;
    }
}
