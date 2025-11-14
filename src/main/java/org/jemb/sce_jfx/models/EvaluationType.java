package org.jemb.sce_jfx.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class EvaluationType {
    private String id;
    private String subjectId;
    private String name;
    private String description;
    private double weight; // Porcentaje que vale (0-100)
    private double maxScore; // Puntuación máxima (por defecto 100)
    private Integer evaluationOrder; // Orden de evaluación
    private boolean isFinalExam;
    private LocalDateTime createdAt;

    // Referencia al sujeto (cuando se cargue desde BD)
    private Subject subject;

    // Constructores
    public EvaluationType() {
        this.id = UUID.randomUUID().toString();
        this.maxScore = 100.0;
        this.isFinalExam = false;
        this.createdAt = LocalDateTime.now();
    }

    public EvaluationType(String subjectId, String name, double weight) {
        this();
        this.subjectId = subjectId;
        this.name = name;
        this.weight = weight;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getMaxScore() { return maxScore; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }

    public Integer getEvaluationOrder() { return evaluationOrder; }
    public void setEvaluationOrder(Integer evaluationOrder) { this.evaluationOrder = evaluationOrder; }

    public boolean isFinalExam() { return isFinalExam; }
    public void setFinalExam(boolean finalExam) { isFinalExam = finalExam; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    // Métodos utilitarios
    public String getWeightPercentage() {
        return String.format("%.1f%%", weight);
    }

    public double getWeightDecimal() {
        return weight / 100.0;
    }

    @Override
    public String toString() {
        return "EvaluationType{" +
                "id='" + id + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", name='" + name + '\'' +
                ", weight=" + weight +
                ", isFinalExam=" + isFinalExam +
                '}';
    }
}
