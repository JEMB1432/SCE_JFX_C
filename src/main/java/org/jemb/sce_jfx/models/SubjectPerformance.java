package org.jemb.sce_jfx.models;

/**
 * Modelo que representa el rendimiento agregado de un estudiante en una materia
 */
public class SubjectPerformance {
    private String subjectName;
    private String subjectCode;
    private double averageScore;
    private double finalScore;
    private int evaluationCount;
    private int gradedEvaluations;
    private String academicYear;
    private Integer semester;

    public SubjectPerformance() {
    }

    public SubjectPerformance(String subjectName, String subjectCode) {
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
        this.evaluationCount = 0;
        this.gradedEvaluations = 0;
        this.averageScore = 0.0;
        this.finalScore = 0.0;
    }

    // Getters y Setters
    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }

    public int getEvaluationCount() {
        return evaluationCount;
    }

    public void setEvaluationCount(int evaluationCount) {
        this.evaluationCount = evaluationCount;
    }

    public int getGradedEvaluations() {
        return gradedEvaluations;
    }

    public void setGradedEvaluations(int gradedEvaluations) {
        this.gradedEvaluations = gradedEvaluations;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public String getDisplayName() {
        return subjectName + " (" + subjectCode + ")";
    }

    public boolean isComplete() {
        return gradedEvaluations > 0;
    }

    @Override
    public String toString() {
        return "SubjectPerformance{" +
                "subjectName='" + subjectName + '\'' +
                ", averageScore=" + averageScore +
                ", finalScore=" + finalScore +
                ", gradedEvaluations=" + gradedEvaluations +
                '}';
    }
}
