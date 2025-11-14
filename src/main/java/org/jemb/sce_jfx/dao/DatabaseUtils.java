package org.jemb.sce_jfx.dao;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DatabaseUtils {

    // Métodos para convertir entre tipos Java y SQL
    public static Date toSqlDate(LocalDate localDate) {
        return localDate != null ? Date.valueOf(localDate) : null;
    }

    public static LocalDate toLocalDate(Date sqlDate) {
        return sqlDate != null ? sqlDate.toLocalDate() : null;
    }

    public static Timestamp toSqlTimestamp(LocalDateTime localDateTime) {
        return localDateTime != null ? Timestamp.valueOf(localDateTime) : null;
    }

    public static LocalDateTime toLocalDateTime(Timestamp sqlTimestamp) {
        return sqlTimestamp != null ? sqlTimestamp.toLocalDateTime() : null;
    }

    // Método para establecer parámetros null-safe
    public static void setNullableParameter(PreparedStatement stmt, int index, Object value) throws SQLException {
        if (value != null) {
            stmt.setObject(index, value);
        } else {
            stmt.setNull(index, Types.NULL);
        }
    }

    // Método para cerrar recursos
    public static void closeQuietly(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    // Log silently
                    System.err.println("Error closing resource: " + e.getMessage());
                }
            }
        }
    }
}
