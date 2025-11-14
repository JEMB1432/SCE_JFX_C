package org.jemb.sce_jfx.dao;

import org.jemb.sce_jfx.config.DatabaseConfig;
import org.jemb.sce_jfx.dao.interfaces.GenericDAO;
import org.jemb.sce_jfx.models.Grade;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GradeDAO implements GenericDAO<Grade, String> {

    private static final String TABLE_NAME = "grades";
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final EvaluationTypeDAO evaluationTypeDAO = new EvaluationTypeDAO();

    @Override
    public Optional<Grade> findById(String id) {
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
                return Optional.of(mapResultSetToGrade(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding grade by ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return Optional.empty();
    }

    @Override
    public List<Grade> findAll() {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY graded_at DESC, created_at DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                grades.add(mapResultSetToGrade(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all grades: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return grades;
    }

    @Override
    public Grade save(Grade grade) {
        String sql = "INSERT INTO " + TABLE_NAME + " (id, enrollment_id, evaluation_type_id, score, comments, graded_at, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, grade.getId());
            stmt.setString(2, grade.getEnrollmentId());
            stmt.setString(3, grade.getEvaluationTypeId());
            DatabaseUtils.setNullableParameter(stmt, 4, grade.getScore());
            DatabaseUtils.setNullableParameter(stmt, 5, grade.getComments());
            DatabaseUtils.setNullableParameter(stmt, 6, DatabaseUtils.toSqlTimestamp(grade.getGradedAt()));
            stmt.setTimestamp(7, DatabaseUtils.toSqlTimestamp(grade.getCreatedAt()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating grade failed, no rows affected.");
            }

            return grade;
        } catch (SQLException e) {
            System.err.println("Error saving grade: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            DatabaseUtils.closeQuietly(stmt, conn);
        }
    }

    @Override
    public Grade update(Grade grade) {
        String sql = "UPDATE " + TABLE_NAME + " SET score = ?, comments = ?, graded_at = ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);

            DatabaseUtils.setNullableParameter(stmt, 1, grade.getScore());
            DatabaseUtils.setNullableParameter(stmt, 2, grade.getComments());
            DatabaseUtils.setNullableParameter(stmt, 3, DatabaseUtils.toSqlTimestamp(grade.getGradedAt()));
            stmt.setString(4, grade.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating grade failed, no rows affected.");
            }

            return grade;
        } catch (SQLException e) {
            System.err.println("Error updating grade: " + e.getMessage());
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
            System.err.println("Error deleting grade: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error deleting grade", e);
        } finally {
            DatabaseUtils.closeQuietly(stmt, conn);
        }
    }

    // Métodos específicos para Grade
    public List<Grade> findByEnrollmentId(String enrollmentId) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE enrollment_id = ? ORDER BY created_at";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, enrollmentId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                grades.add(mapResultSetToGrade(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding grades by enrollment ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return grades;
    }

    public Optional<Grade> findByEnrollmentAndEvaluationType(String enrollmentId, String evaluationTypeId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE enrollment_id = ? AND evaluation_type_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, enrollmentId);
            stmt.setString(2, evaluationTypeId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToGrade(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding grade by enrollment and evaluation type: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return Optional.empty();
    }

    public List<Grade> findByEvaluationTypeId(String evaluationTypeId) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE evaluation_type_id = ? ORDER BY graded_at DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, evaluationTypeId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                grades.add(mapResultSetToGrade(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding grades by evaluation type ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return grades;
    }

    public double calculateFinalGrade(String enrollmentId) {
        String sql = """
            SELECT SUM(g.score * et.weight / 100.0) as final_grade
            FROM grades g
            INNER JOIN evaluation_types et ON g.evaluation_type_id = et.id
            WHERE g.enrollment_id = ? AND g.score IS NOT NULL
            """;

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, enrollmentId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                double finalGrade = rs.getDouble("final_grade");
                return rs.wasNull() ? 0.0 : finalGrade;
            }
        } catch (SQLException e) {
            System.err.println("Error calculating final grade: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return 0.0;
    }

    public List<Grade> findGradedByEnrollmentId(String enrollmentId) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE enrollment_id = ? AND score IS NOT NULL ORDER BY graded_at DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, enrollmentId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                grades.add(mapResultSetToGrade(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding graded grades by enrollment ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return grades;
    }

    // Mapeo de ResultSet a Grade
    private Grade mapResultSetToGrade(ResultSet rs, boolean loadRelations) throws SQLException {
        Grade grade = new Grade();
        grade.setId(rs.getString("id"));
        grade.setEnrollmentId(rs.getString("enrollment_id"));
        grade.setEvaluationTypeId(rs.getString("evaluation_type_id"));

        double score = rs.getDouble("score");
        if (!rs.wasNull()) {
            grade.setScore(score);
        }

        grade.setComments(rs.getString("comments"));

        Timestamp gradedAt = rs.getTimestamp("graded_at");
        if (gradedAt != null) {
            grade.setGradedAt(DatabaseUtils.toLocalDateTime(gradedAt));
        }

        grade.setCreatedAt(DatabaseUtils.toLocalDateTime(rs.getTimestamp("created_at")));

        // Cargar relaciones si se solicita
        if (loadRelations) {
            enrollmentDAO.findById(grade.getEnrollmentId()).ifPresent(grade::setEnrollment);
            evaluationTypeDAO.findById(grade.getEvaluationTypeId()).ifPresent(grade::setEvaluationType);
        }

        return grade;
    }
}

