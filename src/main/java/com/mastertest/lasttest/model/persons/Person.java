package com.mastertest.lasttest.model.persons;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Setter
@Entity
@DynamicUpdate
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class Person {


    @Version
    private Long version;
    @Id
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

    @NotNull(message = "Email cannot be null")
    @Email(message = "Invalid email format")
    private String email;

    @Column(insertable = false, updatable = false)
    private String type;

}
