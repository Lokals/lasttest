package com.mastertest.lasttest.repository;

import com.mastertest.lasttest.model.persons.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, String>, JpaSpecificationExecutor<Person> {

    @Query("SELECT p FROM Person p WHERE p.pesel = :pesel")
    Optional<Person> findByPesel(@Param("pesel") String pesel);

    @Query(value = "SELECT type FROM person WHERE pesel = :pesel", nativeQuery = true)
    Optional<String> findPersonTypeByPesel(@Param("pesel") String pesel);

}
