package com.mastertest.lasttest.repository;

import com.mastertest.lasttest.model.Retiree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RetireeRepository extends JpaRepository<Retiree, Long> {
}
