package org.jemb.sce_jfx.models;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Enrollment {
    private String id;
    private String studentId;
    private String subjectId;
    private String academicYear; // Formato: 2024-2025
    private int semester; // 1 o 2
    private LocalDate enrollmentDate;
    private String status; // enrolled, completed, dropped
    private LocalDateTime createdAt;

    // Referencias a objetos relacionados (para cuando se carguen desde BD)
    private Student student;
    private Subject subject;

    // Constructores
    public Enrollment() {
        this.id = UUID.randomUUID().toString();
        this.enrollmentDate = LocalDate.now();
        this.status = "enrolled";
        this.createdAt = LocalDateTime.now();
    }

    public Enrollment(String studentId, String subjectId, String academicYear, int semester) {
        this();
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.academicYear = academicYear;
        this.semester = semester;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    // Métodos utilitarios
    public boolean isEnrolled() {
        return "enrolled".equals(status);
    }

    public boolean isCompleted() {
        return "completed".equals(status);
    }

    public boolean isDropped() {
        return "dropped".equals(status);
    }

    public String getSemesterString() {
        return "Semestre " + semester;
    }

    public String getAcademicPeriod() {
        return academicYear + " - Semestre " + semester;
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "id='" + id + '\'' +
                ", studentId='" + studentId + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", academicYear='" + academicYear + '\'' +
                ", semester=" + semester +
                ", status='" + status + '\'' +
                '}';
    }
}
