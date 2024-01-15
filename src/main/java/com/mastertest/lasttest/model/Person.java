package com.mastertest.lasttest.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Setter
@Inheritance(strategy = InheritanceType.JOINED)
@Entity
@DynamicUpdate
public abstract class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version
    private Long version;
    @Column(unique = true, nullable = false)
    private String pesel;

    private String firstName;
    private String lastName;
    private Double height;
    private Double weight;
    private String email;
    private String type;
}
