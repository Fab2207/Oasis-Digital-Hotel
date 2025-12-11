package com.gestion.hotelera.controller;

import com.gestion.hotelera.service.ReservaService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class ReportesController {

    private final ReservaService reservaService;

    @GetMapping("/api/ingresos")
    public ResponseEntity<List<Map<String, Object>>> getIngresos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(reservaService.getIngresosPorPeriodo(fechaInicio, fechaFin));
    }

    @GetMapping("/api/ocupacion")
    public ResponseEntity<List<Map<String, Object>>> getOcupacion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(reservaService.getOcupacionDiariaPorPeriodo(fechaInicio, fechaFin));
    }

    @GetMapping(value = "/exportar-pdf", produces = "text/html")
    public ResponseEntity<String> exportarPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        List<Map<String, Object>> ingresos = reservaService.getIngresosPorPeriodo(fechaInicio, fechaFin);

        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Reporte de Ingresos</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; padding: 20px; }");
        html.append("h1 { color: #333; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        html.append("th { background-color: #f2f2f2; }");
        html.append(".total { font-weight: bold; font-size: 1.2em; margin-top: 20px; }");
        html.append("</style>");
        html.append("</head><body>");

        html.append("<h1>Reporte de Ingresos</h1>");
        html.append("<p>Desde: ").append(fechaInicio).append(" Hasta: ").append(fechaFin).append("</p>");

        html.append("<table>");
        html.append("<thead><tr><th>Fecha</th><th>Ingresos (S/.)</th></tr></thead>");
        html.append("<tbody>");

        double total = 0;
        for (Map<String, Object> fila : ingresos) {
            String fecha = (String) fila.get("fecha");
            Double monto = (Double) fila.get("ingresos");
            total += monto;
            html.append("<tr>");
            html.append("<td>").append(fecha).append("</td>");
            html.append("<td>S/. ").append(String.format("%.2f", monto)).append("</td>");
            html.append("</tr>");
        }

        html.append("</tbody></table>");

        html.append("<div class='total'>Ingresos Totales: S/. ").append(String.format("%.2f", total)).append("</div>");

        html.append("<script>window.onload = function() { window.print(); }</script>");

        html.append("</body></html>");

        return ResponseEntity.ok(html.toString());
    }
}
