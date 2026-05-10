package com.sis.instructor.validation;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class GradeValidator {
    private final Set<String> validGradeCodes;

    public GradeValidator() {
        this(Set.of("AA", "BA", "BB", "CB", "CC", "DC", "DD", "FD", "FF"));
    }

    public GradeValidator(Set<String> validGradeCodes) {
        this.validGradeCodes = new LinkedHashSet<>();
        for (String gradeCode : validGradeCodes) {
            this.validGradeCodes.add(normalizeLetterGrade(gradeCode));
        }
    }

    public boolean isValidLetterGrade(String letterGrade) {
        return validGradeCodes.contains(normalizeLetterGrade(letterGrade));
    }

    public String normalizeLetterGrade(String letterGrade) {
        if (letterGrade == null) {
            return "";
        }
        return letterGrade.trim().toUpperCase(Locale.ROOT);
    }
}
