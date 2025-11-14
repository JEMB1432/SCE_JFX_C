package org.jemb.sce_jfx.controllers;

import org.jemb.sce_jfx.dao.EvaluationTypeDAO;
import org.jemb.sce_jfx.dao.SubjectDAO;
import org.jemb.sce_jfx.models.EvaluationType;
import org.jemb.sce_jfx.models.Subject;

import java.util.List;
import java.util.Optional;

public class EvaluationTypeController {
    private final EvaluationTypeDAO evaluationTypeDAO;
    private final SubjectDAO subjectDAO;

    public EvaluationTypeController() {
        this.evaluationTypeDAO = new EvaluationTypeDAO();
        this.subjectDAO = new SubjectDAO();
    }

    // Crear tipo de evaluación
    public EvaluationType createEvaluationType(String subjectId, String name, double weight) {
        // Validar materia
        Optional<Subject> subjectOpt = subjectDAO.findById(subjectId);
        if (subjectOpt.isEmpty()) {
            throw new IllegalArgumentException("La materia no existe");
        }

        // Validar peso
        if (weight < 0 || weight > 100) {
            throw new IllegalArgumentException("El peso debe estar entre 0 y 100");
        }

        // Verificar que el peso total no exceda 100%
        double currentTotal = evaluationTypeDAO.getTotalWeightBySubjectId(subjectId);
        if (currentTotal + weight > 100) {
            throw new IllegalArgumentException(
                String.format("El peso total excedería 100%%. Peso actual: %.2f%%, nuevo peso: %.2f%%", 
                    currentTotal, weight)
            );
        }

        EvaluationType evaluationType = new EvaluationType(subjectId, name, weight);
        EvaluationType saved = evaluationTypeDAO.save(evaluationType);
        
        if (saved == null) {
            throw new RuntimeException("Error al crear el tipo de evaluación");
        }
        
        return saved;
    }

    // Obtener tipo de evaluación por ID
    public Optional<EvaluationType> getEvaluationTypeById(String id) {
        return evaluationTypeDAO.findById(id);
    }

    // Obtener todos los tipos de evaluación
    public List<EvaluationType> getAllEvaluationTypes() {
        return evaluationTypeDAO.findAll();
    }

    // Obtener tipos de evaluación por materia
    public List<EvaluationType> getEvaluationTypesBySubject(String subjectId) {
        if (!subjectDAO.existsById(subjectId)) {
            throw new IllegalArgumentException("La materia no existe");
        }
        return evaluationTypeDAO.findBySubjectId(subjectId);
    }

    // Obtener examen final de una materia
    public Optional<EvaluationType> getFinalExamBySubject(String subjectId) {
        if (!subjectDAO.existsById(subjectId)) {
            throw new IllegalArgumentException("La materia no existe");
        }
        return evaluationTypeDAO.findFinalExamBySubjectId(subjectId);
    }

    // Obtener peso total de una materia
    public double getTotalWeightBySubject(String subjectId) {
        if (!subjectDAO.existsById(subjectId)) {
            throw new IllegalArgumentException("La materia no existe");
        }
        return evaluationTypeDAO.getTotalWeightBySubjectId(subjectId);
    }

    // Actualizar tipo de evaluación
    public EvaluationType updateEvaluationType(EvaluationType evaluationType) {
        // Validar que el tipo de evaluación exista
        Optional<EvaluationType> existingOpt = evaluationTypeDAO.findById(evaluationType.getId());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("El tipo de evaluación no existe");
        }

        // Validar materia
        if (!subjectDAO.existsById(evaluationType.getSubjectId())) {
            throw new IllegalArgumentException("La materia no existe");
        }

        // Validar peso
        if (evaluationType.getWeight() < 0 || evaluationType.getWeight() > 100) {
            throw new IllegalArgumentException("El peso debe estar entre 0 y 100");
        }

        // Verificar que el peso total no exceda 100% (excluyendo el actual)
        EvaluationType existing = existingOpt.get();
        double currentTotal = evaluationTypeDAO.getTotalWeightBySubjectId(evaluationType.getSubjectId());
        double newTotal = currentTotal - existing.getWeight() + evaluationType.getWeight();
        
        if (newTotal > 100) {
            throw new IllegalArgumentException(
                String.format("El peso total excedería 100%%. Peso total actual: %.2f%%, nuevo total: %.2f%%", 
                    currentTotal, newTotal)
            );
        }

        // Validar puntuación máxima
        if (evaluationType.getMaxScore() <= 0) {
            throw new IllegalArgumentException("La puntuación máxima debe ser mayor a 0");
        }

        EvaluationType updated = evaluationTypeDAO.update(evaluationType);
        
        if (updated == null) {
            throw new RuntimeException("Error al actualizar el tipo de evaluación");
        }
        
        return updated;
    }

    // Eliminar tipo de evaluación
    public void deleteEvaluationType(String id) {
        if (!evaluationTypeDAO.findById(id).isPresent()) {
            throw new IllegalArgumentException("El tipo de evaluación no existe");
        }
        
        evaluationTypeDAO.delete(id);
    }
}

