package org.jemb.sce_jfx.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilidad para probar y generar hashes BCrypt
 * Ejecutar este main para verificar qué contraseña corresponde al hash en la BD
 */
public class BCryptTest {
    public static void main(String[] args) {
        // Hash del usuario admin en la base de datos
        String storedHash = "$2a$10$b/SzUIjsPGgvNNhesfaDSeNnAtM0j4gQuqWKioqO4Ad4fqyVNRtaC";
        
        // Contraseñas comunes a probar
        String[] passwordsToTest = {
            "admin123",
            "admin",
            "password",
            "123456",
            "sistema",
            "admin@sistema.edu"
        };
        
        System.out.println("Probando hash BCrypt: " + storedHash);
        System.out.println("=====================================");
        
        for (String password : passwordsToTest) {
            boolean matches = BCrypt.checkpw(password, storedHash);
            System.out.println("Contraseña: '" + password + "' -> " + (matches ? "✓ CORRECTO" : "✗ Incorrecto"));
        }
        
        System.out.println("\n=====================================");
        System.out.println("Para generar un nuevo hash:");
        System.out.println("String hash = BCrypt.hashpw(\"tu_contraseña\", BCrypt.gensalt(10));");
        
        // Generar un hash de ejemplo
        String exampleHash = BCrypt.hashpw("admin123", BCrypt.gensalt(10));
        System.out.println("\nEjemplo de hash generado para 'admin123':");
        System.out.println(exampleHash);
    }
}

