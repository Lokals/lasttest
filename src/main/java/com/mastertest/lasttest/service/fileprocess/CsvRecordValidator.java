package com.mastertest.lasttest.service.fileprocess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvRecordValidator {
    private static final Logger logger = LoggerFactory.getLogger(CsvRecordValidator.class);

    public static boolean isValidPesel(String pesel) {
        logger.debug("Checking pesel: {}", pesel);
        return pesel != null && pesel.matches("\\d{11}");
    }

    public static boolean isValidUniversity(String university) {
        logger.debug("Checking university: {}", university);

        return university != null;
    }

    public static boolean isValidPosition(String position) {
        logger.debug("Checking position: {}", position);

        return position != null;
    }

    public static boolean isValidEmloymentDate(String employmentDate) {
        logger.debug("Checking employmentDate: {}", employmentDate);

        return employmentDate != null;
    }

    public static boolean isValidStudyField(String studyField) {
        logger.debug("Checking studyField: {}", studyField);

        return studyField != null;
    }

    public static boolean isValidEmail(String email) {
        logger.debug("Checking email: {}", email);

        return email != null && email.contains("@") && email.contains(".");
    }

    public static boolean isValidName(String name) {
        logger.debug("Checking name: {}", name);

        return name != null && name.matches("[A-Za-z-]+");
    }

    public static boolean isValidHeight(Double height) {
        logger.debug("Checking height: {}", height);

        return height != null && height >= 1 && height <= 230;
    }

    public static boolean isValidWeight(Double weight) {
        logger.debug("Checking weight: {}", weight);

        return weight != null && weight >= 40 && weight <= 200;
    }

    public static boolean isValidYearOfStudy(Double yearOfStudy) {
        logger.debug("Checking yearOfStudy: {}", yearOfStudy);

        try {
            int year = yearOfStudy.intValue();
            return year >= 1 && year <= 10;
        } catch (NumberFormatException e) {
            logger.error("Invalid format for yearOfStudy: {}", yearOfStudy);
            return false;
        }
    }

    public static boolean isValidScholarship(Double scholarship) {
        logger.debug("Checking scholarship: {}", scholarship);

        return scholarship != null && scholarship >= 0 && scholarship <= 5000;
    }

    public static boolean isValidYearsWorked(Double yearsWorked) {
        try {
            int yearsWorkedint = yearsWorked.intValue();
            logger.debug("Checking yearsWorked: {}", yearsWorkedint);
            return yearsWorkedint >= 0 && yearsWorkedint <= 100;
        } catch (NumberFormatException e) {
            logger.error("Invalid format for yearsWorked: {}", yearsWorked);
            return false;
        }
    }

    public static boolean isValidSalaryAmount(Double salaryAmount) {
        logger.debug("Checking salaryAmount: {}", salaryAmount);

        return salaryAmount != null && salaryAmount >= 0 && salaryAmount <= 100000;
    }

    public static boolean isValidPensionAmount(Double pensionAmount) {
        logger.debug("Checking pensionAmount: {}", pensionAmount);

        return pensionAmount != null && pensionAmount >= 0 && pensionAmount <= 10000;
    }
}
