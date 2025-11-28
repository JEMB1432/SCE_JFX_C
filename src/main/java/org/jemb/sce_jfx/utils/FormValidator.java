package org.jemb.sce_jfx.utils;

import javafx.scene.Node;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Clase utilitaria para validación de formularios
 */
public class FormValidator {

    // Patrones de validación
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[0-9]{10}$|^\\+?[0-9\\s-]{10,15}$");

    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile(
            "^[A-Za-z0-9]+$");

    /**
     * Valida formato de email
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Valida formato de teléfono (10 dígitos o formato internacional)
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // El teléfono es opcional
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Valida que la fecha de nacimiento sea razonable
     * 
     * @param date Fecha de nacimiento
     * @return true si la fecha es válida (entre 15 y 100 años de edad)
     */
    public static boolean isValidDateOfBirth(LocalDate date) {
        if (date == null) {
            return true; // La fecha es opcional
        }

        LocalDate now = LocalDate.now();
        LocalDate minDate = now.minusYears(100); // Máximo 100 años
        LocalDate maxDate = now.minusYears(15); // Mínimo 15 años

        return !date.isBefore(minDate) && !date.isAfter(maxDate);
    }

    /**
     * Verifica si un string está vacío o es null
     */
    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    /**
     * Verifica si un string es alfanumérico
     */
    public static boolean isAlphanumeric(String text) {
        if (isEmpty(text)) {
            return false;
        }
        return ALPHANUMERIC_PATTERN.matcher(text).matches();
    }

    /**
     * Agrega clase CSS de error a un nodo
     */
    public static void addErrorStyle(Node node) {
        if (node != null && !node.getStyleClass().contains("error")) {
            node.getStyleClass().add("error");
        }
    }

    /**
     * Remueve clase CSS de error de un nodo
     */
    public static void removeErrorStyle(Node node) {
        if (node != null) {
            node.getStyleClass().remove("error");
        }
    }

    /**
     * Valida longitud mínima de un texto
     */
    public static boolean hasMinLength(String text, int minLength) {
        if (isEmpty(text)) {
            return false;
        }
        return text.trim().length() >= minLength;
    }

    /**
     * Valida longitud máxima de un texto
     */
    public static boolean hasMaxLength(String text, int maxLength) {
        if (isEmpty(text)) {
            return true;
        }
        return text.trim().length() <= maxLength;
    }

    /**
     * Obtiene mensaje de error para email inválido
     */
    public static String getEmailErrorMessage() {
        return "Por favor ingrese un correo electrónico válido";
    }

    /**
     * Obtiene mensaje de error para teléfono inválido
     */
    public static String getPhoneErrorMessage() {
        return "El teléfono debe tener 10 dígitos";
    }

    /**
     * Obtiene mensaje de error para fecha inválida
     */
    public static String getDateErrorMessage() {
        return "La edad debe estar entre 15 y 100 años";
    }

    /**
     * Obtiene mensaje de error para campo vacío
     */
    public static String getRequiredFieldErrorMessage(String fieldName) {
        return fieldName + " es obligatorio";
    }

    /**
     * Obtiene mensaje de error para campo alfanumérico
     */
    public static String getAlphanumericErrorMessage(String fieldName) {
        return fieldName + " solo puede contener letras y números";
    }
}
