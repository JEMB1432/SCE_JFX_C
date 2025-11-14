package org.jemb.sce_jfx.dao;

import org.jemb.sce_jfx.config.DatabaseConfig;
import org.jemb.sce_jfx.dao.interfaces.GenericDAO;
import org.jemb.sce_jfx.models.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubjectDAO implements GenericDAO<Subject, String> {

    private static final String TABLE_NAME = "subjects";

    @Override
    public Optional<Subject> findById(String id) {
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
                return Optional.of(mapResultSetToSubject(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding subject by ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return Optional.empty();
    }

    @Override
    public List<Subject> findAll() {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY subject_code";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                subjects.add(mapResultSetToSubject(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all subjects: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return subjects;
    }

    @Override
    public Subject save(Subject subject) {
        String sql = "INSERT INTO " + TABLE_NAME + " (id, subject_code, name, description, credits, hours_per_week, semester_available, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, subject.getId());
            stmt.setString(2, subject.getSubjectCode());
            stmt.setString(3, subject.getName());
            DatabaseUtils.setNullableParameter(stmt, 4, subject.getDescription());
            stmt.setInt(5, subject.getCredits());
            stmt.setInt(6, subject.getHoursPerWeek());
            DatabaseUtils.setNullableParameter(stmt, 7, subject.getSemesterAvailable());
            stmt.setString(8, subject.getStatus());
            stmt.setTimestamp(9, DatabaseUtils.toSqlTimestamp(subject.getCreatedAt()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating subject failed, no rows affected.");
            }

            return subject;
        } catch (SQLException e) {
            System.err.println("Error saving subject: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            DatabaseUtils.closeQuietly(stmt, conn);
        }
    }

    @Override
    public Subject update(Subject subject) {
        String sql = "UPDATE " + TABLE_NAME + " SET subject_code = ?, name = ?, description = ?, credits = ?, hours_per_week = ?, semester_available = ?, status = ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, subject.getSubjectCode());
            stmt.setString(2, subject.getName());
            DatabaseUtils.setNullableParameter(stmt, 3, subject.getDescription());
            stmt.setInt(4, subject.getCredits());
            stmt.setInt(5, subject.getHoursPerWeek());
            DatabaseUtils.setNullableParameter(stmt, 6, subject.getSemesterAvailable());
            stmt.setString(7, subject.getStatus());
            stmt.setString(8, subject.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating subject failed, no rows affected.");
            }

            return subject;
        } catch (SQLException e) {
            System.err.println("Error updating subject: " + e.getMessage());
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
            System.err.println("Error deleting subject: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error deleting subject", e);
        } finally {
            DatabaseUtils.closeQuietly(stmt, conn);
        }
    }

    public boolean existsById(String id) {
        String sql = "SELECT 1 FROM " + TABLE_NAME + " WHERE id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            rs = stmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking if subject exists: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME;

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Error counting subjects: " + e.getMessage());
            e.printStackTrace();
            return 0;
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }
    }

    // Métodos específicos para Subject
    public Optional<Subject> findBySubjectCode(String subjectCode) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE subject_code = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, subjectCode);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToSubject(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding subject by code: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return Optional.empty();
    }

    public List<Subject> findByStatus(String status) {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE status = ? ORDER BY subject_code";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            rs = stmt.executeQuery();

            while (rs.next()) {
                subjects.add(mapResultSetToSubject(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding subjects by status: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return subjects;
    }

    public List<Subject> findBySemesterAvailable(int semester) {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE semester_available = ? AND status = 'active' ORDER BY subject_code";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, semester);
            rs = stmt.executeQuery();

            while (rs.next()) {
                subjects.add(mapResultSetToSubject(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding subjects by semester: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return subjects;
    }

    // Mapeo de ResultSet a Subject
    private Subject mapResultSetToSubject(ResultSet rs) throws SQLException {
        Subject subject = new Subject();
        subject.setId(rs.getString("id"));
        subject.setSubjectCode(rs.getString("subject_code"));
        subject.setName(rs.getString("name"));
        subject.setDescription(rs.getString("description"));
        subject.setCredits(rs.getInt("credits"));
        subject.setHoursPerWeek(rs.getInt("hours_per_week"));

        int semesterAvailable = rs.getInt("semester_available");
        if (!rs.wasNull()) {
            subject.setSemesterAvailable(semesterAvailable);
        }

        subject.setStatus(rs.getString("status"));
        subject.setCreatedAt(DatabaseUtils.toLocalDateTime(rs.getTimestamp("created_at")));

        return subject;
    }
}