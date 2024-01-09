package com.mastertest.lasttest.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class PersonData {
    private String type;
    private String firstName;
    private String lastName;
    private String pesel;
    private Double height;
    private Double weight;
    private String email;

//
    private String universityName;
    private Integer yearOfStudy;
    private String studyField;
    private Double scholarship;

    // emer
    private Double pensionAmount;
    private Integer yearsWorked;

    // pracownika
    private Date employmentDate;
    private String position;
    private Double salary;
}
