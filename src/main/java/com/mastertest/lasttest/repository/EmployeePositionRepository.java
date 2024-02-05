package com.mastertest.lasttest.repository;

import com.mastertest.lasttest.model.EmployeePosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeePositionRepository extends JpaRepository<EmployeePosition, String> {

    @Query("SELECT ep FROM EmployeePosition ep WHERE ep.positionName = :positionName")
    List<EmployeePosition> findByPositionName(String positionName);

    @Query("SELECT ep FROM EmployeePosition ep WHERE ep.employee.pesel = :pesel")
    List<EmployeePosition> findByEmployeePesel(@Param("pesel") String pesel);



}
