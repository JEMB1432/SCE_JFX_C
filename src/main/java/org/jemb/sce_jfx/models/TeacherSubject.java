package org.jemb.sce_jfx.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class TeacherSubject {
    private String id;
    private String teacherId;
    private String subjectId;
    private String academicYear; // Formato: 2024-2025
    private int semester; // 1 o 2
    private String status; // active, inactive, completed
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Referencias a objetos relacionados
    private User teacher;
    private Subject subject;

    // Constructores
    public TeacherSubject() {
        this.id = UUID.randomUUID().toString();
        this.status = "active";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public TeacherSubject(String teacherId, String subjectId, String academicYear, int semester) {
        this();
        this.teacherId = teacherId;
        this.subjectId = subjectId;
        this.academicYear = academicYear;
        this.semester = semester;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public User getTeacher() { return teacher; }
    public void setTeacher(User teacher) { this.teacher = teacher; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    // Métodos utilitarios
    public boolean isActive() {
        return "active".equals(status);
    }

    public boolean isCompleted() {
        return "completed".equals(status);
    }

    public String getSemesterString() {
        return "Semestre " + semester;
    }

    public String getAcademicPeriod() {
        return academicYear + " - Semestre " + semester;
    }

    @Override
    public String toString() {
        return "TeacherSubject{" +
                "id='" + id + '\'' +
                ", teacherId='" + teacherId + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", academicYear='" + academicYear + '\'' +
                ", semester=" + semester +
                ", status='" + status + '\'' +
                '}';
    }
}

