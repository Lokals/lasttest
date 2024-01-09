package com.mastertest.lasttest.model.dto;

import com.mastertest.lasttest.model.Student;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentDto {
    private String universityName;
    private Integer yearOfStudy;
    private String studyField;
    private Double scholarship;

    public static StudentDto fromEntity(Student student){
        return StudentDto.builder()
                .universityName(student.getUniversityName())
                .yearOfStudy(student.getYearOfStudy())
                .studyField(student.getStudyField())
                .scholarship(student.getScholarship())
                .build();
    }
}
