package com.mastertest.lasttest.model.dto;

import com.mastertest.lasttest.model.Retiree;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RetireeDto {

    private Double pensionAmount;
    private Integer yearsWorked;


    public static RetireeDto fromEntity(Retiree retiree) {
        return RetireeDto.builder()
                .pensionAmount(retiree.getPensionAmount())
                .yearsWorked(retiree.getYearsWorked())
                .build();
    }
}
