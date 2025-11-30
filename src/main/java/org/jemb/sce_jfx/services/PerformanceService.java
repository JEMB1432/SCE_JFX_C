package org.jemb.sce_jfx.services;

import org.jemb.sce_jfx.config.DatabaseConfig;
import org.jemb.sce_jfx.dao.DatabaseUtils;
import org.jemb.sce_jfx.models.StudentPerformanceData;
import org.jemb.sce_jfx.models.SubjectPerformance;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class PerformanceService {

    public List<StudentPerformanceData> getStudentPerformance(String studentId) {
        List<StudentPerformanceData> performanceList = new ArrayList<>();

        String sql = """
                    SELECT
                        s.id AS student_id,
                        s.first_name,
                        s.last_name,
                        s.student_code,
                        CONCAT(s.first_name, ' ', s.last_name) AS full_name,
                        sub.id AS subject_id,
                        sub.name AS subject_name,
                        sub.subject_code AS subject_code,
                        e.id AS enrollment_id,
                        e.academic_year,
                        e.semester,
                        et.id AS evaluation_type_id,
                        et.name AS evaluation_type,
                        et.weight,
                        g.id AS grade_id,
                        g.score,
                        g.graded_at,
                        g.comments,
                        (g.score * et.weight / 100.0) AS weighted_score
                    FROM students s
                    INNER JOIN enrollments e ON s.id = e.student_id
                    INNER JOIN subjects sub ON e.subject_id = sub.id
                    INNER JOIN grades g ON e.id = g.enrollment_id
                    INNER JOIN evaluation_types et ON g.evaluation_type_id = et.id
                    WHERE s.id = ? AND g.score IS NOT NULL
                    ORDER BY e.academic_year DESC, e.semester DESC, sub.name, g.graded_at
                """;

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                StudentPerformanceData data = new StudentPerformanceData();
                data.setStudentId(rs.getString("student_id"));
                data.setFirstName(rs.getString("first_name"));
                data.setLastName(rs.getString("last_name"));
                data.setStudentCode(rs.getString("student_code"));
                data.setFullName(rs.getString("full_name"));
                data.setSubjectId(rs.getString("subject_id"));
                data.setSubjectName(rs.getString("subject_name"));
                data.setSubjectCode(rs.getString("subject_code"));
                data.setEnrollmentId(rs.getString("enrollment_id"));
                data.setAcademicYear(rs.getString("academic_year"));
                data.setSemester(rs.getInt("semester"));
                data.setEvaluationTypeId(rs.getString("evaluation_type_id"));
                data.setEvaluationType(rs.getString("evaluation_type"));
                data.setWeight(rs.getDouble("weight"));
                data.setGradeId(rs.getString("grade_id"));
                data.setScore(rs.getDouble("score"));

                Timestamp gradedAt = rs.getTimestamp("graded_at");
                if (gradedAt != null) {
                    data.setGradedAt(DatabaseUtils.toLocalDateTime(gradedAt));
                }

                data.setComments(rs.getString("comments"));
                data.setWeightedScore(rs.getDouble("weighted_score"));

                performanceList.add(data);
            }
        } catch (SQLException e) {
            System.err.println("Error getting student performance: " + e.getMessage());
            e.printStackTrace();
        }

        return performanceList;
    }

    /**
     * Agrupa el rendimiento por materia
     */
    public List<SubjectPerformance> getSubjectSummary(String studentId) {
        List<StudentPerformanceData> performanceData = getStudentPerformance(studentId);

        Map<String, SubjectPerformance> subjectMap = new HashMap<>();

        for (StudentPerformanceData data : performanceData) {
            String key = data.getSubjectId();

            SubjectPerformance subject = subjectMap.computeIfAbsent(key,
                    k -> new SubjectPerformance(data.getSubjectName(), data.getSubjectCode()));

            subject.setAcademicYear(data.getAcademicYear());
            subject.setSemester(data.getSemester());
            subject.setEvaluationCount(subject.getEvaluationCount() + 1);
            subject.setGradedEvaluations(subject.getGradedEvaluations() + 1);
        }

        // Calcular promedios y notas finales
        for (SubjectPerformance subject : subjectMap.values()) {
            String subjectId = findSubjectIdByName(performanceData, subject.getSubjectName());
            List<StudentPerformanceData> subjectGrades = performanceData.stream()
                    .filter(d -> d.getSubjectId().equals(subjectId))
                    .collect(Collectors.toList());

            // Promedio simple
            double avgScore = subjectGrades.stream()
                    .mapToDouble(StudentPerformanceData::getScore)
                    .average()
                    .orElse(0.0);
            subject.setAverageScore(avgScore);

            // Nota final ponderada
            double finalScore = subjectGrades.stream()
                    .mapToDouble(StudentPerformanceData::getWeightedScore)
                    .sum();
            subject.setFinalScore(finalScore);
        }

        return new ArrayList<>(subjectMap.values());
    }

    /**
     * Agrupa el rendimiento por tipo de evaluación
     */
    public Map<String, Double> getEvaluationTypeSummary(String studentId) {
        List<StudentPerformanceData> performanceData = getStudentPerformance(studentId);

        Map<String, List<Double>> evaluationScores = performanceData.stream()
                .collect(Collectors.groupingBy(
                        StudentPerformanceData::getEvaluationType,
                        Collectors.mapping(StudentPerformanceData::getScore, Collectors.toList())));

        Map<String, Double> averages = new LinkedHashMap<>();
        evaluationScores.forEach((type, scores) -> {
            double avg = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            averages.put(type, avg);
        });

        return averages;
    }

    /**
     * Obtiene el progreso temporal de calificaciones
     */
    public List<StudentPerformanceData> getProgressOverTime(String studentId) {
        List<StudentPerformanceData> performanceData = getStudentPerformance(studentId);

        // Ordenar por fecha
        performanceData.sort(Comparator.comparing(StudentPerformanceData::getGradedAt));

        return performanceData;
    }

    /**
     * Verifica si un estudiante tiene calificaciones
     */
    public boolean hasGrades(String studentId) {
        return !getStudentPerformance(studentId).isEmpty();
    }

    /**
     * Obtiene el promedio general del estudiante
     */
    public double getOverallAverage(String studentId) {
        List<SubjectPerformance> subjects = getSubjectSummary(studentId);

        if (subjects.isEmpty()) {
            return 0.0;
        }

        return subjects.stream()
                .mapToDouble(SubjectPerformance::getFinalScore)
                .average()
                .orElse(0.0);
    }

    // Método auxiliar
    private String findSubjectIdByName(List<StudentPerformanceData> data, String subjectName) {
        return data.stream()
                .filter(d -> d.getSubjectName().equals(subjectName))
                .findFirst()
                .map(StudentPerformanceData::getSubjectId)
                .orElse("");
    }
}
