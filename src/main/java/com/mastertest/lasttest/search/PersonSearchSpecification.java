package com.mastertest.lasttest.search;

import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.SearchCriteria;
import jakarta.persistence.criteria.*;
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
            return builder.like(builder.lower(root.get(key).as(String.class)), "%" + value.toString().toLowerCase(Locale.ROOT) + "%");
        } else if (operation.equals(">=") || operation.equals("<=") || operation.equals(">") || operation.equals("<")) {
            return builder.greaterThanOrEqualTo(root.get(key), value.toString());
        } else if (operation.equals("between")) {
            return builder.between(root.get(key), (Comparable) value, (Comparable) additionalValue);
        }
        return null;
    }

//    private Path<String> getPath(Root<T> root, String key) {
//        try {
//            return root.get(key);
//        } catch (IllegalArgumentException e) {
//            return null;
//        }
//    }
}
