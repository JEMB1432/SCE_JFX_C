package org.jemb.sce_jfx.dao;

import org.jemb.sce_jfx.config.DatabaseConfig;
import org.jemb.sce_jfx.dao.interfaces.GenericDAO;
import org.jemb.sce_jfx.models.EvaluationType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EvaluationTypeDAO implements GenericDAO<EvaluationType, String> {

    private static final String TABLE_NAME = "evaluation_types";
    private final SubjectDAO subjectDAO = new SubjectDAO();

    @Override
    public Optional<EvaluationType> findById(String id) {
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
                return Optional.of(mapResultSetToEvaluationType(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding evaluation type by ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return Optional.empty();
    }

    @Override
    public List<EvaluationType> findAll() {
        List<EvaluationType> evaluationTypes = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY subject_id, evaluation_order";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                evaluationTypes.add(mapResultSetToEvaluationType(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all evaluation types: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return evaluationTypes;
    }

    @Override
    public EvaluationType save(EvaluationType evaluationType) {
        String sql = "INSERT INTO " + TABLE_NAME + " (id, subject_id, name, description, weight, max_score, evaluation_order, is_final_exam, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, evaluationType.getId());
            stmt.setString(2, evaluationType.getSubjectId());
            stmt.setString(3, evaluationType.getName());
            DatabaseUtils.setNullableParameter(stmt, 4, evaluationType.getDescription());
            stmt.setDouble(5, evaluationType.getWeight());
            stmt.setDouble(6, evaluationType.getMaxScore());
            DatabaseUtils.setNullableParameter(stmt, 7, evaluationType.getEvaluationOrder());
            stmt.setBoolean(8, evaluationType.isFinalExam());
            stmt.setTimestamp(9, DatabaseUtils.toSqlTimestamp(evaluationType.getCreatedAt()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating evaluation type failed, no rows affected.");
            }

            return evaluationType;
        } catch (SQLException e) {
            System.err.println("Error saving evaluation type: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            DatabaseUtils.closeQuietly(stmt, conn);
        }
    }

    @Override
    public EvaluationType update(EvaluationType evaluationType) {
        String sql = "UPDATE " + TABLE_NAME + " SET subject_id = ?, name = ?, description = ?, weight = ?, max_score = ?, evaluation_order = ?, is_final_exam = ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, evaluationType.getSubjectId());
            stmt.setString(2, evaluationType.getName());
            DatabaseUtils.setNullableParameter(stmt, 3, evaluationType.getDescription());
            stmt.setDouble(4, evaluationType.getWeight());
            stmt.setDouble(5, evaluationType.getMaxScore());
            DatabaseUtils.setNullableParameter(stmt, 6, evaluationType.getEvaluationOrder());
            stmt.setBoolean(7, evaluationType.isFinalExam());
            stmt.setString(8, evaluationType.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating evaluation type failed, no rows affected.");
            }

            return evaluationType;
        } catch (SQLException e) {
            System.err.println("Error updating evaluation type: " + e.getMessage());
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
            System.err.println("Error deleting evaluation type: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error deleting evaluation type", e);
        } finally {
            DatabaseUtils.closeQuietly(stmt, conn);
        }
    }

    // Métodos específicos para EvaluationType
    public List<EvaluationType> findBySubjectId(String subjectId) {
        List<EvaluationType> evaluationTypes = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE subject_id = ? ORDER BY evaluation_order, name";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, subjectId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                evaluationTypes.add(mapResultSetToEvaluationType(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding evaluation types by subject ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return evaluationTypes;
    }

    public Optional<EvaluationType> findFinalExamBySubjectId(String subjectId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE subject_id = ? AND is_final_exam = TRUE LIMIT 1";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, subjectId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToEvaluationType(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding final exam by subject ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return Optional.empty();
    }

    public double getTotalWeightBySubjectId(String subjectId) {
        String sql = "SELECT SUM(weight) as total_weight FROM " + TABLE_NAME + " WHERE subject_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, subjectId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                double total = rs.getDouble("total_weight");
                return rs.wasNull() ? 0.0 : total;
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total weight by subject ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return 0.0;
    }

    // Mapeo de ResultSet a EvaluationType
    private EvaluationType mapResultSetToEvaluationType(ResultSet rs, boolean loadRelations) throws SQLException {
        EvaluationType evaluationType = new EvaluationType();
        evaluationType.setId(rs.getString("id"));
        evaluationType.setSubjectId(rs.getString("subject_id"));
        evaluationType.setName(rs.getString("name"));
        evaluationType.setDescription(rs.getString("description"));
        evaluationType.setWeight(rs.getDouble("weight"));
        evaluationType.setMaxScore(rs.getDouble("max_score"));

        int evaluationOrder = rs.getInt("evaluation_order");
        if (!rs.wasNull()) {
            evaluationType.setEvaluationOrder(evaluationOrder);
        }

        evaluationType.setFinalExam(rs.getBoolean("is_final_exam"));
        evaluationType.setCreatedAt(DatabaseUtils.toLocalDateTime(rs.getTimestamp("created_at")));

        // Cargar relación si se solicita
        if (loadRelations) {
            subjectDAO.findById(evaluationType.getSubjectId()).ifPresent(evaluationType::setSubject);
        }

        return evaluationType;
    }
}

