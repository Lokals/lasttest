package com.mastertest.lasttest.search;

import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.SearchCriteria;
import com.mastertest.lasttest.service.fileprocess.ImportStatusService;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

@RequiredArgsConstructor
public class PersonSearchSpecification implements Specification<Person> {

    private final SearchCriteria criteria;
    private static final Logger logger = LoggerFactory.getLogger(PersonSearchSpecification.class);

    @Override
    public Predicate toPredicate(Root<Person> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        String key = criteria.getKey();
        String operation = criteria.getOperation();
        Object value = criteria.getValue();
        Object additionalValue = criteria.getAdditionalValue();

        logger.debug("GENERATED criteria: {}", criteria);

        if (operation.equals(":")) {
            Predicate like = builder.like(builder.lower(root.get(key).as(String.class)), "%" + value.toString().toLowerCase(Locale.ROOT) + "%");
            logger.debug("GENERATED like: {}", like.toString());
            return like;
        } else if (operation.equals(">=") || operation.equals("<=") || operation.equals(">") || operation.equals("<")) {
            if (value != null){

                Predicate predicate = builder.greaterThanOrEqualTo(root.get(key), value.toString());
                logger.debug("GENERATED greaterThanOrEqualTo: {}", predicate.toString());
                return predicate;
            } else {
                return builder.conjunction();
            }
        } else if (operation.equals("between")) {
            Predicate between = builder.between(root.get(key), (Comparable) value, (Comparable) additionalValue);
            logger.debug("GENERATED between: {}", between.toString());
            return between;
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
