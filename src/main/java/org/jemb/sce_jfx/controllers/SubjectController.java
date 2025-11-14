package org.jemb.sce_jfx.controllers;

import org.jemb.sce_jfx.dao.SubjectDAO;
import org.jemb.sce_jfx.models.Subject;

import java.util.List;
import java.util.Optional;

public class SubjectController {
    private final SubjectDAO subjectDAO;

    public SubjectController() {
        this.subjectDAO = new SubjectDAO();
    }

    // Crear nueva materia
    public Subject createSubject(String subjectCode, String name, int credits) {
        // Validar que el código de materia no exista
        if (subjectDAO.findBySubjectCode(subjectCode).isPresent()) {
            throw new IllegalArgumentException("El código de materia ya existe: " + subjectCode);
        }

        // Validar créditos
        if (credits <= 0) {
            throw new IllegalArgumentException("Los créditos deben ser mayores a 0");
        }

        Subject subject = new Subject(subjectCode, name, credits);
        Subject saved = subjectDAO.save(subject);
        
        if (saved == null) {
            throw new RuntimeException("Error al crear la materia");
        }
        
        return saved;
    }

    // Obtener materia por ID
    public Optional<Subject> getSubjectById(String id) {
        return subjectDAO.findById(id);
    }

    // Obtener materia por código
    public Optional<Subject> getSubjectByCode(String subjectCode) {
        return subjectDAO.findBySubjectCode(subjectCode);
    }

    // Obtener todas las materias
    public List<Subject> getAllSubjects() {
        return subjectDAO.findAll();
    }

    // Obtener materias por estado
    public List<Subject> getSubjectsByStatus(String status) {
        return subjectDAO.findByStatus(status);
    }

    // Obtener solo materias activas
    public List<Subject> getActiveSubjects() {
        return subjectDAO.findByStatus("active");
    }

    // Obtener materias por semestre disponible
    public List<Subject> getSubjectsBySemester(int semester) {
        if (semester < 1 || semester > 12) {
            throw new IllegalArgumentException("El semestre debe estar entre 1 y 12");
        }
        return subjectDAO.findBySemesterAvailable(semester);
    }

    // Actualizar materia
    public Subject updateSubject(Subject subject) {
        // Validar que la materia exista
        if (!subjectDAO.existsById(subject.getId())) {
            throw new IllegalArgumentException("La materia no existe");
        }

        // Validar código único si cambió
        Optional<Subject> existingByCode = subjectDAO.findBySubjectCode(subject.getSubjectCode());
        if (existingByCode.isPresent() && !existingByCode.get().getId().equals(subject.getId())) {
            throw new IllegalArgumentException("El código de materia ya está en uso: " + subject.getSubjectCode());
        }

        // Validar créditos
        if (subject.getCredits() <= 0) {
            throw new IllegalArgumentException("Los créditos deben ser mayores a 0");
        }

        // Validar horas por semana
        if (subject.getHoursPerWeek() <= 0) {
            throw new IllegalArgumentException("Las horas por semana deben ser mayores a 0");
        }

        // Validar semestre disponible si se especifica
        if (subject.getSemesterAvailable() != null) {
            if (subject.getSemesterAvailable() < 1 || subject.getSemesterAvailable() > 12) {
                throw new IllegalArgumentException("El semestre disponible debe estar entre 1 y 12");
            }
        }

        Subject updated = subjectDAO.update(subject);
        
        if (updated == null) {
            throw new RuntimeException("Error al actualizar la materia");
        }
        
        return updated;
    }

    // Eliminar materia
    public void deleteSubject(String id) {
        if (!subjectDAO.existsById(id)) {
            throw new IllegalArgumentException("La materia no existe");
        }
        
        subjectDAO.delete(id);
    }

    // Cambiar estado de la materia
    public Subject changeSubjectStatus(String id, String status) {
        Optional<Subject> subjectOpt = subjectDAO.findById(id);
        if (subjectOpt.isEmpty()) {
            throw new IllegalArgumentException("La materia no existe");
        }

        Subject subject = subjectOpt.get();
        subject.setStatus(status);
        
        return updateSubject(subject);
    }

    // Activar materia
    public Subject activateSubject(String id) {
        return changeSubjectStatus(id, "active");
    }

    // Desactivar materia
    public Subject deactivateSubject(String id) {
        return changeSubjectStatus(id, "inactive");
    }

    // Contar materias
    public long countSubjects() {
        return subjectDAO.count();
    }

    // Verificar si existe materia
    public boolean subjectExists(String id) {
        return subjectDAO.existsById(id);
    }
}

