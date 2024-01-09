package com.mastertest.lasttest.model;

import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@Entity
public class Student extends Person {

    private String universityName;
    private Integer yearOfStudy;
    private String studyField;
    private Double scholarship;
}
