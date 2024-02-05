package com.mastertest.lasttest.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
public class EmployeePosition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_pesel")
    private Employee employee;

    @Column(unique = true, nullable = false)
    private String positionName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double salary;
}
