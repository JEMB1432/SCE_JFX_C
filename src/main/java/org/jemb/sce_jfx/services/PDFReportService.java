package org.jemb.sce_jfx.services;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.jemb.sce_jfx.controllers.EnrollmentController;
import org.jemb.sce_jfx.controllers.EvaluationTypeController;
import org.jemb.sce_jfx.controllers.GradeController;
import org.jemb.sce_jfx.models.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFReportService {
    private final EnrollmentController enrollmentController;
    private final GradeController gradeController;
    private final EvaluationTypeController evaluationTypeController;

    public PDFReportService() {
        this.enrollmentController = new EnrollmentController();
        this.gradeController = new GradeController();
        this.evaluationTypeController = new EvaluationTypeController();
    }

    /**
     * Genera un reporte PDF de calificaciones de una materia
     */
    public File generateSubjectGradesReport(TeacherSubject teacherSubject, User teacher) throws FileNotFoundException {
        String fileName = "Reporte_" + teacherSubject.getSubject().getSubjectCode() + "_" 
                + teacherSubject.getAcademicYear() + "_Sem" + teacherSubject.getSemester() + "_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        
        File file = new File(fileName);
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(50, 50, 50, 50);

        // Encabezado
        addHeader(document, teacherSubject, teacher);

        // Obtener inscripciones
        List<Enrollment> enrollments = enrollmentController.getEnrollmentsBySubject(teacherSubject.getSubjectId());
        
        // Filtrar por periodo
        List<Enrollment> filteredEnrollments = enrollments.stream()
                .filter(e -> e.getAcademicYear().equals(teacherSubject.getAcademicYear()) &&
                        e.getSemester() == teacherSubject.getSemester())
                .toList();

        // Obtener tipos de evaluación
        List<EvaluationType> evaluationTypes = evaluationTypeController
                .getEvaluationTypesBySubject(teacherSubject.getSubjectId());

        // Tabla de calificaciones
        addGradesTable(document, filteredEnrollments, evaluationTypes);

        // Estadísticas
        addStatistics(document, filteredEnrollments, evaluationTypes);

        // Pie de página
        addFooter(document);

        document.close();
        return file;
    }

    private void addHeader(Document document, TeacherSubject teacherSubject, User teacher) {
        Paragraph title = new Paragraph("REPORTE DE CALIFICACIONES")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(title);

        Paragraph subjectInfo = new Paragraph(
                "Materia: " + teacherSubject.getSubject().getName() + 
                " (" + teacherSubject.getSubject().getSubjectCode() + ")")
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(subjectInfo);

        Paragraph periodInfo = new Paragraph(
                "Periodo: " + teacherSubject.getAcademicYear() + " - Semestre " + teacherSubject.getSemester())
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(periodInfo);

        Paragraph teacherInfo = new Paragraph(
                "Profesor: " + teacher.getFirstName() + " " + teacher.getLastName())
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(teacherInfo);
    }

    private void addGradesTable(Document document, List<Enrollment> enrollments, 
                                List<EvaluationType> evaluationTypes) {
        if (enrollments.isEmpty()) {
            Paragraph noData = new Paragraph("No hay estudiantes inscritos en esta materia.")
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20);
            document.add(noData);
            return;
        }

        // Calcular número de columnas: Código, Nombre, Evaluaciones, Nota Final
        int numColumns = 2 + evaluationTypes.size() + 1;
        
        // Crear array dinámico de anchos de columna
        float[] columnWidths = new float[numColumns];
        columnWidths[0] = 1.0f; // Código
        columnWidths[1] = 2.5f; // Nombre
        for (int i = 2; i < numColumns - 1; i++) {
            columnWidths[i] = 1.2f; // Evaluaciones
        }
        columnWidths[numColumns - 1] = 1.0f; // Nota Final
        
        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                .useAllAvailableWidth()
                .setMarginTop(20);

        // Encabezados
        Cell headerCell = new Cell().setBackgroundColor(ColorConstants.GRAY)
                .setPadding(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold();
        
        table.addHeaderCell(headerCell.clone(false).add(new Paragraph("Código")));
        table.addHeaderCell(headerCell.clone(false).add(new Paragraph("Estudiante")));
        
        for (EvaluationType evalType : evaluationTypes) {
            table.addHeaderCell(headerCell.clone(false).add(new Paragraph(evalType.getName())));
        }
        
        table.addHeaderCell(headerCell.clone(false).add(new Paragraph("Nota Final")));

        // Filas de datos
        for (Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();
            String code = student != null ? student.getStudentCode() : "N/A";
            String name = student != null ? student.getFullName() : "N/A";

            table.addCell(new Cell().setPadding(5).add(new Paragraph(code)));
            table.addCell(new Cell().setPadding(5).add(new Paragraph(name)));

            // Calificaciones por tipo de evaluación
            for (EvaluationType evalType : evaluationTypes) {
                Grade grade = findGrade(enrollment.getId(), evalType.getId());
                String score = grade != null && grade.getScore() != null 
                        ? String.format("%.2f", grade.getScore()) 
                        : "-";
                table.addCell(new Cell().setPadding(5)
                        .setTextAlignment(TextAlignment.CENTER)
                        .add(new Paragraph(score)));
            }

            // Nota final
            double finalGrade = gradeController.calculateFinalGrade(enrollment.getId());
            String finalGradeStr = finalGrade > 0 ? String.format("%.2f", finalGrade) : "-";
            Cell finalCell = new Cell().setPadding(5)
                    .setTextAlignment(TextAlignment.CENTER)
                    .add(new Paragraph(finalGradeStr));
            if (finalGrade > 0) {
                if (finalGrade >= 70) {
                    finalCell.setBackgroundColor(ColorConstants.GREEN);
                } else {
                    finalCell.setBackgroundColor(ColorConstants.RED);
                }
            }
            table.addCell(finalCell);
        }

        document.add(table);
    }

    private void addStatistics(Document document, List<Enrollment> enrollments, 
                              List<EvaluationType> evaluationTypes) {
        if (enrollments.isEmpty()) {
            return;
        }

        document.add(new Paragraph("\n").setMarginTop(20));

        Paragraph statsTitle = new Paragraph("ESTADÍSTICAS")
                .setFontSize(16)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(statsTitle);

        // Calcular estadísticas
        int totalStudents = enrollments.size();
        int studentsWithGrades = 0;
        double totalFinalGrade = 0;
        int passed = 0;
        int failed = 0;

        for (Enrollment enrollment : enrollments) {
            double finalGrade = gradeController.calculateFinalGrade(enrollment.getId());
            if (finalGrade > 0) {
                studentsWithGrades++;
                totalFinalGrade += finalGrade;
                if (finalGrade >= 70) {
                    passed++;
                } else {
                    failed++;
                }
            }
        }

        double averageGrade = studentsWithGrades > 0 ? totalFinalGrade / studentsWithGrades : 0;

        // Tabla de estadísticas
        Table statsTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setMarginTop(10);

        addStatRow(statsTable, "Total de Estudiantes", String.valueOf(totalStudents));
        addStatRow(statsTable, "Estudiantes Calificados", String.valueOf(studentsWithGrades));
        addStatRow(statsTable, "Promedio General", String.format("%.2f", averageGrade));
        addStatRow(statsTable, "Aprobados (≥70)", String.valueOf(passed));
        addStatRow(statsTable, "Reprobados (<70)", String.valueOf(failed));
        addStatRow(statsTable, "Tasa de Aprobación", 
                studentsWithGrades > 0 ? String.format("%.1f%%", (passed * 100.0 / studentsWithGrades)) : "0%");

        document.add(statsTable);
    }

    private void addStatRow(Table table, String label, String value) {
        Cell labelCell = new Cell().setPadding(8).add(new Paragraph(label).setBold());
        Cell valueCell = new Cell().setPadding(8).add(new Paragraph(value));
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addFooter(Document document) {
        Paragraph footer = new Paragraph(
                "Generado el: " + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30)
                .setFontColor(ColorConstants.GRAY);
        document.add(footer);
    }

    private Grade findGrade(String enrollmentId, String evaluationTypeId) {
        try {
            var grades = gradeController.getGradesByEnrollment(enrollmentId);
            return grades.stream()
                    .filter(g -> g.getEvaluationTypeId().equals(evaluationTypeId))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}

