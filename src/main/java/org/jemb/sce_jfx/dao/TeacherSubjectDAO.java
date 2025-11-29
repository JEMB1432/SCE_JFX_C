package org.jemb.sce_jfx.dao;

import org.jemb.sce_jfx.config.DatabaseConfig;
import org.jemb.sce_jfx.dao.interfaces.GenericDAO;
import org.jemb.sce_jfx.models.TeacherSubject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeacherSubjectDAO implements GenericDAO<TeacherSubject, String> {

    private static final String TABLE_NAME = "teacher_subjects";
    private final UserDAO userDAO = new UserDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();

    @Override
    public Optional<TeacherSubject> findById(String id) {
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
                return Optional.of(mapResultSetToTeacherSubject(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding teacher subject by ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return Optional.empty();
    }

    @Override
    public List<TeacherSubject> findAll() {
        List<TeacherSubject> teacherSubjects = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY academic_year DESC, semester DESC, created_at DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                teacherSubjects.add(mapResultSetToTeacherSubject(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all teacher subjects: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return teacherSubjects;
    }

    @Override
    public TeacherSubject save(TeacherSubject teacherSubject) {
        String sql = "INSERT INTO " + TABLE_NAME
                + " (id, teacher_id, subject_id, academic_year, semester, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, teacherSubject.getId());
            stmt.setString(2, teacherSubject.getTeacherId());
            stmt.setString(3, teacherSubject.getSubjectId());
            stmt.setString(4, teacherSubject.getAcademicYear());
            stmt.setInt(5, teacherSubject.getSemester());
            stmt.setString(6, teacherSubject.getStatus());
            stmt.setTimestamp(7, DatabaseUtils.toSqlTimestamp(teacherSubject.getCreatedAt()));
            stmt.setTimestamp(8, DatabaseUtils.toSqlTimestamp(teacherSubject.getUpdatedAt()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating teacher subject failed, no rows affected.");
            }

            return teacherSubject;
        } catch (SQLException e) {
            System.err.println("Error saving teacher subject: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            DatabaseUtils.closeQuietly(stmt, conn);
        }
    }

    @Override
    public TeacherSubject update(TeacherSubject teacherSubject) {
        String sql = "UPDATE " + TABLE_NAME
                + " SET teacher_id = ?, subject_id = ?, academic_year = ?, semester = ?, status = ?, updated_at = ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, teacherSubject.getTeacherId());
            stmt.setString(2, teacherSubject.getSubjectId());
            stmt.setString(3, teacherSubject.getAcademicYear());
            stmt.setInt(4, teacherSubject.getSemester());
            stmt.setString(5, teacherSubject.getStatus());
            stmt.setTimestamp(6, DatabaseUtils.toSqlTimestamp(teacherSubject.getUpdatedAt()));
            stmt.setString(7, teacherSubject.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating teacher subject failed, no rows affected.");
            }

            return teacherSubject;
        } catch (SQLException e) {
            System.err.println("Error updating teacher subject: " + e.getMessage());
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
            System.err.println("Error deleting teacher subject: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error deleting teacher subject", e);
        } finally {
            DatabaseUtils.closeQuietly(stmt, conn);
        }
    }

    public boolean existsById(String id) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if teacher subject exists: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return false;
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
        } catch (SQLException e) {
            System.err.println("Error counting teacher subjects: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return 0;
    }

    // Métodos específicos para TeacherSubject
    public List<TeacherSubject> findByTeacherId(String teacherId) {
        List<TeacherSubject> teacherSubjects = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE teacher_id = ? ORDER BY academic_year DESC, semester DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, teacherId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                teacherSubjects.add(mapResultSetToTeacherSubject(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding teacher subjects by teacher ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return teacherSubjects;
    }

    public List<TeacherSubject> findByTeacherIdAndStatus(String teacherId, String status) {
        List<TeacherSubject> teacherSubjects = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME
                + " WHERE teacher_id = ? AND status = ? ORDER BY academic_year DESC, semester DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, teacherId);
            stmt.setString(2, status);
            rs = stmt.executeQuery();

            while (rs.next()) {
                teacherSubjects.add(mapResultSetToTeacherSubject(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding teacher subjects by teacher ID and status: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return teacherSubjects;
    }

    public List<TeacherSubject> findBySubjectId(String subjectId) {
        List<TeacherSubject> teacherSubjects = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE subject_id = ? ORDER BY academic_year DESC, semester DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, subjectId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                teacherSubjects.add(mapResultSetToTeacherSubject(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding teacher subjects by subject ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return teacherSubjects;
    }

    public Optional<TeacherSubject> findByTeacherAndSubjectAndPeriod(String teacherId, String subjectId,
            String academicYear, int semester) {
        String sql = "SELECT * FROM " + TABLE_NAME
                + " WHERE teacher_id = ? AND subject_id = ? AND academic_year = ? AND semester = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, teacherId);
            stmt.setString(2, subjectId);
            stmt.setString(3, academicYear);
            stmt.setInt(4, semester);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToTeacherSubject(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding teacher subject by teacher, subject and period: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return Optional.empty();
    }

    public List<TeacherSubject> findByAcademicYearAndSemester(String academicYear, int semester) {
        List<TeacherSubject> teacherSubjects = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME
                + " WHERE academic_year = ? AND semester = ? ORDER BY created_at DESC";

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
                teacherSubjects.add(mapResultSetToTeacherSubject(rs, true));
            }
        } catch (SQLException e) {
            System.err.println("Error finding teacher subjects by academic year and semester: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return teacherSubjects;
    }

    // Mapeo de ResultSet a TeacherSubject
    private TeacherSubject mapResultSetToTeacherSubject(ResultSet rs, boolean loadRelations) throws SQLException {
        TeacherSubject teacherSubject = new TeacherSubject();
        teacherSubject.setId(rs.getString("id"));
        teacherSubject.setTeacherId(rs.getString("teacher_id"));
        teacherSubject.setSubjectId(rs.getString("subject_id"));
        teacherSubject.setAcademicYear(rs.getString("academic_year"));
        teacherSubject.setSemester(rs.getInt("semester"));
        teacherSubject.setStatus(rs.getString("status"));
        teacherSubject.setCreatedAt(DatabaseUtils.toLocalDateTime(rs.getTimestamp("created_at")));
        teacherSubject.setUpdatedAt(DatabaseUtils.toLocalDateTime(rs.getTimestamp("updated_at")));

        // Cargar relaciones si se solicita
        if (loadRelations) {
            userDAO.findById(teacherSubject.getTeacherId()).ifPresent(teacherSubject::setTeacher);
            subjectDAO.findById(teacherSubject.getSubjectId()).ifPresent(teacherSubject::setSubject);
        }

        return teacherSubject;
    }
}
