package org.jemb.sce_jfx.controllers;

import org.jemb.sce_jfx.dao.EnrollmentDAO;
import org.jemb.sce_jfx.dao.EvaluationTypeDAO;
import org.jemb.sce_jfx.dao.GradeDAO;
import org.jemb.sce_jfx.models.Enrollment;
import org.jemb.sce_jfx.models.EvaluationType;
import org.jemb.sce_jfx.models.Grade;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class GradeController {
    private final GradeDAO gradeDAO;
    private final EnrollmentDAO enrollmentDAO;
    private final EvaluationTypeDAO evaluationTypeDAO;

    public GradeController() {
        this.gradeDAO = new GradeDAO();
        this.enrollmentDAO = new EnrollmentDAO();
        this.evaluationTypeDAO = new EvaluationTypeDAO();
    }

    // Registrar calificación
    public Grade recordGrade(String enrollmentId, String evaluationTypeId, Double score, String comments) {
        // Validar inscripción
        Optional<Enrollment> enrollmentOpt = enrollmentDAO.findById(enrollmentId);
        if (enrollmentOpt.isEmpty()) {
            throw new IllegalArgumentException("La inscripción no existe");
        }

        Enrollment enrollment = enrollmentOpt.get();
        if (!enrollment.isEnrolled()) {
            throw new IllegalArgumentException("El estudiante no está inscrito en esta materia");
        }

        // Validar tipo de evaluación
        Optional<EvaluationType> evaluationTypeOpt = evaluationTypeDAO.findById(evaluationTypeId);
        if (evaluationTypeOpt.isEmpty()) {
            throw new IllegalArgumentException("El tipo de evaluación no existe");
        }

        EvaluationType evaluationType = evaluationTypeOpt.get();
        
        // Validar que el tipo de evaluación corresponda a la materia de la inscripción
        if (!evaluationType.getSubjectId().equals(enrollment.getSubjectId())) {
            throw new IllegalArgumentException("El tipo de evaluación no corresponde a la materia de la inscripción");
        }

        // Validar puntuación
        if (score != null) {
            if (score < 0 || score > evaluationType.getMaxScore()) {
                throw new IllegalArgumentException(
                    String.format("La puntuación debe estar entre 0 y %.2f", evaluationType.getMaxScore())
                );
            }
        }

        // Verificar si ya existe una calificación
        Optional<Grade> existing = gradeDAO.findByEnrollmentAndEvaluationType(enrollmentId, evaluationTypeId);
        
        Grade grade;
        if (existing.isPresent()) {
            // Actualizar calificación existente
            grade = existing.get();
            grade.setScore(score);
            grade.setComments(comments);
            if (score != null) {
                grade.setGradedAt(LocalDateTime.now());
            }
            grade = gradeDAO.update(grade);
        } else {
            // Crear nueva calificación
            grade = new Grade(enrollmentId, evaluationTypeId, score);
            grade.setComments(comments);
            if (score != null) {
                grade.setGradedAt(LocalDateTime.now());
            }
            grade = gradeDAO.save(grade);
        }

        if (grade == null) {
            throw new RuntimeException("Error al registrar la calificación");
        }

        return grade;
    }

    // Obtener calificación por ID
    public Optional<Grade> getGradeById(String id) {
        return gradeDAO.findById(id);
    }

    // Obtener todas las calificaciones
    public List<Grade> getAllGrades() {
        return gradeDAO.findAll();
    }

    // Obtener calificaciones de una inscripción
    public List<Grade> getGradesByEnrollment(String enrollmentId) {
        if (!enrollmentDAO.findById(enrollmentId).isPresent()) {
            throw new IllegalArgumentException("La inscripción no existe");
        }
        return gradeDAO.findByEnrollmentId(enrollmentId);
    }

    // Obtener calificaciones calificadas de una inscripción
    public List<Grade> getGradedGradesByEnrollment(String enrollmentId) {
        if (!enrollmentDAO.findById(enrollmentId).isPresent()) {
            throw new IllegalArgumentException("La inscripción no existe");
        }
        return gradeDAO.findGradedByEnrollmentId(enrollmentId);
    }

    // Obtener calificaciones por tipo de evaluación
    public List<Grade> getGradesByEvaluationType(String evaluationTypeId) {
        if (!evaluationTypeDAO.findById(evaluationTypeId).isPresent()) {
            throw new IllegalArgumentException("El tipo de evaluación no existe");
        }
        return gradeDAO.findByEvaluationTypeId(evaluationTypeId);
    }

    // Calcular calificación final de una inscripción
    public double calculateFinalGrade(String enrollmentId) {
        if (!enrollmentDAO.findById(enrollmentId).isPresent()) {
            throw new IllegalArgumentException("La inscripción no existe");
        }
        return gradeDAO.calculateFinalGrade(enrollmentId);
    }

    // Actualizar calificación
    public Grade updateGrade(Grade grade) {
        // Validar que la calificación exista
        Optional<Grade> existingOpt = gradeDAO.findById(grade.getId());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("La calificación no existe");
        }

        // Validar inscripción
        if (!enrollmentDAO.findById(grade.getEnrollmentId()).isPresent()) {
            throw new IllegalArgumentException("La inscripción no existe");
        }

        // Validar tipo de evaluación
        Optional<EvaluationType> evaluationTypeOpt = evaluationTypeDAO.findById(grade.getEvaluationTypeId());
        if (evaluationTypeOpt.isEmpty()) {
            throw new IllegalArgumentException("El tipo de evaluación no existe");
        }

        EvaluationType evaluationType = evaluationTypeOpt.get();

        // Validar puntuación
        if (grade.getScore() != null) {
            if (grade.getScore() < 0 || grade.getScore() > evaluationType.getMaxScore()) {
                throw new IllegalArgumentException(
                    String.format("La puntuación debe estar entre 0 y %.2f", evaluationType.getMaxScore())
                );
            }
            if (grade.getGradedAt() == null) {
                grade.setGradedAt(LocalDateTime.now());
            }
        }

        Grade updated = gradeDAO.update(grade);
        
        if (updated == null) {
            throw new RuntimeException("Error al actualizar la calificación");
        }
        
        return updated;
    }

    // Eliminar calificación
    public void deleteGrade(String id) {
        if (!gradeDAO.findById(id).isPresent()) {
            throw new IllegalArgumentException("La calificación no existe");
        }
        
        gradeDAO.delete(id);
    }

    // Obtener calificación específica
    public Optional<Grade> getGradeByEnrollmentAndEvaluationType(String enrollmentId, String evaluationTypeId) {
        return gradeDAO.findByEnrollmentAndEvaluationType(enrollmentId, evaluationTypeId);
    }
}

