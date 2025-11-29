package org.jemb.sce_jfx.controllers;

import org.jemb.sce_jfx.dao.UserDAO;
import org.jemb.sce_jfx.models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;

public class UserController {
    private final UserDAO userDAO;
    private static final int BCRYPT_ROUNDS = 10;

    public UserController() {
        this.userDAO = new UserDAO();
    }

    // Crear nuevo usuario
    public User createUser(String email, String password, String role, String firstName, String lastName) {
        // Validar email único
        Optional<User> existing = userDAO.findByEmail(email);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado");
        }

        // Validar rol
        if (!isValidRole(role)) {
            throw new IllegalArgumentException("Rol inválido. Use: admin, teacher o assistant");
        }

        // Validar password
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        if (password.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }

        // Hashear contraseña
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS));

        // Crear usuario
        User user = new User(email.trim(), passwordHash, role, firstName.trim(), lastName.trim());
        User saved = userDAO.save(user);

        if (saved == null) {
            throw new RuntimeException("Error al crear el usuario");
        }

        return saved;
    }

    // Obtener usuario por ID
    public Optional<User> getUserById(String id) {
        return userDAO.findById(id);
    }

    // Obtener todos los usuarios
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    // Obtener usuarios por rol
    public List<User> getUsersByRole(String role) {
        return userDAO.findByRole(role);
    }

    // Obtener usuario por email
    public Optional<User> getUserByEmail(String email) {
        return userDAO.findByEmail(email);
    }

    // Actualizar usuario
    public User updateUser(User user) {
        // Validar que el usuario exista
        if (!userDAO.findById(user.getId()).isPresent()) {
            throw new IllegalArgumentException("El usuario no existe");
        }

        // Validar rol
        if (!isValidRole(user.getRole())) {
            throw new IllegalArgumentException("Rol inválido. Use: admin, teacher o assistant");
        }

        // Validar email único (excepto el propio usuario)
        Optional<User> existingEmail = userDAO.findByEmail(user.getEmail());
        if (existingEmail.isPresent() && !existingEmail.get().getId().equals(user.getId())) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado por otro usuario");
        }

        User updated = userDAO.update(user);

        if (updated == null) {
            throw new RuntimeException("Error al actualizar el usuario");
        }

        return updated;
    }

    // Actualizar usuario con nueva contraseña
    public User updateUserWithPassword(User user, String newPassword) {
        // Validar contraseña
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }

        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }

        // Hashear nueva contraseña
        String passwordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
        user.setPasswordHash(passwordHash);

        return updateUser(user);
    }

    // Cambiar contraseña de un usuario
    public User changePassword(String userId, String newPassword) {
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("El usuario no existe");
        }

        User user = userOpt.get();
        return updateUserWithPassword(user, newPassword);
    }

    // Eliminar usuario
    public void deleteUser(String id) {
        if (!userDAO.findById(id).isPresent()) {
            throw new IllegalArgumentException("El usuario no existe");
        }

        userDAO.delete(id);
    }

    // Cambiar estado de usuario
    public User changeUserStatus(String id, boolean isActive) {
        Optional<User> userOpt = userDAO.findById(id);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("El usuario no existe");
        }

        User user = userOpt.get();
        user.setActive(isActive);

        return updateUser(user);
    }

    // Activar usuario
    public User activateUser(String id) {
        return changeUserStatus(id, true);
    }

    // Desactivar usuario
    public User deactivateUser(String id) {
        return changeUserStatus(id, false);
    }

    // Validar si el rol es válido
    private boolean isValidRole(String role) {
        return role != null && (role.equals("admin") || role.equals("teacher") || role.equals("assistant"));
    }

    // Verificar si un email ya existe
    public boolean emailExists(String email) {
        return userDAO.findByEmail(email).isPresent();
    }

    // Verificar si un email ya existe (excepto para un usuario específico)
    public boolean emailExistsForOtherUser(String email, String userId) {
        Optional<User> existing = userDAO.findByEmail(email);
        return existing.isPresent() && !existing.get().getId().equals(userId);
    }

    // Validar contraseña con BCrypt
    public boolean validatePassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    // Obtener el rol en formato legible
    public static String getRoleDisplayName(String role) {
        switch (role) {
            case "admin":
                return "Administrador";
            case "teacher":
                return "Profesor";
            case "assistant":
                return "Asistente";
            default:
                return role;
        }
    }
}
