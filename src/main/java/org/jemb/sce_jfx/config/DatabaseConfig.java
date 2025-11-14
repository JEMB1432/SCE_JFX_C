package org.jemb.sce_jfx.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {
    private static HikariDataSource dataSource;

    public static void initialize() {
        try {
            Properties props = new Properties();
            InputStream input = DatabaseConfig.class.getResourceAsStream(
                    "/org/jemb/sce_jfx/config.properties"
            );

            if (input == null) {
                throw new RuntimeException("No se encontró config.properties");
            }

            props.load(input);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));

            // MySQL o MariaDB: elegir uno
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");  
            // config.setDriverClassName("org.mariadb.jdbc.Driver");

            // Configuración del pool
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            config.setAutoCommit(true);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.setLeakDetectionThreshold(60000);

            // Mejor para MySQL / MariaDB
            config.setConnectionTestQuery("SELECT 1");

            dataSource = new HikariDataSource(config);

            // Probar conexión
            try (Connection conn = dataSource.getConnection()) {
                System.out.println("Conexión a la base de datos establecida correctamente");
            }

        } catch (IOException | SQLException e) {
            throw new RuntimeException("Error al inicializar la base de datos", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource no inicializado");
        }
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Conexiones del pool cerradas");
        }
    }
}
