package com.mastertest.lasttest.model.importfile;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;


@Getter
@Setter
@Entity
@Table(name = "import_lock")
public class ImportLock {

    @Id
    private Integer id;

    private Boolean isLocked;

    private Timestamp lockedAt;

}
