package com.mastertest.lasttest.model.dto;

import com.mastertest.lasttest.model.Student;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDto extends PersonDto{

    @NotBlank(message = "University name cannot be blank")
    private String universityName;
    @NotNull(message = "Year of study cannot be null")
    @Min(value = 1, message = "Year of study must be at least 1")
    private Integer yearOfStudy;
    @NotBlank(message = "Study field cannot be blank")
    private String studyField;
    @NotNull(message = "Scholarship cannot be null")
    @PositiveOrZero(message = "Scholarship cannot be negative")
    private Double scholarship;

    @Builder
    public StudentDto(String firstName, String lastName, String pesel, Double height, Double weight, String email,
                      String universityName, Integer yearOfStudy, String studyField, Double scholarship) {
        super(firstName, lastName, pesel, height, weight, email);
        this.universityName = universityName;
        this.yearOfStudy = yearOfStudy;
        this.studyField = studyField;
        this.scholarship = scholarship;
    }

    public static StudentDto fromEntity(Student student){
        return StudentDto.builder()
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .pesel(student.getPesel())
                .height(student.getHeight())
                .weight(student.getWeight())
                .email(student.getEmail())
                .universityName(student.getUniversityName())
                .yearOfStudy(student.getYearOfStudy())
                .studyField(student.getStudyField())
                .scholarship(student.getScholarship())
                .build();
    }
}
