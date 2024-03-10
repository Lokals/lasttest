package com.mastertest.lasttest.repository;

import com.mastertest.lasttest.model.importfile.ImportLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportLockRepository extends JpaRepository<ImportLock, Integer> {

}
