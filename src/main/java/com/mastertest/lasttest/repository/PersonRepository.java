package com.mastertest.lasttest.repository;

import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.dto.PersonSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    @Query("SELECT p FROM Person p WHERE " +
            "(:#{#criteria.type} IS NULL OR " +
            "(CASE WHEN :#{#criteria.type} = 'student' THEN TYPE(p) = Student " +
            "WHEN :#{#criteria.type} = 'employee' THEN TYPE(p) = Employee " +
            "WHEN :#{#criteria.type} = 'retiree' THEN TYPE(p) = Retiree " +
            "ELSE TYPE(p) = Person END)) AND " +
            "(:#{#criteria.name} IS NULL OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :#{#criteria.name}, '%')) " +
            "OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :#{#criteria.name}, '%'))) AND " +
            "(:#{#criteria.heightFrom} IS NULL OR p.height >= :#{#criteria.heightFrom}) AND " +
            "(:#{#criteria.heightTo} IS NULL OR p.height <= :#{#criteria.heightTo}) AND " +
            "(:#{#criteria.weightFrom} IS NULL OR p.weight >= :#{#criteria.weightFrom}) AND " +
            "(:#{#criteria.weightTo} IS NULL OR p.weight <= :#{#criteria.weightTo}) AND " +
            "(:#{#criteria.email} IS NULL OR LOWER(p.email) LIKE LOWER(CONCAT('%', :#{#criteria.email}, '%'))) AND " +
            "(:#{#criteria.salaryFrom} IS NULL OR (TYPE(p) = Employee AND p.salary >= :#{#criteria.salaryFrom})) AND " +
            "(:#{#criteria.salaryTo} IS NULL OR (TYPE(p) = Employee AND p.salary <= :#{#criteria.salaryTo})) AND " +
            "(:#{#criteria.universityName} IS NULL OR (TYPE(p) = Student AND LOWER(p.universityName) LIKE LOWER(CONCAT('%', :#{#criteria.universityName}, '%'))))")
    Page<Person> searchByCriteria(@Param("criteria") PersonSearchCriteria criteria, Pageable pageable);

    @Query("SELECT p FROM Person p WHERE p.pesel = :pesel")
    Optional<Person> findByPesel(@Param("pesel") String pesel);


}
