package com.hosteleria.service;

import com.hosteleria.controller.HosteleriaController;
import com.hosteleria.model.Empleado;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

// CAMBIAR POR ITEXT7, GENERA DOCUMENTOS DE FORMA MAS SIMPLE

/**
 * Generación de documentos PDF sobre empleados: informes, certificados, resúmenes.
 */
public class GeneradorDocumentosService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Font FONT_TITULO = new Font(Font.HELVETICA, 18, Font.BOLD);
    private static final Font FONT_SUBTITULO = new Font(Font.HELVETICA, 12, Font.BOLD);
    private static final Font FONT_NORMAL = new Font(Font.HELVETICA, 11, Font.NORMAL);

    private final HosteleriaController ctrl = new HosteleriaController();
    private final EstadisticasEmpleadoService estadisticas = new EstadisticasEmpleadoService();

    /**
     * Genera un informe PDF con la ficha del empleado y estadísticas en un rango de fechas.
     */
    public Path generarInformeEmpleado(int idEmpleado, LocalDate desde, LocalDate hasta, Path directorioSalida) throws IOException, DocumentException {
        Optional<Empleado> opt = ctrl.getEmpleadoCompleto(idEmpleado);
        if (opt.isEmpty()) throw new IllegalArgumentException("Empleado no encontrado: " + idEmpleado);
        Empleado e = opt.get();
        EstadisticasEmpleadoService.EstadisticasEmpleadoDTO stats = estadisticas.estadisticasEmpleado(idEmpleado, desde, hasta);
        String nombreArchivo = "informe_empleado_" + idEmpleado + "_" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".pdf";
        Path ruta = directorioSalida.resolve(nombreArchivo);

        try (FileOutputStream fos = new FileOutputStream(ruta.toFile())) {
            Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(doc, fos);
            doc.open();

            doc.add(new Paragraph("INFORME DE EMPLEADO", FONT_TITULO));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Datos del empleado", FONT_SUBTITULO));
            doc.add(new Paragraph("Nombre: " + e.getNombre() + " " + e.getApellidos(), FONT_NORMAL));
            doc.add(new Paragraph("DNI: " + (e.getDni() != null ? e.getDni() : "—"), FONT_NORMAL));
            doc.add(new Paragraph("Email: " + (e.getEmail() != null ? e.getEmail() : "—"), FONT_NORMAL));
            doc.add(new Paragraph("Teléfono: " + (e.getTelefono() != null ? e.getTelefono() : "—"), FONT_NORMAL));
            doc.add(new Paragraph("Fecha contratación: " + (e.getFechaContratacion() != null ? e.getFechaContratacion().format(FMT) : "—"), FONT_NORMAL));
            doc.add(new Paragraph("Área: " + (e.getArea() != null ? e.getArea().getNombre() : "—"), FONT_NORMAL));
            doc.add(new Paragraph("Puesto: " + (e.getPuesto() != null ? e.getPuesto().getTitulo() : "—"), FONT_NORMAL));
            doc.add(new Paragraph("Tipo contrato: " + (e.getTipoContrato() != null ? e.getTipoContrato().name() : "—"), FONT_NORMAL));
            doc.add(new Paragraph("Estado: " + (e.getEstado() != null ? e.getEstado().name() : "—"), FONT_NORMAL));
            doc.add(new Paragraph(" "));

            if (stats != null) {
                doc.add(new Paragraph("Estadísticas del periodo " + desde.format(FMT) + " - " + hasta.format(FMT), FONT_SUBTITULO));
                doc.add(new Paragraph("Horas trabajadas: " + String.format("%.2f", stats.getTotalHorasTrabajadas()), FONT_NORMAL));
                doc.add(new Paragraph("Horas extra: " + String.format("%.2f", stats.getTotalHorasExtra()), FONT_NORMAL));
                doc.add(new Paragraph("Nº fichajes: " + stats.getNumeroFichajes(), FONT_NORMAL));
                doc.add(new Paragraph("Días de ausencia: " + stats.getDiasAusencia(), FONT_NORMAL));
                doc.add(new Paragraph("Nº evaluaciones: " + stats.getNumeroEvaluaciones() + " (puntuación media: " + String.format("%.1f", stats.getPuntuacionMedia()) + ")", FONT_NORMAL));
                doc.add(new Paragraph("Total propinas: " + (stats.getTotalPropinas() != null ? stats.getTotalPropinas() + " €" : "—"), FONT_NORMAL));
                doc.add(new Paragraph("Total nóminas (neto): " + (stats.getTotalNominas() != null ? stats.getTotalNominas() + " €" : "—"), FONT_NORMAL));
            }

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Documento generado el " + LocalDate.now().format(FMT) + ".", new Font(Font.HELVETICA, 9, Font.ITALIC)));
            doc.close();
        }
        return ruta;
    }

    /**
     * Genera un certificado de trabajo (documento simple para el empleado).
     */
    public Path generarCertificadoTrabajo(int idEmpleado, Path directorioSalida) throws IOException, DocumentException {
        Optional<Empleado> opt = ctrl.getEmpleadoCompleto(idEmpleado);
        if (opt.isEmpty()) throw new IllegalArgumentException("Empleado no encontrado: " + idEmpleado);
        Empleado e = opt.get();
        String nombreArchivo = "certificado_trabajo_" + idEmpleado + "_" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".pdf";
        Path ruta = directorioSalida.resolve(nombreArchivo);

        try (FileOutputStream fos = new FileOutputStream(ruta.toFile())) {
            Document doc = new Document(PageSize.A4, 60, 60, 60, 60);
            PdfWriter.getInstance(doc, fos);
            doc.open();

            doc.add(new Paragraph("CERTIFICADO DE TRABAJO", new Font(Font.HELVETICA, 20, Font.BOLD)));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Se certifica que D/Dña. " + e.getNombre() + " " + e.getApellidos()
                + ", con DNI " + (e.getDni() != null ? e.getDni() : "—")
                + ", está vinculado/a a esta empresa desde el "
                + (e.getFechaContratacion() != null ? e.getFechaContratacion().format(FMT) : "—")
                + " en el puesto de " + (e.getPuesto() != null ? e.getPuesto().getTitulo() : "—")
                + (e.getArea() != null ? ", en el área de " + e.getArea().getNombre() : "")
                + ".", FONT_NORMAL));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("A petición del interesado/a y para los fines que estime convenientes, se expide el presente certificado.", FONT_NORMAL));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Fecha: " + LocalDate.now().format(FMT), FONT_NORMAL));
            doc.close();
        }
        return ruta;
    }

    /**
     * Genera un resumen global de la plantilla en PDF (estadísticas globales).
     */
    public Path generarResumenPlantilla(Path directorioSalida) throws IOException, DocumentException {
        EstadisticasEmpleadoService.EstadisticasGlobalesDTO g = estadisticas.estadisticasGlobales();
        String nombreArchivo = "resumen_plantilla_" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".pdf";
        Path ruta = directorioSalida.resolve(nombreArchivo);

        try (FileOutputStream fos = new FileOutputStream(ruta.toFile())) {
            Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(doc, fos);
            doc.open();

            doc.add(new Paragraph("RESUMEN DE PLANTILLA", FONT_TITULO));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Total empleados: " + g.getTotalEmpleados(), FONT_NORMAL));
            doc.add(new Paragraph("Activos: " + g.getActivos() + " | Baja definitiva: " + g.getBajaDefinitiva()
                + " | Vacaciones: " + g.getVacaciones() + " | Baja temporal: " + g.getBajaTemporal(), FONT_NORMAL));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Empleados por área:", FONT_SUBTITULO));
            for (var entry : g.getEmpleadosPorArea().entrySet()) {
                doc.add(new Paragraph("  • " + entry.getKey() + ": " + entry.getValue(), FONT_NORMAL));
            }
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Empleados por tipo de contrato:", FONT_SUBTITULO));
            for (var entry : g.getEmpleadosPorTipoContrato().entrySet()) {
                doc.add(new Paragraph("  • " + entry.getKey() + ": " + entry.getValue(), FONT_NORMAL));
            }
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Documento generado el " + LocalDate.now().format(FMT) + ".", new Font(Font.HELVETICA, 9, Font.ITALIC)));
            doc.close();
        }
        return ruta;
    }
}
