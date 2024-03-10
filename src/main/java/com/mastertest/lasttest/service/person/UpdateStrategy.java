package com.mastertest.lasttest.service.person;

import com.mastertest.lasttest.model.dto.PersonDto;
import com.mastertest.lasttest.model.dto.command.UpdatePersonCommand;

public interface UpdateStrategy<T extends PersonDto, U> {
    T updateAndValidate(UpdatePersonCommand<U> updatePersonCommand, String pesel) throws Exception;

}
