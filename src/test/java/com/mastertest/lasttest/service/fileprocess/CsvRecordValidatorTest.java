package com.mastertest.lasttest.service.fileprocess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CsvRecordValidatorTest {


    @Test
    void isValidPesel_ValidPesel_ReturnsTrue() {
        assertTrue(CsvRecordValidator.isValidPesel("12345678901"));
    }

    @Test
    void isValidPesel_InvalidPesel_ReturnsFalse() {
        assertFalse(CsvRecordValidator.isValidPesel("12345"));
    }

    @Test
    void isValidUniversity_ValidUniversity_ReturnsTrue() {
        assertTrue(CsvRecordValidator.isValidUniversity("University of Test"));
    }

    @Test
    void isValidUniversity_NullUniversity_ReturnsFalse() {
        assertFalse(CsvRecordValidator.isValidUniversity(null));
    }

    @Test
    void isValidPosition_ValidPosition_ReturnsTrue() {
        assertTrue(CsvRecordValidator.isValidPosition("Manager"));
    }

    @Test
    void isValidPosition_NullPosition_ReturnsFalse() {
        assertFalse(CsvRecordValidator.isValidPosition(null));
    }

    @Test
    void isValidEmloymentDate_ValidDate_ReturnsTrue() {
        assertTrue(CsvRecordValidator.isValidEmloymentDate("2022-01-01"));
    }

    @Test
    void isValidEmloymentDate_NullDate_ReturnsFalse() {
        assertFalse(CsvRecordValidator.isValidEmloymentDate(null));
    }

    @Test
    void isValidStudyField_ValidField_ReturnsTrue() {
        assertTrue(CsvRecordValidator.isValidStudyField("Computer Science"));
    }

    @Test
    void isValidStudyField_NullField_ReturnsFalse() {
        assertFalse(CsvRecordValidator.isValidStudyField(null));
    }

    @Test
    void isValidEmail_ValidEmail_ReturnsTrue() {
        assertTrue(CsvRecordValidator.isValidEmail("test@example.com"));
    }

    @Test
    void isValidEmail_InvalidEmail_ReturnsFalse() {
        assertFalse(CsvRecordValidator.isValidEmail("testexample.com"));
    }

    @Test
    void isValidName_ValidName_ReturnsTrue() {
        assertTrue(CsvRecordValidator.isValidName("John"));
    }

    @Test
    void isValidName_InvalidName_ReturnsFalse() {
        assertFalse(CsvRecordValidator.isValidName("John123"));
    }

    @Test
    void isValidHeight_ValidHeight_ReturnsTrue() {
        assertTrue(CsvRecordValidator.isValidHeight(170.0));
    }

    @Test
    void isValidHeight_InvalidHeight_ReturnsFalse() {
        assertFalse(CsvRecordValidator.isValidHeight(250.0));
    }

    @Test
    void isValidWeight_ValidWeight_ReturnsTrue() {
        assertTrue(CsvRecordValidator.isValidWeight(70.0));
    }

    @Test
    void isValidWeight_InvalidWeight_ReturnsFalse() {
        assertFalse(CsvRecordValidator.isValidWeight(35.0));
    }

    @Test
    void isValidYearOfStudy_ValidYear_ReturnsTrue() {
        assertTrue(CsvRecordValidator.isValidYearOfStudy(3.0));
    }

    @Test
    void isValidYearOfStudy_InvalidYear_ReturnsFalse() {
        assertFalse(CsvRecordValidator.isValidYearOfStudy(0.0));
    }

    @Test
    void isValidScholarship_ValidScholarship_ReturnsTrue() {
        assertTrue(CsvRecordValidator.isValidScholarship(1000.0));
    }

    @Test
    void isValidScholarship_InvalidScholarship_ReturnsFalse() {
        assertFalse(CsvRecordValidator.isValidScholarship(-100.0));
    }

    @Test
    void isValidYearsWorked_ValidYears_ReturnsTrue() {
        assertTrue(CsvRecordValidator.isValidYearsWorked(10.0));
    }

    @Test
    void isValidYearsWorked_InvalidYears_ReturnsFalse() {
        assertFalse(CsvRecordValidator.isValidYearsWorked(-1.0));
    }

    @Test
    void isValidSalaryAmount_ValidSalary_ReturnsTrue() {
        assertTrue(CsvRecordValidator.isValidSalaryAmount(30000.0));
    }

    @Test
    void isValidSalaryAmount_InvalidSalary_ReturnsFalse() {
        assertFalse(CsvRecordValidator.isValidSalaryAmount(-5000.0));
    }

    @Test
    void isValidPensionAmount_ValidPension_ReturnsTrue() {
        assertTrue(CsvRecordValidator.isValidPensionAmount(2000.0));
    }

    @Test
    void isValidPensionAmount_InvalidPension_ReturnsFalse() {
        assertFalse(CsvRecordValidator.isValidPensionAmount(15000.0));
    }
}

