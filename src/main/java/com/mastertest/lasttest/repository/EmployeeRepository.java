package com.mastertest.lasttest.repository;


import com.mastertest.lasttest.model.persons.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    @Query("SELECT e FROM Employee e WHERE e.position = :position")
    Employee findByPosition(@Param("position") String position);


    @Query("SELECT e FROM Employee e WHERE e.pesel = :pesel")
    Employee findByPesel(@Param("pesel") String pesel);
}
