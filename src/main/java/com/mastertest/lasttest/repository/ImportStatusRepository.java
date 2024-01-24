package com.mastertest.lasttest.repository;


import com.mastertest.lasttest.model.factory.ImportStatus;
import com.mastertest.lasttest.model.factory.StatusFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ImportStatusRepository extends JpaRepository<ImportStatus, Long> {


    @Query("SELECT s FROM ImportStatus s WHERE s.status IN :statuses")
    List<ImportStatus> findByStatuses(@Param("statuses") Collection<StatusFile> statuses);

    @Query("SELECT i.processedRows FROM ImportStatus i WHERE i.id = :id")
    Long findProcessedRowsById(@Param("id") Long id);

}
