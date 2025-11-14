package org.jemb.sce_jfx;

import org.jemb.sce_jfx.config.DatabaseConfig;
import org.jemb.sce_jfx.dao.StudentDAO;
import org.jemb.sce_jfx.dao.SubjectDAO;
import org.jemb.sce_jfx.dao.UserDAO;
import org.jemb.sce_jfx.models.Student;
import org.jemb.sce_jfx.models.Subject;
import org.jemb.sce_jfx.models.User;
import org.jemb.sce_jfx.services.AuthService;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class Main {
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("  SISTEMA DE CONTROL DE ESTUDIANTES");
        System.out.println("  PRUEBAS DE FUNCIONALIDAD");
        System.out.println("==========================================\n");

        try {
            // Inicializar conexión a la BD
            System.out.println("[1] Inicializando conexión a la base de datos...");
            DatabaseConfig.initialize();
            testPass("Conexión a la base de datos establecida");

            // Pruebas de BCrypt
            testBCrypt();

            // Pruebas de UserDAO
            testUserDAO();

            // Pruebas de AuthService
            testAuthService();

            // Pruebas de StudentDAO
            testStudentDAO();

            // Pruebas de SubjectDAO
            testSubjectDAO();

            // Resumen final
            printSummary();

        } catch (Exception e) {
            System.err.println("\nERROR CRÍTICO: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cerrar pool
            DatabaseConfig.close();
            System.out.println("\n[FIN] Conexiones cerradas.");
        }
    }

    // ==================== PRUEBAS DE BCRYPT ====================
    private static void testBCrypt() {
        System.out.println("\n--- PRUEBAS DE BCRYPT ---");

        // Test 1: Generar hash
        String password = "admin123";
        String hash = AuthService.hashPassword(password);
        assert hash != null && hash.startsWith("$2a$") : "Hash BCrypt debe comenzar con $2a$";
        testPass("Generación de hash BCrypt");

        // Test 2: Verificar hash
        boolean isValid = BCrypt.checkpw(password, hash);
        assert isValid : "Verificación de hash debe ser exitosa";
        testPass("Verificación de hash BCrypt");

        // Test 3: Hash incorrecto
        boolean isInvalid = BCrypt.checkpw("wrongpassword", hash);
        assert !isInvalid : "Hash incorrecto debe fallar";
        testPass("Rechazo de contraseña incorrecta");

        // Test 4: Verificar hash del admin en BD
        String adminHash = "$2a$10$b/SzUIjsPGgvNNhesfaDSeNnAtM0j4gQuqWKioqO4Ad4fqyVNRtaC";
        String[] testPasswords = {"admin123", "admin", "password", "123456"};
        boolean foundMatch = false;
        for (String testPwd : testPasswords) {
            if (BCrypt.checkpw(testPwd, adminHash)) {
                System.out.println("  ✓ Hash del admin corresponde a: '" + testPwd + "'");
                foundMatch = true;
                break;
            }
        }
        if (foundMatch) {
            testPass("Hash del admin verificado");
        } else {
            testFail("Hash del admin no coincide con contraseñas comunes");
        }
    }

    // ==================== PRUEBAS DE USERDAO ====================
    private static void testUserDAO() {
        System.out.println("\n--- PRUEBAS DE USERDAO ---");

        UserDAO userDAO = new UserDAO();

        // Test 1: Crear usuario de prueba
        String testEmail = "test.user@example.com";
        String testPassword = "test123";
        String testHash = AuthService.hashPassword(testPassword);
        
        User testUser = new User();
        testUser.setId(java.util.UUID.randomUUID().toString());
        testUser.setEmail(testEmail);
        testUser.setPasswordHash(testHash);
        testUser.setRole("teacher");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setActive(true);

        // Limpiar si existe
        userDAO.findByEmail(testEmail).ifPresent(u -> userDAO.delete(u.getId()));

        User saved = userDAO.save(testUser);
        assert saved != null : "Usuario debe guardarse correctamente";
        testPass("Guardar usuario");

        // Test 2: Buscar por email
        var foundOpt = userDAO.findByEmail(testEmail);
        assert foundOpt.isPresent() : "Usuario debe encontrarse por email";
        User found = foundOpt.get();
        assert found.getEmail().equals(testEmail) : "Email debe coincidir";
        testPass("Buscar usuario por email");

        // Test 3: Buscar por ID
        var foundByIdOpt = userDAO.findById(testUser.getId());
        assert foundByIdOpt.isPresent() : "Usuario debe encontrarse por ID";
        testPass("Buscar usuario por ID");

        // Test 4: Actualizar usuario
        found.setLastName("Updated");
        User updated = userDAO.update(found);
        assert updated != null && updated.getLastName().equals("Updated") : "Usuario debe actualizarse";
        testPass("Actualizar usuario");

        // Test 5: Actualizar último login
        userDAO.updateLastLogin(found.getId());
        var afterLogin = userDAO.findById(found.getId());
        assert afterLogin.isPresent() && afterLogin.get().getLastLogin() != null : "Last login debe actualizarse";
        testPass("Actualizar último login");

        // Test 6: Listar todos los usuarios
        List<User> allUsers = userDAO.findAll();
        assert allUsers != null && !allUsers.isEmpty() : "Debe haber usuarios en la BD";
        testPass("Listar todos los usuarios (" + allUsers.size() + " encontrados)");

        // Limpiar
        userDAO.delete(testUser.getId());
        testPass("Eliminar usuario de prueba");
    }

    // ==================== PRUEBAS DE AUTHSERVICE ====================
    private static void testAuthService() {
        System.out.println("\n--- PRUEBAS DE AUTHSERVICE ---");

        AuthService authService = new AuthService();
        UserDAO userDAO = new UserDAO();

        // Crear usuario de prueba para login
        String testEmail = "login.test@example.com";
        String testPassword = "login123";
        String testHash = AuthService.hashPassword(testPassword);

        User loginUser = new User();
        loginUser.setId(java.util.UUID.randomUUID().toString());
        loginUser.setEmail(testEmail);
        loginUser.setPasswordHash(testHash);
        loginUser.setRole("teacher");
        loginUser.setFirstName("Login");
        loginUser.setLastName("Test");
        loginUser.setActive(true);

        // Limpiar si existe
        userDAO.findByEmail(testEmail).ifPresent(u -> userDAO.delete(u.getId()));

        userDAO.save(loginUser);

        // Test 1: Login exitoso
        User authenticated = authService.login(testEmail, testPassword);
        assert authenticated != null : "Login debe ser exitoso";
        assert authenticated.getEmail().equals(testEmail) : "Email debe coincidir";
        assert authService.isAuthenticated() : "Usuario debe estar autenticado";
        testPass("Login exitoso");

        // Test 2: Obtener usuario actual
        User current = authService.getCurrentUser();
        assert current != null && current.getEmail().equals(testEmail) : "Usuario actual debe estar disponible";
        testPass("Obtener usuario actual");

        // Test 3: Login con contraseña incorrecta
        authService.logout();
        User failed = authService.login(testEmail, "wrongpassword");
        assert failed == null : "Login con contraseña incorrecta debe fallar";
        assert !authService.isAuthenticated() : "No debe estar autenticado";
        testPass("Rechazo de contraseña incorrecta");

        // Test 4: Login con email inexistente
        User notFound = authService.login("nonexistent@example.com", testPassword);
        assert notFound == null : "Login con email inexistente debe fallar";
        testPass("Rechazo de email inexistente");

        // Test 5: Login con usuario inactivo
        loginUser.setActive(false);
        userDAO.update(loginUser);
        User inactive = authService.login(testEmail, testPassword);
        assert inactive == null : "Login con usuario inactivo debe fallar";
        testPass("Rechazo de usuario inactivo");

        // Reactivar para limpieza
        loginUser.setActive(true);
        userDAO.update(loginUser);

        // Test 6: Logout
        authService.login(testEmail, testPassword);
        authService.logout();
        assert !authService.isAuthenticated() : "Después de logout no debe estar autenticado";
        assert authService.getCurrentUser() == null : "Usuario actual debe ser null";
        testPass("Logout");

        // Limpiar
        userDAO.delete(loginUser.getId());
    }

    // ==================== PRUEBAS DE STUDENTDAO ====================
    private static void testStudentDAO() {
        System.out.println("\n--- PRUEBAS DE STUDENTDAO ---");

        StudentDAO studentDAO = new StudentDAO();

        // Test 1: Crear estudiante
        Student testStudent = new Student();
        testStudent.setId(java.util.UUID.randomUUID().toString());
        testStudent.setStudentCode("TEST001");
        testStudent.setFirstName("Juan");
        testStudent.setLastName("Pérez");
        testStudent.setEmail("juan.perez@test.com");
        testStudent.setStatus("active");

        // Limpiar si existe
        studentDAO.findByStudentCode("TEST001").ifPresent(s -> studentDAO.delete(s.getId()));

        Student saved = studentDAO.save(testStudent);
        assert saved != null : "Estudiante debe guardarse";
        testPass("Guardar estudiante");

        // Test 2: Buscar por código
        var foundOpt = studentDAO.findByStudentCode("TEST001");
        assert foundOpt.isPresent() : "Estudiante debe encontrarse por código";
        testPass("Buscar estudiante por código");

        // Test 3: Buscar por email
        var foundByEmailOpt = studentDAO.findByEmail("juan.perez@test.com");
        assert foundByEmailOpt.isPresent() : "Estudiante debe encontrarse por email";
        testPass("Buscar estudiante por email");

        // Test 4: Actualizar estudiante
        Student found = foundOpt.get();
        found.setPhone("1234567890");
        Student updated = studentDAO.update(found);
        assert updated != null && updated.getPhone() != null : "Estudiante debe actualizarse";
        testPass("Actualizar estudiante");

        // Test 5: Listar estudiantes activos
        List<Student> activeStudents = studentDAO.findByStatus("active");
        assert activeStudents != null : "Debe retornar lista de estudiantes activos";
        testPass("Listar estudiantes activos (" + activeStudents.size() + " encontrados)");

        // Test 6: Contar estudiantes
        long count = studentDAO.count();
        assert count > 0 : "Debe haber estudiantes en la BD";
        testPass("Contar estudiantes (" + count + " total)");

        // Limpiar
        studentDAO.delete(testStudent.getId());
        testPass("Eliminar estudiante de prueba");
    }

    // ==================== PRUEBAS DE SUBJECTDAO ====================
    private static void testSubjectDAO() {
        System.out.println("\n--- PRUEBAS DE SUBJECTDAO ---");

        SubjectDAO subjectDAO = new SubjectDAO();

        // Test 1: Crear materia
        Subject testSubject = new Subject();
        testSubject.setId(java.util.UUID.randomUUID().toString());
        testSubject.setSubjectCode("MAT001");
        testSubject.setName("Matemáticas I");
        testSubject.setCredits(4);
        testSubject.setStatus("active");

        // Limpiar si existe
        subjectDAO.findBySubjectCode("MAT001").ifPresent(s -> subjectDAO.delete(s.getId()));

        Subject saved = subjectDAO.save(testSubject);
        assert saved != null : "Materia debe guardarse";
        testPass("Guardar materia");

        // Test 2: Buscar por código
        var foundOpt = subjectDAO.findBySubjectCode("MAT001");
        assert foundOpt.isPresent() : "Materia debe encontrarse por código";
        testPass("Buscar materia por código");

        // Test 3: Actualizar materia
        Subject found = foundOpt.get();
        found.setDescription("Curso de matemáticas básicas");
        Subject updated = subjectDAO.update(found);
        assert updated != null && updated.getDescription() != null : "Materia debe actualizarse";
        testPass("Actualizar materia");

        // Test 4: Listar materias activas
        List<Subject> activeSubjects = subjectDAO.findByStatus("active");
        assert activeSubjects != null : "Debe retornar lista de materias activas";
        testPass("Listar materias activas (" + activeSubjects.size() + " encontradas)");

        // Test 5: Contar materias
        long count = subjectDAO.count();
        assert count > 0 : "Debe haber materias en la BD";
        testPass("Contar materias (" + count + " total)");

        // Limpiar
        subjectDAO.delete(testSubject.getId());
        testPass("Eliminar materia de prueba");
    }

    // ==================== UTILIDADES DE PRUEBA ====================
    private static void testPass(String testName) {
        testsPassed++;
        System.out.println("  ✓ " + testName);
    }

    private static void testFail(String testName) {
        testsFailed++;
        System.out.println("  ✗ " + testName);
    }

    private static void printSummary() {
        System.out.println("\n==========================================");
        System.out.println("  RESUMEN DE PRUEBAS");
        System.out.println("==========================================");
        System.out.println("  Pruebas exitosas: " + testsPassed);
        System.out.println("  Pruebas fallidas: " + testsFailed);
        System.out.println("  Total: " + (testsPassed + testsFailed));
        
        if (testsFailed == 0) {
            System.out.println("\nTODAS LAS PRUEBAS PASARON");
        } else {
            System.out.println("\nALGUNAS PRUEBAS FALLARON");
        }
        System.out.println("==========================================");
    }
}
