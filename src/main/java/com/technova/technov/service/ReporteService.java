package com.technova.technov.service;

import com.technova.technov.domain.dto.ProductoDto;
import com.technova.technov.domain.dto.UsuarioDto;
import com.technova.technov.domain.dto.VentaDto;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReporteService {

    public byte[] generarPdfProductos(List<ProductoDto> productos) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        
        // Título
        contentStream.beginText();
        contentStream.setFont(fontBold, 16);
        contentStream.newLineAtOffset(50, 750);
        contentStream.showText("REPORTE DE PRODUCTOS");
        contentStream.endText();

        // Fecha
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.newLineAtOffset(50, 730);
        contentStream.showText("Fecha: " + fecha);
        contentStream.endText();

        // Encabezados de tabla
        contentStream.beginText();
        contentStream.setFont(fontBold, 10);
        contentStream.newLineAtOffset(50, 700);
        contentStream.showText("ID");
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(80, 700);
        contentStream.showText("Nombre");
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(250, 700);
        contentStream.showText("Categoría");
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(350, 700);
        contentStream.showText("Marca");
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(450, 700);
        contentStream.showText("Precio");
        contentStream.endText();

        // Datos
        int y = 680;
        for (ProductoDto producto : productos) {
            if (y < 50) {
                contentStream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                y = 750;
            }

            contentStream.beginText();
            contentStream.setFont(font, 9);
            contentStream.newLineAtOffset(50, y);
            contentStream.showText(String.valueOf(producto.getId()));
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(80, y);
            contentStream.showText(producto.getNombre() != null ? producto.getNombre() : "N/A");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(250, y);
            contentStream.showText(producto.getCaracteristica() != null && producto.getCaracteristica().getCategoria() != null 
                    ? producto.getCaracteristica().getCategoria() : "N/A");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(350, y);
            contentStream.showText(producto.getCaracteristica() != null && producto.getCaracteristica().getMarca() != null 
                    ? producto.getCaracteristica().getMarca() : "N/A");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(450, y);
            contentStream.showText(producto.getCaracteristica() != null && producto.getCaracteristica().getPrecioVenta() != null 
                    ? producto.getCaracteristica().getPrecioVenta().toString() : "N/A");
            contentStream.endText();

            y -= 20;
        }

        contentStream.close();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        document.close();

        return outputStream.toByteArray();
    }

    public byte[] generarExcelProductos(List<ProductoDto> productos) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        // Estilo para encabezados
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Crear encabezados
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Nombre", "Categoría", "Marca", "Precio Venta", "Stock"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Llenar datos
        int rowNum = 1;
        for (ProductoDto producto : productos) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(producto.getId());
            row.createCell(1).setCellValue(producto.getNombre() != null ? producto.getNombre() : "N/A");
            row.createCell(2).setCellValue(producto.getCaracteristica() != null && producto.getCaracteristica().getCategoria() != null 
                    ? producto.getCaracteristica().getCategoria() : "N/A");
            row.createCell(3).setCellValue(producto.getCaracteristica() != null && producto.getCaracteristica().getMarca() != null 
                    ? producto.getCaracteristica().getMarca() : "N/A");
            row.createCell(4).setCellValue(producto.getCaracteristica() != null && producto.getCaracteristica().getPrecioVenta() != null 
                    ? producto.getCaracteristica().getPrecioVenta().doubleValue() : 0);
            row.createCell(5).setCellValue(producto.getStock() != null ? producto.getStock() : 0);
        }

        // Autoajustar columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    public byte[] generarExcelUsuarios(List<UsuarioDto> usuarios) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Usuarios");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Nombre", "Email", "Rol", "Tipo Documento", "Número Documento"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (UsuarioDto usuario : usuarios) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(usuario.getId());
            row.createCell(1).setCellValue(usuario.getName() != null ? usuario.getName() : "N/A");
            row.createCell(2).setCellValue(usuario.getEmail() != null ? usuario.getEmail() : "N/A");
            row.createCell(3).setCellValue(usuario.getRole() != null ? usuario.getRole() : "N/A");
            row.createCell(4).setCellValue(usuario.getDocumentType() != null ? usuario.getDocumentType() : "N/A");
            row.createCell(5).setCellValue(usuario.getDocumentNumber() != null ? usuario.getDocumentNumber() : "N/A");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    public byte[] generarExcelVentas(List<VentaDto> ventas) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Ventas");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Usuario ID", "Fecha", "Total", "Items"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (VentaDto venta : ventas) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(venta.getVentaId() != null ? venta.getVentaId() : 0);
            row.createCell(1).setCellValue(venta.getUsuarioId() != null ? venta.getUsuarioId() : 0);
            row.createCell(2).setCellValue(venta.getFechaVenta() != null ? venta.getFechaVenta().toString() : "N/A");
            row.createCell(3).setCellValue(venta.getTotal() != null ? venta.getTotal().doubleValue() : 0);
            row.createCell(4).setCellValue(venta.getItems() != null ? venta.getItems().size() : 0);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    public byte[] generarPdfUsuarios(List<UsuarioDto> usuarios) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        
        // Título
        contentStream.beginText();
        contentStream.setFont(fontBold, 16);
        contentStream.newLineAtOffset(50, 750);
        contentStream.showText("REPORTE DE USUARIOS");
        contentStream.endText();

        // Fecha
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.newLineAtOffset(50, 730);
        contentStream.showText("Fecha: " + fecha);
        contentStream.endText();

        // Encabezados
        contentStream.beginText();
        contentStream.setFont(fontBold, 10);
        contentStream.newLineAtOffset(50, 700);
        contentStream.showText("ID");
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(80, 700);
        contentStream.showText("Nombre");
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(250, 700);
        contentStream.showText("Email");
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(400, 700);
        contentStream.showText("Rol");
        contentStream.endText();

        // Datos
        int y = 680;
        for (UsuarioDto usuario : usuarios) {
            if (y < 50) {
                contentStream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                y = 750;
            }

            contentStream.beginText();
            contentStream.setFont(font, 9);
            contentStream.newLineAtOffset(50, y);
            contentStream.showText(String.valueOf(usuario.getId()));
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(80, y);
            String nombre = usuario.getName() != null ? usuario.getName() : "N/A";
            contentStream.showText(nombre.length() > 20 ? nombre.substring(0, 20) : nombre);
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(250, y);
            String email = usuario.getEmail() != null ? usuario.getEmail() : "N/A";
            contentStream.showText(email.length() > 25 ? email.substring(0, 25) : email);
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(400, y);
            contentStream.showText(usuario.getRole() != null ? usuario.getRole() : "N/A");
            contentStream.endText();

            y -= 20;
        }

        contentStream.close();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        document.close();

        return outputStream.toByteArray();
    }

    public byte[] generarPdfVentas(List<VentaDto> ventas) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        
        // Título
        contentStream.beginText();
        contentStream.setFont(fontBold, 16);
        contentStream.newLineAtOffset(50, 750);
        contentStream.showText("REPORTE DE VENTAS");
        contentStream.endText();

        // Fecha
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.newLineAtOffset(50, 730);
        contentStream.showText("Fecha: " + fecha);
        contentStream.endText();

        // Encabezados
        contentStream.beginText();
        contentStream.setFont(fontBold, 10);
        contentStream.newLineAtOffset(50, 700);
        contentStream.showText("ID");
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(80, 700);
        contentStream.showText("Usuario ID");
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(170, 700);
        contentStream.showText("Fecha");
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(300, 700);
        contentStream.showText("Total");
        contentStream.endText();

        // Datos
        int y = 680;
        for (VentaDto venta : ventas) {
            if (y < 50) {
                contentStream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                y = 750;
            }

            contentStream.beginText();
            contentStream.setFont(font, 9);
            contentStream.newLineAtOffset(50, y);
            contentStream.showText(String.valueOf(venta.getVentaId() != null ? venta.getVentaId() : "N/A"));
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(80, y);
            contentStream.showText(String.valueOf(venta.getUsuarioId() != null ? venta.getUsuarioId() : "N/A"));
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(170, y);
            contentStream.showText(venta.getFechaVenta() != null ? venta.getFechaVenta().toString() : "N/A");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(300, y);
            contentStream.showText(venta.getTotal() != null ? venta.getTotal().toString() : "N/A");
            contentStream.endText();

            y -= 20;
        }

        contentStream.close();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        document.close();

        return outputStream.toByteArray();
    }
}
