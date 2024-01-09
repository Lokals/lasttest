package com.mastertest.lasttest.model.dto;


import com.mastertest.lasttest.model.Person;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PersonDto {


    protected Long id;
    protected String pesel;
    protected String firstName;
    protected String lastName;
    protected Double height;
    protected Double weight;
    protected String email;


    public static PersonDto fromEntity(Person person){
        return PersonDto.builder()
                .id(person.getId())
                .pesel(person.getPesel())
                .firstName(person.getFirstName())
                .lastName(person.getLastName())
                .height(person.getHeight())
                .weight(person.getWeight())
                .email(person.getEmail())
                .build();
    }
}
