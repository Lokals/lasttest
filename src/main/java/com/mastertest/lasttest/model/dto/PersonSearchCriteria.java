package com.mastertest.lasttest.model.dto;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@RequiredArgsConstructor
public class PersonSearchCriteria {

    private String type;
    private String name;
    private Double heightFrom;
    private Double heightTo;
    private Double weightFrom;
    private Double weightTo;
    private String email;
    private Double salaryFrom;
    private Double salaryTo;
    private String universityName;


}
