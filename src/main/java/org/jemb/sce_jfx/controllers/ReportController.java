package org.jemb.sce_jfx.controllers;

import org.jemb.sce_jfx.dao.EnrollmentDAO;
import org.jemb.sce_jfx.dao.GradeDAO;
import org.jemb.sce_jfx.dao.StudentDAO;
import org.jemb.sce_jfx.models.Enrollment;
import org.jemb.sce_jfx.models.Grade;
import org.jemb.sce_jfx.models.Student;
import org.jemb.sce_jfx.models.StudentSummary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReportController {
    private final StudentDAO studentDAO;
    private final EnrollmentDAO enrollmentDAO;
    private final GradeDAO gradeDAO;

    public ReportController() {
        this.studentDAO = new StudentDAO();
        this.enrollmentDAO = new EnrollmentDAO();
        this.gradeDAO = new GradeDAO();
    }

    // Generar resumen de estudiante
    public StudentSummary generateStudentSummary(String studentId) {
        Optional<Student> studentOpt = studentDAO.findById(studentId);
        if (studentOpt.isEmpty()) {
            throw new IllegalArgumentException("El estudiante no existe");
        }

        Student student = studentOpt.get();
        List<Enrollment> enrollments = enrollmentDAO.findByStudentId(studentId);

        StudentSummary summary = new StudentSummary(student, enrollments);
        
        // Calcular GPA y créditos
        calculateGPAAndCredits(summary, enrollments);

        return summary;
    }

    // Calcular GPA y créditos totales
    private void calculateGPAAndCredits(StudentSummary summary, List<Enrollment> enrollments) {
        double totalWeightedScore = 0.0;
        int totalCredits = 0;
        int completedCredits = 0;

        for (Enrollment enrollment : enrollments) {
            if (enrollment.isCompleted()) {
                double finalGrade = gradeDAO.calculateFinalGrade(enrollment.getId());
                
                // Obtener créditos de la materia
                int credits = 0;
                if (enrollment.getSubject() != null) {
                    credits = enrollment.getSubject().getCredits();
                } else {
                    // Si no está cargada la relación, obtenerla
                    Optional<org.jemb.sce_jfx.models.Subject> subjectOpt = 
                        new org.jemb.sce_jfx.dao.SubjectDAO().findById(enrollment.getSubjectId());
                    if (subjectOpt.isPresent()) {
                        credits = subjectOpt.get().getCredits();
                    }
                }

                totalWeightedScore += finalGrade * credits;
                totalCredits += credits;
                completedCredits += credits;
            } else if (enrollment.isEnrolled()) {
                // Contar créditos de materias inscritas
                int credits = 0;
                if (enrollment.getSubject() != null) {
                    credits = enrollment.getSubject().getCredits();
                } else {
                    Optional<org.jemb.sce_jfx.models.Subject> subjectOpt = 
                        new org.jemb.sce_jfx.dao.SubjectDAO().findById(enrollment.getSubjectId());
                    if (subjectOpt.isPresent()) {
                        credits = subjectOpt.get().getCredits();
                    }
                }
                totalCredits += credits;
            }
        }

        // Calcular GPA
        if (completedCredits > 0) {
            summary.setOverallGPA(totalWeightedScore / completedCredits);
        } else {
            summary.setOverallGPA(0.0);
        }

        summary.setTotalCredits(totalCredits);
    }

    // Obtener calificaciones de un estudiante para gráficos
    public Map<String, List<GradeData>> getStudentGradesForChart(String studentId) {
        Optional<Student> studentOpt = studentDAO.findById(studentId);
        if (studentOpt.isEmpty()) {
            throw new IllegalArgumentException("El estudiante no existe");
        }

        List<Enrollment> enrollments = enrollmentDAO.findByStudentId(studentId);
        Map<String, List<GradeData>> gradesBySubject = new HashMap<>();

        for (Enrollment enrollment : enrollments) {
            String subjectName = enrollment.getSubject() != null 
                ? enrollment.getSubject().getName() 
                : "Materia " + enrollment.getSubjectId();
            
            List<Grade> grades = gradeDAO.findGradedByEnrollmentId(enrollment.getId());
            List<GradeData> gradeDataList = new ArrayList<>();

            for (Grade grade : grades) {
                GradeData gradeData = new GradeData();
                gradeData.evaluationName = grade.getEvaluationType() != null 
                    ? grade.getEvaluationType().getName() 
                    : "Evaluación";
                gradeData.score = grade.getScore();
                gradeData.maxScore = grade.getEvaluationType() != null 
                    ? grade.getEvaluationType().getMaxScore() 
                    : 100.0;
                gradeData.weight = grade.getEvaluationType() != null 
                    ? grade.getEvaluationType().getWeight() 
                    : 0.0;
                gradeDataList.add(gradeData);
            }

            if (!gradeDataList.isEmpty()) {
                gradesBySubject.put(subjectName, gradeDataList);
            }
        }

        return gradesBySubject;
    }

    // Obtener datos de rendimiento por período
    public Map<String, Double> getPerformanceByPeriod(String studentId) {
        Optional<Student> studentOpt = studentDAO.findById(studentId);
        if (studentOpt.isEmpty()) {
            throw new IllegalArgumentException("El estudiante no existe");
        }

        List<Enrollment> enrollments = enrollmentDAO.findByStudentId(studentId);
        Map<String, Double> performanceByPeriod = new HashMap<>();

        for (Enrollment enrollment : enrollments) {
            if (enrollment.isCompleted()) {
                String period = enrollment.getAcademicPeriod();
                double finalGrade = gradeDAO.calculateFinalGrade(enrollment.getId());
                performanceByPeriod.put(period, finalGrade);
            }
        }

        return performanceByPeriod;
    }

    // Clase interna para datos de calificación
    public static class GradeData {
        public String evaluationName;
        public Double score;
        public Double maxScore;
        public Double weight;
    }

    // Obtener estadísticas generales
    public Map<String, Object> getGeneralStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalStudents", studentDAO.count());
        stats.put("activeStudents", studentDAO.findByStatus("active").size());
        stats.put("totalEnrollments", enrollmentDAO.findAll().size());
        stats.put("activeEnrollments", enrollmentDAO.findByStatus("enrolled").size());
        stats.put("completedEnrollments", enrollmentDAO.findByStatus("completed").size());
        
        return stats;
    }
}

