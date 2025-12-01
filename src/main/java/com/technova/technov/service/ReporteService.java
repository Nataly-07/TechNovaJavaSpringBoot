package com.technova.technov.service;

import com.technova.technov.domain.dto.CompraDto;
import com.technova.technov.domain.dto.CompraDetalleDto;
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
import java.math.BigDecimal;
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

    public byte[] generarFacturaCompra(CompraDto compra, UsuarioDto usuario) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        
        // Encabezado con gradiente Technova (color púrpura)
        // Fondo del encabezado
        contentStream.setNonStrokingColor(102.0f / 255.0f, 126.0f / 255.0f, 234.0f / 255.0f); // #667eea
        contentStream.addRect(0, 700, 595, 100);
        contentStream.fill();
        
        // Título de la factura en blanco
        contentStream.setNonStrokingColor(1.0f, 1.0f, 1.0f); // Blanco
        contentStream.beginText();
        contentStream.setFont(fontBold, 24);
        contentStream.newLineAtOffset(50, 750);
        contentStream.showText("FACTURA DE COMPRA");
        contentStream.endText();

        // Información de la empresa
        contentStream.beginText();
        contentStream.setFont(fontBold, 14);
        contentStream.newLineAtOffset(50, 720);
        contentStream.showText("TECHNOVA");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 11);
        contentStream.newLineAtOffset(50, 705);
        contentStream.showText("Sistema de Gestión de Inventario");
        contentStream.endText();
        
        // Número de factura en caja destacada
        contentStream.setNonStrokingColor(1.0f, 1.0f, 1.0f);
        contentStream.addRect(400, 720, 145, 50);
        contentStream.fill();
        contentStream.setNonStrokingColor(102.0f / 255.0f, 126.0f / 255.0f, 234.0f / 255.0f);
        contentStream.beginText();
        contentStream.setFont(font, 10);
        contentStream.newLineAtOffset(410, 755);
        contentStream.showText("Número de Factura");
        contentStream.endText();
        contentStream.beginText();
        contentStream.setFont(fontBold, 18);
        contentStream.newLineAtOffset(410, 735);
        contentStream.showText("#" + (compra.getCompraId() != null ? compra.getCompraId() : "N/A"));
        contentStream.endText();

        // Información del cliente (texto negro)
        contentStream.setNonStrokingColor(0.0f, 0.0f, 0.0f); // Negro
        int yPos = 680;
        
        // Caja de información del cliente
        contentStream.setNonStrokingColor(248.0f / 255.0f, 249.0f / 255.0f, 250.0f / 255.0f); // #f8f9fa
        contentStream.addRect(50, 600, 250, 70);
        contentStream.fill();
        contentStream.setNonStrokingColor(102.0f / 255.0f, 126.0f / 255.0f, 234.0f / 255.0f); // #667eea
        contentStream.setLineWidth(4);
        contentStream.addRect(50, 600, 250, 70);
        contentStream.stroke();
        
        contentStream.setNonStrokingColor(102.0f / 255.0f, 126.0f / 255.0f, 234.0f / 255.0f);
        contentStream.beginText();
        contentStream.setFont(fontBold, 12);
        contentStream.newLineAtOffset(60, 660);
        contentStream.showText("Información del Cliente");
        contentStream.endText();

        contentStream.setNonStrokingColor(0.0f, 0.0f, 0.0f); // Negro para texto
        if (usuario != null) {
            contentStream.beginText();
            contentStream.setFont(font, 10);
            contentStream.newLineAtOffset(60, 640);
            contentStream.showText("Nombre: " + (usuario.getName() != null ? usuario.getName() : "N/A"));
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(font, 10);
            contentStream.newLineAtOffset(60, 625);
            contentStream.showText("Email: " + (usuario.getEmail() != null ? usuario.getEmail() : "N/A"));
            contentStream.endText();

            if (usuario.getPhone() != null) {
                contentStream.beginText();
                contentStream.setFont(font, 10);
                contentStream.newLineAtOffset(60, 610);
                contentStream.showText("Teléfono: " + usuario.getPhone());
                contentStream.endText();
            }
        }

        // Caja de información de la compra
        contentStream.setNonStrokingColor(248.0f / 255.0f, 249.0f / 255.0f, 250.0f / 255.0f); // #f8f9fa
        contentStream.addRect(320, 600, 250, 70);
        contentStream.fill();
        contentStream.setNonStrokingColor(118.0f / 255.0f, 75.0f / 255.0f, 162.0f / 255.0f); // #764ba2
        contentStream.setLineWidth(4);
        contentStream.addRect(320, 600, 250, 70);
        contentStream.stroke();
        
        contentStream.setNonStrokingColor(118.0f / 255.0f, 75.0f / 255.0f, 162.0f / 255.0f);
        contentStream.beginText();
        contentStream.setFont(fontBold, 12);
        contentStream.newLineAtOffset(330, 660);
        contentStream.showText("Información de la Compra");
        contentStream.endText();

        contentStream.setNonStrokingColor(0.0f, 0.0f, 0.0f);
        if (compra.getFechaCompra() != null) {
            String fechaCompra = compra.getFechaCompra().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            contentStream.beginText();
            contentStream.setFont(font, 10);
            contentStream.newLineAtOffset(330, 640);
            contentStream.showText("Fecha: " + fechaCompra);
            contentStream.endText();
        }

        if (compra.getEstado() != null) {
            contentStream.beginText();
            contentStream.setFont(font, 10);
            contentStream.newLineAtOffset(330, 625);
            contentStream.showText("Estado: " + compra.getEstado());
            contentStream.endText();
        }
        
        yPos = 580;

        // Tabla de productos
        contentStream.setNonStrokingColor(0.0f, 0.0f, 0.0f);
        contentStream.beginText();
        contentStream.setFont(fontBold, 13);
        contentStream.newLineAtOffset(50, yPos);
        contentStream.showText("Productos Comprados");
        contentStream.endText();

        yPos -= 25;
        
        // Encabezado de tabla con fondo Technova
        contentStream.setNonStrokingColor(102.0f / 255.0f, 126.0f / 255.0f, 234.0f / 255.0f); // #667eea
        contentStream.addRect(50, yPos - 15, 500, 20);
        contentStream.fill();
        
        contentStream.setNonStrokingColor(1.0f, 1.0f, 1.0f); // Texto blanco
        contentStream.beginText();
        contentStream.setFont(fontBold, 10);
        contentStream.newLineAtOffset(55, yPos - 5);
        contentStream.showText("Producto");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(fontBold, 10);
        contentStream.newLineAtOffset(250, yPos - 5);
        contentStream.showText("Cantidad");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(fontBold, 10);
        contentStream.newLineAtOffset(320, yPos - 5);
        contentStream.showText("Precio Unit.");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(fontBold, 10);
        contentStream.newLineAtOffset(420, yPos - 5);
        contentStream.showText("Subtotal");
        contentStream.endText();

        yPos -= 25;
        contentStream.setNonStrokingColor(0.0f, 0.0f, 0.0f); // Negro para texto de productos

        // Productos
        boolean alternate = false;
        if (compra.getItems() != null && !compra.getItems().isEmpty()) {
            for (CompraDetalleDto detalle : compra.getItems()) {
                if (yPos < 100) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPos = 750;
                    alternate = false;
                }

                // Fondo alternado para filas
                if (alternate) {
                    contentStream.setNonStrokingColor(248.0f / 255.0f, 249.0f / 255.0f, 250.0f / 255.0f); // #f8f9fa
                    contentStream.addRect(50, yPos - 12, 500, 15);
                    contentStream.fill();
                }
                alternate = !alternate;

                String nombreProducto = detalle.getNombreProducto() != null ? detalle.getNombreProducto() : "N/A";
                if (nombreProducto.length() > 30) {
                    nombreProducto = nombreProducto.substring(0, 27) + "...";
                }

                contentStream.setNonStrokingColor(0.0f, 0.0f, 0.0f);
                contentStream.beginText();
                contentStream.setFont(font, 9);
                contentStream.newLineAtOffset(55, yPos - 5);
                contentStream.showText(nombreProducto);
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(font, 9);
                contentStream.newLineAtOffset(250, yPos - 5);
                contentStream.showText(String.valueOf(detalle.getCantidad() != null ? detalle.getCantidad() : 0));
                contentStream.endText();

                String precioUnit = detalle.getPrecio() != null ? String.format("%.0f", detalle.getPrecio().doubleValue()) : "0";
                contentStream.beginText();
                contentStream.setFont(font, 9);
                contentStream.newLineAtOffset(320, yPos - 5);
                contentStream.showText("$" + precioUnit);
                contentStream.endText();

                BigDecimal subtotal = BigDecimal.ZERO;
                if (detalle.getPrecio() != null && detalle.getCantidad() != null) {
                    subtotal = detalle.getPrecio().multiply(BigDecimal.valueOf(detalle.getCantidad()));
                }
                contentStream.setNonStrokingColor(39.0f / 255.0f, 174.0f / 255.0f, 96.0f / 255.0f); // Verde para subtotal
                contentStream.beginText();
                contentStream.setFont(fontBold, 9);
                contentStream.newLineAtOffset(420, yPos - 5);
                contentStream.showText("$" + String.format("%.0f", subtotal.doubleValue()));
                contentStream.endText();

                yPos -= 15;
            }
        }

        // Total destacado
        yPos -= 20;
        contentStream.setNonStrokingColor(0.0f, 0.0f, 0.0f);
        contentStream.setLineWidth(2);
        contentStream.moveTo(50, yPos);
        contentStream.lineTo(550, yPos);
        contentStream.stroke();

        yPos -= 25;
        // Caja destacada para el total
        contentStream.setNonStrokingColor(248.0f / 255.0f, 249.0f / 255.0f, 250.0f / 255.0f); // #f8f9fa
        contentStream.addRect(300, yPos - 25, 250, 40);
        contentStream.fill();
        contentStream.setNonStrokingColor(102.0f / 255.0f, 126.0f / 255.0f, 234.0f / 255.0f); // #667eea
        contentStream.setLineWidth(3);
        contentStream.addRect(300, yPos - 25, 250, 40);
        contentStream.stroke();
        
        contentStream.setNonStrokingColor(102.0f / 255.0f, 126.0f / 255.0f, 234.0f / 255.0f);
        contentStream.beginText();
        contentStream.setFont(fontBold, 11);
        contentStream.newLineAtOffset(310, yPos);
        contentStream.showText("TOTAL A PAGAR");
        contentStream.endText();

        String total = compra.getTotal() != null ? String.format("%.0f", compra.getTotal().doubleValue()) : "0";
        contentStream.beginText();
        contentStream.setFont(fontBold, 18);
        contentStream.newLineAtOffset(310, yPos - 18);
        contentStream.showText("$" + total);
        contentStream.endText();

        // Pie de página
        yPos -= 40;
        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.newLineAtOffset(50, yPos);
        contentStream.showText("Gracias por su compra!");
        contentStream.endText();

        yPos -= 15;
        String fechaGeneracion = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.newLineAtOffset(50, yPos);
        contentStream.showText("Factura generada el: " + fechaGeneracion);
        contentStream.endText();

        contentStream.close();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        document.close();

        return outputStream.toByteArray();
    }
}
