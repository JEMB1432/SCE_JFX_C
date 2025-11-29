package org.jemb.sce_jfx.dao;

import org.jemb.sce_jfx.config.DatabaseConfig;
import org.jemb.sce_jfx.dao.interfaces.GenericDAO;
import org.jemb.sce_jfx.models.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentDAO implements GenericDAO<Student, String> {

    private static final String TABLE_NAME = "students";

    @Override
    public Optional<Student> findById(String id) {
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
                return Optional.of(mapResultSetToStudent(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding student by ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return Optional.empty();
    }

    @Override
    public List<Student> findAll() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all students: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return students;
    }

    @Override
    public Student save(Student student) {
        String sql = "INSERT INTO " + TABLE_NAME + " (id, student_code, first_name, last_name, email, phone, date_of_birth, address, enrollment_date, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, student.getId());
            stmt.setString(2, student.getStudentCode());
            stmt.setString(3, student.getFirstName());
            stmt.setString(4, student.getLastName());
            stmt.setString(5, student.getEmail());
            DatabaseUtils.setNullableParameter(stmt, 6, student.getPhone());
            DatabaseUtils.setNullableParameter(stmt, 7, DatabaseUtils.toSqlDate(student.getDateOfBirth()));
            DatabaseUtils.setNullableParameter(stmt, 8, student.getAddress());
            stmt.setDate(9, DatabaseUtils.toSqlDate(student.getEnrollmentDate()));
            stmt.setString(10, student.getStatus());
            stmt.setTimestamp(11, DatabaseUtils.toSqlTimestamp(student.getCreatedAt()));
            stmt.setTimestamp(12, DatabaseUtils.toSqlTimestamp(student.getUpdatedAt()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating student failed, no rows affected.");
            }

            return student;
        } catch (SQLException e) {
            System.err.println("Error saving student: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            DatabaseUtils.closeQuietly(stmt, conn);
        }
    }

    @Override
    public Student update(Student student) {
        String sql = "UPDATE " + TABLE_NAME + " SET student_code = ?, first_name = ?, last_name = ?, email = ?, phone = ?, date_of_birth = ?, address = ?, status = ?, updated_at = ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, student.getStudentCode());
            stmt.setString(2, student.getFirstName());
            stmt.setString(3, student.getLastName());
            stmt.setString(4, student.getEmail());
            DatabaseUtils.setNullableParameter(stmt, 5, student.getPhone());
            DatabaseUtils.setNullableParameter(stmt, 6, DatabaseUtils.toSqlDate(student.getDateOfBirth()));
            DatabaseUtils.setNullableParameter(stmt, 7, student.getAddress());
            stmt.setString(8, student.getStatus());
            stmt.setTimestamp(9, DatabaseUtils.toSqlTimestamp(student.getUpdatedAt()));
            stmt.setString(10, student.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating student failed, no rows affected.");
            }

            return student;
        } catch (SQLException e) {
            System.err.println("Error updating student: " + e.getMessage());
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
            System.err.println("Error deleting student: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error deleting student", e);
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
            System.err.println("Error checking if student exists: " + e.getMessage());
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
            System.err.println("Error counting students: " + e.getMessage());
            e.printStackTrace();
            return 0;
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }
    }

    // Métodos específicos para Student
    public Optional<Student> findByStudentCode(String studentCode) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE student_code = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentCode);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToStudent(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding student by code: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return Optional.empty();
    }

    public Optional<Student> findByEmail(String email) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE email = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToStudent(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding student by email: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return Optional.empty();
    }

    public List<Student> findByStatus(String status) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE status = ? ORDER BY first_name, last_name";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            rs = stmt.executeQuery();

            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding students by status: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtils.closeQuietly(rs, stmt, conn);
        }

        return students;
    }

    // Mapeo de ResultSet a Student
    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setId(rs.getString("id"));
        student.setStudentCode(rs.getString("student_code"));
        student.setFirstName(rs.getString("first_name"));
        student.setLastName(rs.getString("last_name"));
        student.setEmail(rs.getString("email"));
        student.setPhone(rs.getString("phone"));
        student.setDateOfBirth(DatabaseUtils.toLocalDate(rs.getDate("date_of_birth")));
        student.setAddress(rs.getString("address"));
        student.setEnrollmentDate(DatabaseUtils.toLocalDate(rs.getDate("enrollment_date")));
        student.setStatus(rs.getString("status"));
        student.setCreatedAt(DatabaseUtils.toLocalDateTime(rs.getTimestamp("created_at")));
        student.setUpdatedAt(DatabaseUtils.toLocalDateTime(rs.getTimestamp("updated_at")));

        return student;
    }
}