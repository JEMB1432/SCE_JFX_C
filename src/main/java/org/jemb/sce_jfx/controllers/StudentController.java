package org.jemb.sce_jfx.controllers;

import org.jemb.sce_jfx.dao.StudentDAO;
import org.jemb.sce_jfx.models.Student;

import java.util.List;
import java.util.Optional;

public class StudentController {
    private final StudentDAO studentDAO;

    public StudentController() {
        this.studentDAO = new StudentDAO();
    }

    // Crear nuevo estudiante
    public Student createStudent(String studentCode, String firstName, String lastName, String email) {
        // Validar que el código de estudiante no exista
        if (studentDAO.findByStudentCode(studentCode).isPresent()) {
            throw new IllegalArgumentException("El código de estudiante ya existe: " + studentCode);
        }

        // Validar que el email no exista
        if (studentDAO.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado: " + email);
        }

        Student student = new Student(studentCode, firstName, lastName, email);
        Student saved = studentDAO.save(student);
        
        if (saved == null) {
            throw new RuntimeException("Error al crear el estudiante");
        }
        
        return saved;
    }

    // Obtener estudiante por ID
    public Optional<Student> getStudentById(String id) {
        return studentDAO.findById(id);
    }

    // Obtener estudiante por código
    public Optional<Student> getStudentByCode(String studentCode) {
        return studentDAO.findByStudentCode(studentCode);
    }

    // Obtener todos los estudiantes
    public List<Student> getAllStudents() {
        return studentDAO.findAll();
    }

    // Obtener estudiantes por estado
    public List<Student> getStudentsByStatus(String status) {
        return studentDAO.findByStatus(status);
    }

    // Obtener solo estudiantes activos
    public List<Student> getActiveStudents() {
        return studentDAO.findByStatus("active");
    }

    // Actualizar estudiante
    public Student updateStudent(Student student) {
        // Validar que el estudiante exista
        if (!studentDAO.existsById(student.getId())) {
            throw new IllegalArgumentException("El estudiante no existe");
        }

        // Validar código único si cambió
        Optional<Student> existingByCode = studentDAO.findByStudentCode(student.getStudentCode());
        if (existingByCode.isPresent() && !existingByCode.get().getId().equals(student.getId())) {
            throw new IllegalArgumentException("El código de estudiante ya está en uso: " + student.getStudentCode());
        }

        // Validar email único si cambió
        Optional<Student> existingByEmail = studentDAO.findByEmail(student.getEmail());
        if (existingByEmail.isPresent() && !existingByEmail.get().getId().equals(student.getId())) {
            throw new IllegalArgumentException("El email ya está en uso: " + student.getEmail());
        }

        student.setUpdatedAt(java.time.LocalDateTime.now());
        Student updated = studentDAO.update(student);
        
        if (updated == null) {
            throw new RuntimeException("Error al actualizar el estudiante");
        }
        
        return updated;
    }

    // Eliminar estudiante
    public void deleteStudent(String id) {
        if (!studentDAO.existsById(id)) {
            throw new IllegalArgumentException("El estudiante no existe");
        }
        
        studentDAO.delete(id);
    }

    // Cambiar estado del estudiante
    public Student changeStudentStatus(String id, String status) {
        Optional<Student> studentOpt = studentDAO.findById(id);
        if (studentOpt.isEmpty()) {
            throw new IllegalArgumentException("El estudiante no existe");
        }

        Student student = studentOpt.get();
        student.setStatus(status);
        student.setUpdatedAt(java.time.LocalDateTime.now());
        
        return updateStudent(student);
    }

    // Activar estudiante
    public Student activateStudent(String id) {
        return changeStudentStatus(id, "active");
    }

    // Desactivar estudiante
    public Student deactivateStudent(String id) {
        return changeStudentStatus(id, "inactive");
    }

    // Graduar estudiante
    public Student graduateStudent(String id) {
        return changeStudentStatus(id, "graduated");
    }

    // Contar estudiantes
    public long countStudents() {
        return studentDAO.count();
    }

    // Verificar si existe estudiante
    public boolean studentExists(String id) {
        return studentDAO.existsById(id);
    }
}

