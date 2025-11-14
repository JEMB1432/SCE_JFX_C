package org.jemb.sce_jfx.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Subject {
    private String id;
    private String subjectCode;
    private String name;
    private String description;
    private int credits;
    private int hoursPerWeek;
    private Integer semesterAvailable;
    private String status; // active, inactive
    private LocalDateTime createdAt;

    // Constructores
    public Subject() {
        this.id = UUID.randomUUID().toString();
        this.credits = 3;
        this.hoursPerWeek = 4;
        this.status = "active";
        this.createdAt = LocalDateTime.now();
    }

    public Subject(String subjectCode, String name, int credits) {
        this();
        this.subjectCode = subjectCode;
        this.name = name;
        this.credits = credits;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public int getHoursPerWeek() { return hoursPerWeek; }
    public void setHoursPerWeek(int hoursPerWeek) { this.hoursPerWeek = hoursPerWeek; }

    public Integer getSemesterAvailable() { return semesterAvailable; }
    public void setSemesterAvailable(Integer semesterAvailable) { this.semesterAvailable = semesterAvailable; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Métodos utilitarios
    public boolean isActive() {
        return "active".equals(status);
    }

    public String getCreditsString() {
        return credits + " crédito" + (credits != 1 ? "s" : "");
    }

    @Override
    public String toString() {
        return "Subject{" +
                "id='" + id + '\'' +
                ", subjectCode='" + subjectCode + '\'' +
                ", name='" + name + '\'' +
                ", credits=" + credits +
                ", status='" + status + '\'' +
                '}';
    }
}
