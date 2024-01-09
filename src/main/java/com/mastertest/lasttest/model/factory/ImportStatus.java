package com.mastertest.lasttest.model.factory;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@Entity
public class ImportStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String filename;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @Enumerated(EnumType.STRING)
    private StatusFile status;
    private Long processedRows;
}
