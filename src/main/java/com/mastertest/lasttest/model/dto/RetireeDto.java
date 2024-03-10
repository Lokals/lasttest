package com.mastertest.lasttest.model.dto;

import com.mastertest.lasttest.model.persons.Retiree;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetireeDto extends PersonDto{
    @NotNull(message = "Pension cannot be null")
    @PositiveOrZero(message = "Pension cannot be negative")
    private Double pensionAmount;
    @NotNull(message = "Worked years cannot be null")
    @Min(value = 1, message = "Worked years must be at least 1")
    private Integer yearsWorked;

    @Builder
    public RetireeDto(String firstName, String lastName, String pesel, Double height, Double weight, String email,
                      Double pensionAmount, Integer yearsWorked) {
        super(firstName, lastName, pesel, height, weight, email);
        this.pensionAmount = pensionAmount;
        this.yearsWorked = yearsWorked;

    }

    public static RetireeDto fromEntity(Retiree retiree) {
        return RetireeDto.builder()
                .firstName(retiree.getFirstName())
                .lastName(retiree.getLastName())
                .pesel(retiree.getPesel())
                .height(retiree.getHeight())
                .weight(retiree.getWeight())
                .email(retiree.getEmail())
                .pensionAmount(retiree.getPensionAmount())
                .yearsWorked(retiree.getYearsWorked())
                .build();
    }
}
