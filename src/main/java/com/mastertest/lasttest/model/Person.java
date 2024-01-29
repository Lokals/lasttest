package com.mastertest.lasttest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    @NotBlank(message = "First name cannot be blank")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    @NotNull(message = "Height cannot be null")
    @Positive(message = "Height must be positive")
    private Double height;

    @NotNull(message = "Weight cannot be null")
    @Positive(message = "Weight must be positive")
    private Double weight;

    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Type cannot be blank")
    private String type;
}
