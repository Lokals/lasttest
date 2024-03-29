package com.mastertest.lasttest.model.search;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class SearchCriteria {

    private String key;
    private String operation;
    private Object value;
    private Object additionalValue;

}
