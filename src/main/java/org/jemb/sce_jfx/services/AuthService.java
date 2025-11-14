package org.jemb.sce_jfx.services;

import org.jemb.sce_jfx.dao.UserDAO;
import org.jemb.sce_jfx.models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class AuthService {
    private final UserDAO userDAO;
    private User currentUser;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Autentica un usuario con email y contraseña usando BCrypt
     * @param email Email del usuario
     * @param password Contraseña en texto plano
     * @return Usuario autenticado o null si las credenciales son inválidas
     */
    public User login(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            return null;
        }

        Optional<User> userOpt = userDAO.findByEmail(email.trim());
        
        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();
        
        // Verificar que el usuario esté activo
        if (!user.isActive()) {
            return null;
        }

        // Verificar contraseña con BCrypt
        String storedHash = user.getPasswordHash();
        if (storedHash == null || !BCrypt.checkpw(password, storedHash)) {
            return null;
        }

        // Actualizar último login
        user.setLastLogin(java.time.LocalDateTime.now());
        userDAO.updateLastLogin(user.getId());

        this.currentUser = user;
        return user;
    }

    /**
     * Hashea una contraseña usando BCrypt
     * @param password Contraseña en texto plano
     * @return Hash BCrypt de la contraseña
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }

    /**
     * Obtiene el usuario actualmente autenticado
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Cierra la sesión del usuario actual
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Verifica si hay un usuario autenticado
     */
    public boolean isAuthenticated() {
        return currentUser != null;
    }
}


