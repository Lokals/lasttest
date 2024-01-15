package com.mastertest.lasttest.search;

import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.SearchCriteria;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SpecificationGenerator {

    public Specification<Person> getSpecification(Map<String, String> allParams) {
        Specification<Person> spec = Specification.where(null);
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (value.contains(",")) {
                String[] range = value.split(",");
                if (range.length == 2) {
                    SearchCriteria criteriaBetween = new SearchCriteria(key, "between", range[0], range[1]);
                    spec = spec.and(new PersonSearchSpecification(criteriaBetween));
                }
            } else {
                SearchCriteria criteria = new SearchCriteria(key, ":", value, null);
                spec = spec.and(new PersonSearchSpecification(criteria));
            }
        }
        return spec;
    }
}
