package org.jemb.sce_jfx.controllers;

import org.jemb.sce_jfx.dao.TeacherSubjectDAO;
import org.jemb.sce_jfx.dao.UserDAO;
import org.jemb.sce_jfx.models.TeacherSubject;
import org.jemb.sce_jfx.models.User;

import java.util.List;
import java.util.Optional;

public class TeacherSubjectController {
    private final TeacherSubjectDAO teacherSubjectDAO;
    private final UserDAO userDAO;

    public TeacherSubjectController() {
        this.teacherSubjectDAO = new TeacherSubjectDAO();
        this.userDAO = new UserDAO();
    }

    // Crear nueva asignación profesor-materia
    public TeacherSubject createAssignment(String teacherId, String subjectId, String academicYear, int semester) {
        // Validar que el usuario sea un profesor
        Optional<User> userOpt = userDAO.findById(teacherId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("El usuario no existe");
        }

        User user = userOpt.get();
        if (!user.isTeacher()) {
            throw new IllegalArgumentException("El usuario debe tener el rol de profesor");
        }

        // Validar que no exista una asignación duplicada
        Optional<TeacherSubject> existing = teacherSubjectDAO.findByTeacherAndSubjectAndPeriod(
                teacherId, subjectId, academicYear, semester);

        if (existing.isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe una asignación para este profesor, materia y periodo académico");
        }

        // Validar semestre
        if (semester < 1) {
            throw new IllegalArgumentException("El semestre debe ser un número positivo mayor a 0");
        }

        // Validar año académico (formato: YYYY-YYYY)
        if (!isValidAcademicYear(academicYear)) {
            throw new IllegalArgumentException("Formato de año académico inválido. Use el formato: 2024-2025");
        }

        TeacherSubject assignment = new TeacherSubject(teacherId, subjectId, academicYear, semester);
        TeacherSubject saved = teacherSubjectDAO.save(assignment);

        if (saved == null) {
            throw new RuntimeException("Error al crear la asignación");
        }

        return saved;
    }

    // Obtener asignación por ID
    public Optional<TeacherSubject> getAssignmentById(String id) {
        return teacherSubjectDAO.findById(id);
    }

    // Obtener todas las asignaciones
    public List<TeacherSubject> getAllAssignments() {
        return teacherSubjectDAO.findAll();
    }

    // Obtener asignaciones por profesor
    public List<TeacherSubject> getAssignmentsByTeacher(String teacherId) {
        return teacherSubjectDAO.findByTeacherId(teacherId);
    }

    // Obtener asignaciones por materia
    public List<TeacherSubject> getAssignmentsBySubject(String subjectId) {
        return teacherSubjectDAO.findBySubjectId(subjectId);
    }

    // Obtener asignaciones por periodo académico
    public List<TeacherSubject> getAssignmentsByPeriod(String academicYear, int semester) {
        return teacherSubjectDAO.findByAcademicYearAndSemester(academicYear, semester);
    }

    // Obtener asignaciones activas de un profesor
    public List<TeacherSubject> getActiveAssignmentsByTeacher(String teacherId) {
        return teacherSubjectDAO.findByTeacherIdAndStatus(teacherId, "active");
    }

    // Actualizar asignación
    public TeacherSubject updateAssignment(TeacherSubject assignment) {
        // Validar que la asignación exista
        if (!teacherSubjectDAO.existsById(assignment.getId())) {
            throw new IllegalArgumentException("La asignación no existe");
        }

        // Validar que el profesor existe y tiene el rol correcto
        Optional<User> userOpt = userDAO.findById(assignment.getTeacherId());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("El profesor no existe");
        }

        if (!userOpt.get().isTeacher()) {
            throw new IllegalArgumentException("El usuario debe tener el rol de profesor");
        }

        // Validar semestre
        if (assignment.getSemester() < 1) {
            throw new IllegalArgumentException("El semestre debe ser un número positivo mayor a 0");
        }

        // Validar año académico
        if (!isValidAcademicYear(assignment.getAcademicYear())) {
            throw new IllegalArgumentException("Formato de año académico inválido. Use el formato: 2024-2025");
        }

        // Validar duplicados (excepto la misma asignación)
        Optional<TeacherSubject> existing = teacherSubjectDAO.findByTeacherAndSubjectAndPeriod(
                assignment.getTeacherId(),
                assignment.getSubjectId(),
                assignment.getAcademicYear(),
                assignment.getSemester());

        if (existing.isPresent() && !existing.get().getId().equals(assignment.getId())) {
            throw new IllegalArgumentException(
                    "Ya existe otra asignación para este profesor, materia y periodo académico");
        }

        assignment.setUpdatedAt(java.time.LocalDateTime.now());
        TeacherSubject updated = teacherSubjectDAO.update(assignment);

        if (updated == null) {
            throw new RuntimeException("Error al actualizar la asignación");
        }

        return updated;
    }

    // Eliminar asignación
    public void deleteAssignment(String id) {
        if (!teacherSubjectDAO.existsById(id)) {
            throw new IllegalArgumentException("La asignación no existe");
        }

        teacherSubjectDAO.delete(id);
    }

    // Cambiar estado de la asignación
    public TeacherSubject changeAssignmentStatus(String id, String status) {
        Optional<TeacherSubject> assignmentOpt = teacherSubjectDAO.findById(id);
        if (assignmentOpt.isEmpty()) {
            throw new IllegalArgumentException("La asignación no existe");
        }

        TeacherSubject assignment = assignmentOpt.get();
        assignment.setStatus(status);
        assignment.setUpdatedAt(java.time.LocalDateTime.now());

        return updateAssignment(assignment);
    }

    // Activar asignación
    public TeacherSubject activateAssignment(String id) {
        return changeAssignmentStatus(id, "active");
    }

    // Inactivar asignación
    public TeacherSubject inactivateAssignment(String id) {
        return changeAssignmentStatus(id, "inactive");
    }

    // Completar asignación
    public TeacherSubject completeAssignment(String id) {
        return changeAssignmentStatus(id, "completed");
    }

    // Contar asignaciones
    public long countAssignments() {
        return teacherSubjectDAO.count();
    }

    // Verificar si existe asignación
    public boolean assignmentExists(String id) {
        return teacherSubjectDAO.existsById(id);
    }

    // Validar formato de año académico (YYYY-YYYY)
    private boolean isValidAcademicYear(String academicYear) {
        if (academicYear == null || academicYear.trim().isEmpty()) {
            return false;
        }

        // Formato esperado: 2024-2025
        String pattern = "^\\d{4}-\\d{4}$";
        if (!academicYear.matches(pattern)) {
            return false;
        }

        String[] years = academicYear.split("-");
        try {
            int year1 = Integer.parseInt(years[0]);
            int year2 = Integer.parseInt(years[1]);

            // El segundo año debe ser exactamente un año después del primero
            return year2 == year1 + 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
