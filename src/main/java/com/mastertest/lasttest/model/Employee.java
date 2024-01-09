package com.mastertest.lasttest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Entity
public class Employee extends Person {
    private Date employmentDate;
    @Column(unique = true, nullable = false)
    private String position;
    private Double salary;
}
