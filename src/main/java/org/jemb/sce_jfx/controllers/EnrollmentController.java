package org.jemb.sce_jfx.controllers;

import org.jemb.sce_jfx.dao.EnrollmentDAO;
import org.jemb.sce_jfx.dao.StudentDAO;
import org.jemb.sce_jfx.dao.SubjectDAO;
import org.jemb.sce_jfx.models.Enrollment;
import org.jemb.sce_jfx.models.Student;
import org.jemb.sce_jfx.models.Subject;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class EnrollmentController {
    private final EnrollmentDAO enrollmentDAO;
    private final StudentDAO studentDAO;
    private final SubjectDAO subjectDAO;

    public EnrollmentController() {
        this.enrollmentDAO = new EnrollmentDAO();
        this.studentDAO = new StudentDAO();
        this.subjectDAO = new SubjectDAO();
    }

    // Inscribir estudiante en materia
    public Enrollment enrollStudent(String studentId, String subjectId, String academicYear, int semester) {
        // Validar estudiante
        Optional<Student> studentOpt = studentDAO.findById(studentId);
        if (studentOpt.isEmpty()) {
            throw new IllegalArgumentException("El estudiante no existe");
        }
        
        Student student = studentOpt.get();
        if (!student.isActive()) {
            throw new IllegalArgumentException("El estudiante no está activo");
        }

        // Validar materia
        Optional<Subject> subjectOpt = subjectDAO.findById(subjectId);
        if (subjectOpt.isEmpty()) {
            throw new IllegalArgumentException("La materia no existe");
        }
        
        Subject subject = subjectOpt.get();
        if (!subject.isActive()) {
            throw new IllegalArgumentException("La materia no está activa");
        }

        // Validar que el semestre del estudiante coincida con el semestre disponible de la materia
        if (student.getSemester() == null) {
            throw new IllegalArgumentException("El estudiante no tiene un semestre asignado. Por favor, actualice el semestre del estudiante.");
        }

        if (subject.getSemesterAvailable() != null) {
            if (student.getSemester() < subject.getSemesterAvailable()) {
                throw new IllegalArgumentException(
                    String.format("El estudiante está en el semestre %d, pero la materia '%s' está disponible a partir del semestre %d. " +
                                 "El estudiante debe estar en el semestre %d o superior para inscribirse en esta materia.",
                    student.getSemester(), subject.getName(), subject.getSemesterAvailable(), subject.getSemesterAvailable()));
            }
        }

        // Validar semestre del período académico
        if (semester < 1 || semester > 12) {
            throw new IllegalArgumentException("El semestre debe estar entre 1 y 12");
        }

        // Validar formato de año académico (ej: 2024-2025)
        if (!isValidAcademicYear(academicYear)) {
            throw new IllegalArgumentException("Formato de año académico inválido. Use el formato: YYYY-YYYY");
        }

        // Verificar si ya está inscrito
        Optional<Enrollment> existing = enrollmentDAO.findByStudentAndSubjectAndPeriod(
            studentId, subjectId, academicYear, semester
        );
        if (existing.isPresent()) {
            throw new IllegalArgumentException("El estudiante ya está inscrito en esta materia para este período");
        }

        Enrollment enrollment = new Enrollment(studentId, subjectId, academicYear, semester);
        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setStatus("enrolled");

        Enrollment saved = enrollmentDAO.save(enrollment);
        
        if (saved == null) {
            throw new RuntimeException("Error al inscribir al estudiante");
        }
        
        return saved;
    }

    // Obtener inscripción por ID
    public Optional<Enrollment> getEnrollmentById(String id) {
        return enrollmentDAO.findById(id);
    }

    // Obtener todas las inscripciones
    public List<Enrollment> getAllEnrollments() {
        return enrollmentDAO.findAll();
    }

    // Obtener inscripciones de un estudiante
    public List<Enrollment> getEnrollmentsByStudent(String studentId) {
        if (!studentDAO.existsById(studentId)) {
            throw new IllegalArgumentException("El estudiante no existe");
        }
        return enrollmentDAO.findByStudentId(studentId);
    }

    // Obtener inscripciones de una materia
    public List<Enrollment> getEnrollmentsBySubject(String subjectId) {
        if (!subjectDAO.existsById(subjectId)) {
            throw new IllegalArgumentException("La materia no existe");
        }
        return enrollmentDAO.findBySubjectId(subjectId);
    }

    // Obtener inscripciones por período académico
    public List<Enrollment> getEnrollmentsByPeriod(String academicYear, int semester) {
        if (semester < 1 || semester > 12) {
            throw new IllegalArgumentException("El semestre debe estar entre 1 y 12");
        }
        return enrollmentDAO.findByAcademicYearAndSemester(academicYear, semester);
    }

    // Obtener inscripciones por estado
    public List<Enrollment> getEnrollmentsByStatus(String status) {
        return enrollmentDAO.findByStatus(status);
    }

    // Actualizar inscripción
    public Enrollment updateEnrollment(Enrollment enrollment) {
        // Validar que la inscripción exista
        Optional<Enrollment> existingOpt = enrollmentDAO.findById(enrollment.getId());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("La inscripción no existe");
        }

        // Validar estudiante si cambió
        if (!studentDAO.existsById(enrollment.getStudentId())) {
            throw new IllegalArgumentException("El estudiante no existe");
        }

        // Validar materia si cambió
        if (!subjectDAO.existsById(enrollment.getSubjectId())) {
            throw new IllegalArgumentException("La materia no existe");
        }

        // Validar formato de año académico
        if (enrollment.getAcademicYear() != null && !isValidAcademicYear(enrollment.getAcademicYear())) {
            throw new IllegalArgumentException("Formato de año académico inválido. Use el formato: YYYY-YYYY");
        }

        // Validar semestre
        if (enrollment.getSemester() < 1 || enrollment.getSemester() > 12) {
            throw new IllegalArgumentException("El semestre debe estar entre 1 y 12");
        }

        Enrollment updated = enrollmentDAO.update(enrollment);
        
        if (updated == null) {
            throw new RuntimeException("Error al actualizar la inscripción");
        }
        
        return updated;
    }

    // Cambiar estado de inscripción
    public Enrollment changeEnrollmentStatus(String id, String status) {
        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("Estado inválido. Use: enrolled, completed, dropped");
        }

        Optional<Enrollment> enrollmentOpt = enrollmentDAO.findById(id);
        if (enrollmentOpt.isEmpty()) {
            throw new IllegalArgumentException("La inscripción no existe");
        }

        Enrollment enrollment = enrollmentOpt.get();
        enrollment.setStatus(status);
        
        return updateEnrollment(enrollment);
    }

    // Completar inscripción
    public Enrollment completeEnrollment(String id) {
        return changeEnrollmentStatus(id, "completed");
    }

    // Dar de baja inscripción
    public Enrollment dropEnrollment(String id) {
        return changeEnrollmentStatus(id, "dropped");
    }

    // Eliminar inscripción
    public void deleteEnrollment(String id) {
        if (!enrollmentDAO.findById(id).isPresent()) {
            throw new IllegalArgumentException("La inscripción no existe");
        }
        
        enrollmentDAO.delete(id);
    }

    // Validar formato de año académico
    private boolean isValidAcademicYear(String academicYear) {
        if (academicYear == null || academicYear.trim().isEmpty()) {
            return false;
        }
        // Formato: YYYY-YYYY
        return academicYear.matches("\\d{4}-\\d{4}");
    }

    // Validar estado
    private boolean isValidStatus(String status) {
        return status != null && (status.equals("enrolled") || 
                                  status.equals("completed") || 
                                  status.equals("dropped"));
    }
}

