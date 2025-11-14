package org.jemb.sce_jfx.models;

import java.util.List;

public class StudentSummary {
    private Student student;
    private List<Enrollment> enrollments;
    private double overallGPA;
    private int completedSubjects;
    private int enrolledSubjects;
    private int totalCredits;

    // Constructores
    public StudentSummary() {}

    public StudentSummary(Student student, List<Enrollment> enrollments) {
        this.student = student;
        this.enrollments = enrollments;
        calculateSummary();
    }

    // Getters y Setters
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public List<Enrollment> getEnrollments() { return enrollments; }
    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments;
        calculateSummary();
    }

    public double getOverallGPA() { return overallGPA; }
    public void setOverallGPA(double overallGPA) { this.overallGPA = overallGPA; }

    public int getCompletedSubjects() { return completedSubjects; }
    public void setCompletedSubjects(int completedSubjects) { this.completedSubjects = completedSubjects; }

    public int getEnrolledSubjects() { return enrolledSubjects; }
    public void setEnrolledSubjects(int enrolledSubjects) { this.enrolledSubjects = enrolledSubjects; }

    public int getTotalCredits() { return totalCredits; }
    public void setTotalCredits(int totalCredits) { this.totalCredits = totalCredits; }

    // Métodos utilitarios
    private void calculateSummary() {
        if (enrollments == null) return;

        this.completedSubjects = (int) enrollments.stream()
                .filter(Enrollment::isCompleted)
                .count();

        this.enrolledSubjects = (int) enrollments.stream()
                .filter(Enrollment::isEnrolled)
                .count();

        // Calcular GPA (aquí necesitarías las calificaciones reales)
        // Esto es un placeholder - la implementación real dependerá de tu lógica de negocio
        this.overallGPA = 0.0;
        this.totalCredits = 0;
    }

    public String getGPAString() {
        return String.format("%.2f", overallGPA);
    }

    public String getCompletionRate() {
        if (enrolledSubjects == 0) return "0%";
        double rate = (completedSubjects * 100.0) / enrolledSubjects;
        return String.format("%.1f%%", rate);
    }

    @Override
    public String toString() {
        return "StudentSummary{" +
                "student=" + student.getFullName() +
                ", overallGPA=" + overallGPA +
                ", completedSubjects=" + completedSubjects +
                ", enrolledSubjects=" + enrolledSubjects +
                ", totalCredits=" + totalCredits +
                '}';
    }
}
