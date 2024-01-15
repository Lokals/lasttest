package com.mastertest.lasttest.repository;

import com.mastertest.lasttest.model.EmployeePosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeePositionRepository extends JpaRepository<EmployeePosition, Long> {

    @Query("SELECT ep FROM EmployeePosition ep WHERE ep.positionName = :positionName")
    List<EmployeePosition> findByPositionName(String positionName);


}
