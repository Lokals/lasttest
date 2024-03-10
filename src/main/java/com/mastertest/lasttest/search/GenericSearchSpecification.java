package com.mastertest.lasttest.search;


import com.mastertest.lasttest.model.search.SearchCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public class GenericSearchSpecification<T> implements Specification<T> {

    private final SearchCriteria criteria;

    public GenericSearchSpecification(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        String key = criteria.getKey();
        String operation = criteria.getOperation();
        Object value = criteria.getValue();
        Object additionalValue = criteria.getAdditionalValue();

        if ("between".equals(operation) && additionalValue != null) {
            return builder.between(root.get(key).as(Comparable.class), (Comparable) value, (Comparable) additionalValue);
        } else if (":".equals(operation)) {
            return builder.like(builder.lower(root.get(key).as(String.class)), "%" + value.toString().toLowerCase(Locale.ROOT) + "%");
        }
        return null;
    }
}
