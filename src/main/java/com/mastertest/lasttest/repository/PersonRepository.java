package com.mastertest.lasttest.repository;

import com.mastertest.lasttest.model.Person;
import com.mastertest.lasttest.model.SearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long>, JpaSpecificationExecutor<Person> {

    @Query("SELECT p FROM Person p WHERE p.pesel = :pesel")
    Optional<Person> findByPesel(@Param("pesel") String pesel);


}
