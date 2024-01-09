package com.mastertest.lasttest.model;


import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@Entity
public class Retiree extends Person {

    private Double pensionAmount;
    private Integer yearsWorked;
}
