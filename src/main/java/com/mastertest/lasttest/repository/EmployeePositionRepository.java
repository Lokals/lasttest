package com.mastertest.lasttest.repository;

import com.mastertest.lasttest.model.EmployeePosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeePositionRepository extends JpaRepository<EmployeePosition, Long> {

    @Query("SELECT ep FROM EmployeePosition ep WHERE ep.positionName = :positionName")
    List<EmployeePosition> findByPositionName(String positionName);

    @Query("SELECT ep FROM EmployeePosition ep WHERE ep.employee.id = :employeeId AND ep.id != :excludeId")
    List<EmployeePosition> findByEmployeeIdAndNotId(@Param("employeeId") Long employeeId, @Param("excludeId") Long excludeId);

}
