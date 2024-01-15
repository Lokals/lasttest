package com.mastertest.lasttest.search;

import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.SearchCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

@RequiredArgsConstructor
public class PersonSearchSpecification implements Specification<Person> {

    private final SearchCriteria criteria;

    @Override
    public Predicate toPredicate(Root<Person> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        String key = criteria.getKey();
        String operation = criteria.getOperation();
        Object value = criteria.getValue();
        Object additionalValue = criteria.getAdditionalValue();

        if (operation.equals(":")) {
            Predicate like = builder.like(builder.lower(root.get(key).as(String.class)), "%" + value.toString().toLowerCase(Locale.ROOT) + "%");
            return like;
        } else if (operation.equals("between")) {
            Predicate between = builder.between(root.get(key), (Comparable) value, (Comparable) additionalValue);
            return between;
        }
        return null;
    }
}
