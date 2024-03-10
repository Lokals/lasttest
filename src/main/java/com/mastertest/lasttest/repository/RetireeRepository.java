package com.mastertest.lasttest.repository;

import com.mastertest.lasttest.model.persons.Retiree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RetireeRepository extends JpaRepository<Retiree, String> {
}
