package org.jemb.sce_jfx.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Grade {
    private String id;
    private String enrollmentId;
    private String evaluationTypeId;
    private Double score; // Puede ser null si no se ha calificado
    private String comments;
    private LocalDateTime gradedAt;
    private LocalDateTime createdAt;

    // Referencias a objetos relacionados
    private Enrollment enrollment;
    private EvaluationType evaluationType;

    // Constructores
    public Grade() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    public Grade(String enrollmentId, String evaluationTypeId, Double score) {
        this();
        this.enrollmentId = enrollmentId;
        this.evaluationTypeId = evaluationTypeId;
        this.score = score;
        if (score != null) {
            this.gradedAt = LocalDateTime.now();
        }
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getEvaluationTypeId() { return evaluationTypeId; }
    public void setEvaluationTypeId(String evaluationTypeId) { this.evaluationTypeId = evaluationTypeId; }

    public Double getScore() { return score; }
    public void setScore(Double score) {
        this.score = score;
        if (score != null && this.gradedAt == null) {
            this.gradedAt = LocalDateTime.now();
        }
    }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Enrollment getEnrollment() { return enrollment; }
    public void setEnrollment(Enrollment enrollment) { this.enrollment = enrollment; }

    public EvaluationType getEvaluationType() { return evaluationType; }
    public void setEvaluationType(EvaluationType evaluationType) { this.evaluationType = evaluationType; }

    // Métodos utilitarios
    public boolean isGraded() {
        return score != null;
    }

    public String getScoreString() {
        return score != null ? String.format("%.2f", score) : "No calificado";
    }

    public double getWeightedScore() {
        if (score == null || evaluationType == null) return 0.0;
        return score * (evaluationType.getWeight() / 100.0);
    }

    public String getGradeLetter() {
        if (score == null) return "N/A";

        if (score >= 90) return "A";
        else if (score >= 80) return "B";
        else if (score >= 70) return "C";
        else if (score >= 60) return "D";
        else return "F";
    }

    public boolean isPassing() {
        return score != null && score >= 60.0;
    }

    @Override
    public String toString() {
        return "Grade{" +
                "id='" + id + '\'' +
                ", enrollmentId='" + enrollmentId + '\'' +
                ", evaluationTypeId='" + evaluationTypeId + '\'' +
                ", score=" + score +
                ", isGraded=" + isGraded() +
                '}';
    }
}
