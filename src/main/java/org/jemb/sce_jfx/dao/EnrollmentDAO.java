package org.jemb.sce_jfx.dao;

import org.jemb.sce_jfx.config.DatabaseConfig;
import org.jemb.sce_jfx.dao.interfaces.GenericDAO;
import org.jemb.sce_jfx.models.Enrollment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnrollmentDAO implements GenericDAO<Enrollment, String> {

    private static final String TABLE_NAME = "enrollments";
    private final StudentDAO studentDAO = new StudentDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();

    @Override
    public Optional<Enrollment> findById(String id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToEnrollment(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding enrollment by ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return Optional.empty();
    }

    @Override
    public List<Enrollment> findAll() {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY enrollment_date DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollment(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all enrollments: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return enrollments;
    }

    @Override
    public Enrollment save(Enrollment enrollment) {
        String sql = "INSERT INTO " + TABLE_NAME + " (id, student_id, subject_id, academic_year, semester, enrollment_date, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, enrollment.getId());
            stmt.setString(2, enrollment.getStudentId());
            stmt.setString(3, enrollment.getSubjectId());
            stmt.setString(4, enrollment.getAcademicYear());
            stmt.setInt(5, enrollment.getSemester());
            stmt.setDate(6, DatabaseUtils.toSqlDate(enrollment.getEnrollmentDate()));
            stmt.setString(7, enrollment.getStatus());
            stmt.setTimestamp(8, DatabaseUtils.toSqlTimestamp(enrollment.getCreatedAt()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating enrollment failed, no rows affected.");
            }

            return enrollment;
        } catch (SQLException e) {
            System.err.println("Error saving enrollment: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            DatabaseUtils.closeQuietly(stmt, conn);
        }
    }

    @Override
    public Enrollment update(Enrollment enrollment) {
        String sql = "UPDATE " + TABLE_NAME + " SET student_id = ?, subject_id = ?, academic_year = ?, semester = ?, enrollment_date = ?, status = ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, enrollment.getStudentId());
            stmt.setString(2, enrollment.getSubjectId());
            stmt.setString(3, enrollment.getAcademicYear());
            stmt.setInt(4, enrollment.getSemester());
            stmt.setDate(5, DatabaseUtils.toSqlDate(enrollment.getEnrollmentDate()));
            stmt.setString(6, enrollment.getStatus());
            stmt.setString(7, enrollment.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating enrollment failed, no rows affected.");
            }

            return enrollment;
        } catch (SQLException e) {
            System.err.println("Error updating enrollment: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            DatabaseUtils.closeQuietly(stmt, conn);
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting enrollment: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error deleting enrollment", e);
        } finally {
            DatabaseUtils.closeQuietly(stmt, conn);
        }
    }

    // Métodos específicos para Enrollment
    public List<Enrollment> findByStudentId(String studentId) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE student_id = ? ORDER BY academic_year DESC, semester DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollment(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding enrollments by student ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return enrollments;
    }

    public List<Enrollment> findBySubjectId(String subjectId) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE subject_id = ? ORDER BY enrollment_date DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, subjectId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollment(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding enrollments by subject ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return enrollments;
    }

    public List<Enrollment> findByAcademicYearAndSemester(String academicYear, int semester) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE academic_year = ? AND semester = ? ORDER BY enrollment_date DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, academicYear);
            stmt.setInt(2, semester);
            rs = stmt.executeQuery();

            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollment(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding enrollments by academic year and semester: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return enrollments;
    }

    public Optional<Enrollment> findByStudentAndSubjectAndPeriod(String studentId, String subjectId, String academicYear, int semester) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE student_id = ? AND subject_id = ? AND academic_year = ? AND semester = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentId);
            stmt.setString(2, subjectId);
            stmt.setString(3, academicYear);
            stmt.setInt(4, semester);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToEnrollment(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding enrollment by student, subject and period: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return Optional.empty();
    }

    public List<Enrollment> findByStatus(String status) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE status = ? ORDER BY enrollment_date DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            rs = stmt.executeQuery();

            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollment(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding enrollments by status: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return enrollments;
    }

    // Mapeo de ResultSet a Enrollment
    private Enrollment mapResultSetToEnrollment(ResultSet rs, boolean loadRelations) throws SQLException {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(rs.getString("id"));
        enrollment.setStudentId(rs.getString("student_id"));
        enrollment.setSubjectId(rs.getString("subject_id"));
        enrollment.setAcademicYear(rs.getString("academic_year"));
        enrollment.setSemester(rs.getInt("semester"));
        enrollment.setEnrollmentDate(DatabaseUtils.toLocalDate(rs.getDate("enrollment_date")));
        enrollment.setStatus(rs.getString("status"));
        enrollment.setCreatedAt(DatabaseUtils.toLocalDateTime(rs.getTimestamp("created_at")));

        // Cargar relaciones si se solicita
        if (loadRelations) {
            studentDAO.findById(enrollment.getStudentId()).ifPresent(enrollment::setStudent);
            subjectDAO.findById(enrollment.getSubjectId()).ifPresent(enrollment::setSubject);
        }

        return enrollment;
    }
}

