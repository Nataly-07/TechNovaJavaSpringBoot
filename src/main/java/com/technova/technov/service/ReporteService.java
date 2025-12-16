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
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReporteService {

    /**
     * Carga el logo de Technova desde los recursos para PDF
     */
    private PDImageXObject cargarLogo(PDDocument document) throws IOException {
        try {
            ClassPathResource resource = new ClassPathResource("static/frontend/imagenes/logo technova.png");
            InputStream inputStream = resource.getInputStream();
            return PDImageXObject.createFromByteArray(document, inputStream.readAllBytes(), "logo-technova");
        } catch (Exception e) {
            System.err.println("Error al cargar el logo: " + e.getMessage());
            return null;
        }
    }

    /**
     * Carga el logo de Technova como bytes para Excel
     */
    private byte[] cargarLogoBytes() throws IOException {
        try {
            ClassPathResource resource = new ClassPathResource("static/frontend/imagenes/logo technova.png");
            InputStream inputStream = resource.getInputStream();
            return IOUtils.toByteArray(inputStream);
        } catch (Exception e) {
            System.err.println("Error al cargar el logo para Excel: " + e.getMessage());
            return null;
        }
    }

    /**
     * Agrega el logo a una hoja de Excel
     */
    private void agregarLogoAExcel(XSSFWorkbook workbook, XSSFSheet sheet, int rowIndex, int colIndex) {
        try {
            byte[] logoBytes = cargarLogoBytes();
            if (logoBytes == null) return;

            int pictureIdx = workbook.addPicture(logoBytes, Workbook.PICTURE_TYPE_PNG);
            
            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = new XSSFClientAnchor();
            
            // Posicionar el logo en la celda especificada
            anchor.setCol1(colIndex);
            anchor.setRow1(rowIndex);
            anchor.setCol2(colIndex + 2); // Ancho de 2 columnas
            anchor.setRow2(rowIndex + 2); // Alto de 2 filas
            
            XSSFPicture picture = drawing.createPicture(anchor, pictureIdx);
            
            // Ajustar el tamaño del logo (opcional, para que no sea muy grande)
            picture.resize(0.5); // Reducir al 50% del tamaño original
            
        } catch (Exception e) {
            System.err.println("Error al agregar logo a Excel: " + e.getMessage());
        }
    }

    public byte[] generarPdfProductos(List<ProductoDto> productos) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        
        // Cargar logo
        PDImageXObject logo = cargarLogo(document);
        
        // Encabezado con fondo Technova
        float headerColorR = 102.0f / 255.0f;
        float headerColorG = 126.0f / 255.0f;
        float headerColorB = 234.0f / 255.0f;
        
        contentStream.setNonStrokingColor(headerColorR, headerColorG, headerColorB);
        contentStream.addRect(0, 750, 595, 60);
        contentStream.fill();
        
        // Dibujar logo si está disponible
        if (logo != null) {
            float logoWidth = 50;
            float logoHeight = 50;
            float logoX = 50;
            float logoY = 760;
            contentStream.drawImage(logo, logoX, logoY, logoWidth, logoHeight);
        }
        
        // Título en blanco
        contentStream.setNonStrokingColor(1.0f, 1.0f, 1.0f);
        contentStream.beginText();
        contentStream.setFont(fontBold, 20);
        contentStream.newLineAtOffset(logo != null ? 110 : 50, 775);
        contentStream.showText("REPORTE DE PRODUCTOS");
        contentStream.endText();

        // Fecha
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        contentStream.beginText();
        contentStream.setFont(font, 11);
        contentStream.newLineAtOffset(50, 755);
        contentStream.showText("Fecha de generación: " + fecha);
        contentStream.endText();
        
        // Total de productos
        contentStream.beginText();
        contentStream.setFont(font, 11);
        contentStream.newLineAtOffset(50, 740);
        contentStream.showText("Total de productos: " + productos.size());
        contentStream.endText();

        // Encabezados de tabla con fondo
        float yHeader = 700;
        contentStream.setNonStrokingColor(headerColorR, headerColorG, headerColorB);
        contentStream.addRect(50, yHeader - 15, 500, 20);
        contentStream.fill();
        
        contentStream.setNonStrokingColor(1.0f, 1.0f, 1.0f);
        contentStream.beginText();
        contentStream.setFont(fontBold, 10);
        contentStream.newLineAtOffset(55, yHeader - 5);
        contentStream.showText("ID");
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(90, yHeader - 5);
        contentStream.showText("Nombre");
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(250, yHeader - 5);
        contentStream.showText("Categoría");
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(350, yHeader - 5);
        contentStream.showText("Marca");
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(450, yHeader - 5);
        contentStream.showText("Precio");
        contentStream.endText();

        // Línea separadora
        contentStream.setStrokingColor(headerColorR, headerColorG, headerColorB);
        contentStream.setLineWidth(1);
        contentStream.moveTo(50, yHeader - 15);
        contentStream.lineTo(550, yHeader - 15);
        contentStream.stroke();

        // Datos con filas alternadas
        float y = 680;
        boolean alternate = false;
        float lightGray = 248.0f / 255.0f;
        
        for (ProductoDto producto : productos) {
            if (y < 50) {
                contentStream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                y = 750;
                alternate = false;
            }

            // Fondo alternado
            if (alternate) {
                contentStream.setNonStrokingColor(lightGray, lightGray, lightGray);
                contentStream.addRect(50, y - 12, 500, 15);
                contentStream.fill();
            }
            alternate = !alternate;

            // Borde de fila
            contentStream.setStrokingColor(0.8f, 0.8f, 0.8f);
            contentStream.setLineWidth(0.5f);
            contentStream.addRect(50, y - 12, 500, 15);
            contentStream.stroke();

            contentStream.setNonStrokingColor(0.0f, 0.0f, 0.0f);
            contentStream.beginText();
            contentStream.setFont(font, 9);
            contentStream.newLineAtOffset(55, y - 5);
            contentStream.showText(String.valueOf(producto.getId()));
            contentStream.endText();

            String nombre = producto.getNombre() != null ? producto.getNombre() : "N/A";
            if (nombre.length() > 25) nombre = nombre.substring(0, 22) + "...";
            contentStream.beginText();
            contentStream.newLineAtOffset(90, y - 5);
            contentStream.showText(nombre);
            contentStream.endText();

            String categoria = producto.getCaracteristica() != null && producto.getCaracteristica().getCategoria() != null 
                    ? producto.getCaracteristica().getCategoria() : "N/A";
            if (categoria.length() > 15) categoria = categoria.substring(0, 12) + "...";
            contentStream.beginText();
            contentStream.newLineAtOffset(250, y - 5);
            contentStream.showText(categoria);
            contentStream.endText();

            String marca = producto.getCaracteristica() != null && producto.getCaracteristica().getMarca() != null 
                    ? producto.getCaracteristica().getMarca() : "N/A";
            if (marca.length() > 15) marca = marca.substring(0, 12) + "...";
            contentStream.beginText();
            contentStream.newLineAtOffset(350, y - 5);
            contentStream.showText(marca);
            contentStream.endText();

            String precio = producto.getCaracteristica() != null && producto.getCaracteristica().getPrecioVenta() != null 
                    ? "$" + String.format("%.0f", producto.getCaracteristica().getPrecioVenta().doubleValue()) : "N/A";
            contentStream.setNonStrokingColor(39.0f / 255.0f, 174.0f / 255.0f, 96.0f / 255.0f);
            contentStream.beginText();
            contentStream.setFont(fontBold, 9);
            contentStream.newLineAtOffset(450, y - 5);
            contentStream.showText(precio);
            contentStream.endText();

            y -= 15;
        }

        // Pie de página
        y -= 20;
        contentStream.setStrokingColor(0.7f, 0.7f, 0.7f);
        contentStream.setLineWidth(1);
        contentStream.moveTo(50, y);
        contentStream.lineTo(550, y);
        contentStream.stroke();
        
        y -= 15;
        contentStream.setNonStrokingColor(0.5f, 0.5f, 0.5f);
        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.newLineAtOffset(50, y);
        contentStream.showText("Reporte generado por Technova - Sistema de Gestión");
        contentStream.endText();

        contentStream.close();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        document.close();

        return outputStream.toByteArray();
    }

    public byte[] generarExcelProductos(List<ProductoDto> productos) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Productos");

        // Agregar logo en la primera fila
        Row logoRow = sheet.createRow(0);
        logoRow.setHeightInPoints(60);
        agregarLogoAExcel(workbook, sheet, 0, 0);
        
        // Crear fila de título
        Row titleRow = sheet.createRow(1);
        titleRow.setHeightInPoints(25);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REPORTE DE PRODUCTOS");
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

        // Estilo para encabezados con colores Technova
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());

        // Estilo para datos
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        dataStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        dataStyle.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        dataStyle.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Estilo para filas alternadas
        CellStyle alternateStyle = workbook.createCellStyle();
        alternateStyle.cloneStyleFrom(dataStyle);
        alternateStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        alternateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Estilo para precio (moneda)
        CellStyle priceStyle = workbook.createCellStyle();
        priceStyle.cloneStyleFrom(dataStyle);
        DataFormat format = workbook.createDataFormat();
        priceStyle.setDataFormat(format.getFormat("$#,##0"));
        priceStyle.setAlignment(HorizontalAlignment.RIGHT);
        Font priceFont = workbook.createFont();
        priceFont.setBold(true);
        priceFont.setColor(IndexedColors.DARK_GREEN.getIndex());
        priceStyle.setFont(priceFont);

        // Crear encabezados (ahora en la fila 2)
        Row headerRow = sheet.createRow(2);
        headerRow.setHeightInPoints(20);
        String[] headers = {"ID", "Nombre", "Categoría", "Marca", "Precio Venta", "Stock"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Llenar datos (empezando en la fila 3)
        int rowNum = 3;
        for (ProductoDto producto : productos) {
            Row row = sheet.createRow(rowNum++);
            row.setHeightInPoints(18);
            
            boolean isAlternate = (rowNum % 2 == 0);
            CellStyle currentStyle = isAlternate ? alternateStyle : dataStyle;
            
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(producto.getId());
            cell0.setCellStyle(currentStyle);
            
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(producto.getNombre() != null ? producto.getNombre() : "N/A");
            cell1.setCellStyle(currentStyle);
            
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(producto.getCaracteristica() != null && producto.getCaracteristica().getCategoria() != null 
                    ? producto.getCaracteristica().getCategoria() : "N/A");
            cell2.setCellStyle(currentStyle);
            
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(producto.getCaracteristica() != null && producto.getCaracteristica().getMarca() != null 
                    ? producto.getCaracteristica().getMarca() : "N/A");
            cell3.setCellStyle(currentStyle);
            
            Cell cell4 = row.createCell(4);
            double precio = producto.getCaracteristica() != null && producto.getCaracteristica().getPrecioVenta() != null 
                    ? producto.getCaracteristica().getPrecioVenta().doubleValue() : 0;
            cell4.setCellValue(precio);
            CellStyle priceCellStyle = workbook.createCellStyle();
            priceCellStyle.cloneStyleFrom(isAlternate ? alternateStyle : dataStyle);
            priceCellStyle.setDataFormat(format.getFormat("$#,##0"));
            priceCellStyle.setAlignment(HorizontalAlignment.RIGHT);
            Font priceFontCell = workbook.createFont();
            priceFontCell.setBold(true);
            priceFontCell.setColor(IndexedColors.DARK_GREEN.getIndex());
            priceCellStyle.setFont(priceFontCell);
            cell4.setCellStyle(priceCellStyle);
            
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(producto.getStock() != null ? producto.getStock() : 0);
            cell5.setCellStyle(currentStyle);
        }

        // Autoajustar columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    public byte[] generarExcelUsuarios(List<UsuarioDto> usuarios) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Usuarios");

        // Agregar logo en la primera fila
        Row logoRow = sheet.createRow(0);
        logoRow.setHeightInPoints(60);
        agregarLogoAExcel(workbook, sheet, 0, 0);
        
        // Crear fila de título
        Row titleRow = sheet.createRow(1);
        titleRow.setHeightInPoints(25);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REPORTE DE USUARIOS");
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

        // Estilo para encabezados
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());

        // Estilo para datos
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        dataStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        dataStyle.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        dataStyle.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Estilo para filas alternadas
        CellStyle alternateStyle = workbook.createCellStyle();
        alternateStyle.cloneStyleFrom(dataStyle);
        alternateStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        alternateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Estilo para rol (con colores)
        CellStyle rolAdminStyle = workbook.createCellStyle();
        rolAdminStyle.cloneStyleFrom(dataStyle);
        Font rolAdminFont = workbook.createFont();
        rolAdminFont.setBold(true);
        rolAdminFont.setColor(IndexedColors.RED.getIndex());
        rolAdminStyle.setFont(rolAdminFont);
        rolAdminStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle rolEmpleadoStyle = workbook.createCellStyle();
        rolEmpleadoStyle.cloneStyleFrom(dataStyle);
        Font rolEmpleadoFont = workbook.createFont();
        rolEmpleadoFont.setBold(true);
        rolEmpleadoFont.setColor(IndexedColors.BLUE.getIndex());
        rolEmpleadoStyle.setFont(rolEmpleadoFont);
        rolEmpleadoStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle rolClienteStyle = workbook.createCellStyle();
        rolClienteStyle.cloneStyleFrom(dataStyle);
        Font rolClienteFont = workbook.createFont();
        rolClienteFont.setBold(true);
        rolClienteFont.setColor(IndexedColors.DARK_GREEN.getIndex());
        rolClienteStyle.setFont(rolClienteFont);
        rolClienteStyle.setAlignment(HorizontalAlignment.CENTER);

        Row headerRow = sheet.createRow(2);
        headerRow.setHeightInPoints(20);
        String[] headers = {"ID", "Nombre", "Email", "Rol", "Tipo Documento", "Número Documento"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 3;
        for (UsuarioDto usuario : usuarios) {
            Row row = sheet.createRow(rowNum++);
            row.setHeightInPoints(18);
            
            boolean isAlternate = (rowNum % 2 == 0);
            CellStyle currentStyle = isAlternate ? alternateStyle : dataStyle;
            
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(usuario.getId());
            cell0.setCellStyle(currentStyle);
            
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(usuario.getName() != null ? usuario.getName() : "N/A");
            cell1.setCellStyle(currentStyle);
            
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(usuario.getEmail() != null ? usuario.getEmail() : "N/A");
            cell2.setCellStyle(currentStyle);
            
            Cell cell3 = row.createCell(3);
            String rol = usuario.getRole() != null ? usuario.getRole() : "N/A";
            cell3.setCellValue(rol.toUpperCase());
            // Aplicar estilo según rol
            if ("admin".equalsIgnoreCase(rol)) {
                CellStyle rolStyle = workbook.createCellStyle();
                rolStyle.cloneStyleFrom(isAlternate ? alternateStyle : dataStyle);
                rolStyle.setFont(rolAdminFont);
                rolStyle.setAlignment(HorizontalAlignment.CENTER);
                cell3.setCellStyle(rolStyle);
            } else if ("empleado".equalsIgnoreCase(rol)) {
                CellStyle rolStyle = workbook.createCellStyle();
                rolStyle.cloneStyleFrom(isAlternate ? alternateStyle : dataStyle);
                rolStyle.setFont(rolEmpleadoFont);
                rolStyle.setAlignment(HorizontalAlignment.CENTER);
                cell3.setCellStyle(rolStyle);
            } else {
                CellStyle rolStyle = workbook.createCellStyle();
                rolStyle.cloneStyleFrom(isAlternate ? alternateStyle : dataStyle);
                rolStyle.setFont(rolClienteFont);
                rolStyle.setAlignment(HorizontalAlignment.CENTER);
                cell3.setCellStyle(rolStyle);
            }
            
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(usuario.getDocumentType() != null ? usuario.getDocumentType() : "N/A");
            cell4.setCellStyle(currentStyle);
            
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(usuario.getDocumentNumber() != null ? usuario.getDocumentNumber() : "N/A");
            cell5.setCellStyle(currentStyle);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    public byte[] generarExcelVentas(List<VentaDto> ventas) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Ventas");

        // Agregar logo en la primera fila
        Row logoRow = sheet.createRow(0);
        logoRow.setHeightInPoints(60);
        agregarLogoAExcel(workbook, sheet, 0, 0);
        
        // Crear fila de título
        Row titleRow = sheet.createRow(1);
        titleRow.setHeightInPoints(25);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REPORTE DE VENTAS");
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setColor(IndexedColors.DARK_GREEN.getIndex());
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));

        // Estilo para encabezados
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());

        // Estilo para datos
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        dataStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        dataStyle.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        dataStyle.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Estilo para filas alternadas
        CellStyle alternateStyle = workbook.createCellStyle();
        alternateStyle.cloneStyleFrom(dataStyle);
        alternateStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        alternateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Estilo para total (moneda)
        DataFormat format = workbook.createDataFormat();
        CellStyle totalStyle = workbook.createCellStyle();
        totalStyle.cloneStyleFrom(dataStyle);
        totalStyle.setDataFormat(format.getFormat("$#,##0"));
        totalStyle.setAlignment(HorizontalAlignment.RIGHT);
        Font totalFont = workbook.createFont();
        totalFont.setBold(true);
        totalFont.setColor(IndexedColors.DARK_GREEN.getIndex());
        totalStyle.setFont(totalFont);

        // Estilo para fecha
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.cloneStyleFrom(dataStyle);
        dateStyle.setDataFormat(format.getFormat("dd/mm/yyyy"));

        Row headerRow = sheet.createRow(2);
        headerRow.setHeightInPoints(20);
        String[] headers = {"ID", "Usuario ID", "Fecha", "Items", "Total"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 3;
        for (VentaDto venta : ventas) {
            Row row = sheet.createRow(rowNum++);
            row.setHeightInPoints(18);
            
            boolean isAlternate = (rowNum % 2 == 0);
            CellStyle currentStyle = isAlternate ? alternateStyle : dataStyle;
            
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(venta.getVentaId() != null ? venta.getVentaId() : 0);
            cell0.setCellStyle(currentStyle);
            
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(venta.getUsuarioId() != null ? venta.getUsuarioId() : 0);
            cell1.setCellStyle(currentStyle);
            
            Cell cell2 = row.createCell(2);
            if (venta.getFechaVenta() != null) {
                cell2.setCellValue(venta.getFechaVenta().toString());
            } else {
                cell2.setCellValue("N/A");
            }
            CellStyle dateCellStyle = workbook.createCellStyle();
            dateCellStyle.cloneStyleFrom(isAlternate ? alternateStyle : dataStyle);
            dateCellStyle.setDataFormat(format.getFormat("dd/mm/yyyy"));
            cell2.setCellStyle(dateCellStyle);
            
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(venta.getItems() != null ? venta.getItems().size() : 0);
            cell3.setCellStyle(currentStyle);
            
            Cell cell4 = row.createCell(4);
            double total = venta.getTotal() != null ? venta.getTotal().doubleValue() : 0;
            cell4.setCellValue(total);
            CellStyle totalCellStyle = workbook.createCellStyle();
            totalCellStyle.cloneStyleFrom(isAlternate ? alternateStyle : dataStyle);
            totalCellStyle.setDataFormat(format.getFormat("$#,##0"));
            totalCellStyle.setAlignment(HorizontalAlignment.RIGHT);
            Font totalFontCell = workbook.createFont();
            totalFontCell.setBold(true);
            totalFontCell.setColor(IndexedColors.DARK_GREEN.getIndex());
            totalCellStyle.setFont(totalFontCell);
            cell4.setCellStyle(totalCellStyle);
        }

        // Agregar fila de totales
        Row totalRow = sheet.createRow(rowNum);
        totalRow.setHeightInPoints(22);
        
        Cell totalLabelCell = totalRow.createCell(3);
        totalLabelCell.setCellValue("TOTAL:");
        CellStyle totalLabelStyle = workbook.createCellStyle();
        totalLabelStyle.setFont(headerFont);
        totalLabelStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        totalLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        totalLabelStyle.setAlignment(HorizontalAlignment.RIGHT);
        totalLabelStyle.setBorderTop(BorderStyle.MEDIUM);
        totalLabelStyle.setBorderBottom(BorderStyle.MEDIUM);
        totalLabelStyle.setBorderLeft(BorderStyle.MEDIUM);
        totalLabelStyle.setBorderRight(BorderStyle.MEDIUM);
        totalLabelCell.setCellStyle(totalLabelStyle);
        
        BigDecimal totalGeneral = ventas.stream()
                .filter(v -> v.getTotal() != null)
                .map(VentaDto::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Cell totalValueCell = totalRow.createCell(4);
        totalValueCell.setCellValue(totalGeneral.doubleValue());
        CellStyle totalValueStyle = workbook.createCellStyle();
        totalValueStyle.setFont(headerFont);
        totalValueStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        totalValueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        totalValueStyle.setDataFormat(format.getFormat("$#,##0"));
        totalValueStyle.setAlignment(HorizontalAlignment.RIGHT);
        totalValueStyle.setBorderTop(BorderStyle.MEDIUM);
        totalValueStyle.setBorderBottom(BorderStyle.MEDIUM);
        totalValueStyle.setBorderLeft(BorderStyle.MEDIUM);
        totalValueStyle.setBorderRight(BorderStyle.MEDIUM);
        totalValueCell.setCellStyle(totalValueStyle);

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
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
        
        // Cargar logo
        PDImageXObject logo = cargarLogo(document);
        
        // Encabezado con fondo Technova
        float headerColorR = 118.0f / 255.0f;
        float headerColorG = 75.0f / 255.0f;
        float headerColorB = 162.0f / 255.0f;
        
        contentStream.setNonStrokingColor(headerColorR, headerColorG, headerColorB);
        contentStream.addRect(0, 750, 595, 60);
        contentStream.fill();
        
        // Dibujar logo si está disponible
        if (logo != null) {
            float logoWidth = 50;
            float logoHeight = 50;
            float logoX = 50;
            float logoY = 760;
            contentStream.drawImage(logo, logoX, logoY, logoWidth, logoHeight);
        }
        
        // Título en blanco
        contentStream.setNonStrokingColor(1.0f, 1.0f, 1.0f);
        contentStream.beginText();
        contentStream.setFont(fontBold, 20);
        contentStream.newLineAtOffset(logo != null ? 110 : 50, 775);
        contentStream.showText("REPORTE DE USUARIOS");
        contentStream.endText();

        // Fecha
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        contentStream.beginText();
        contentStream.setFont(font, 11);
        contentStream.newLineAtOffset(50, 755);
        contentStream.showText("Fecha de generación: " + fecha);
        contentStream.endText();
        
        // Total de usuarios
        contentStream.beginText();
        contentStream.setFont(font, 11);
        contentStream.newLineAtOffset(50, 740);
        contentStream.showText("Total de usuarios: " + usuarios.size());
        contentStream.endText();

        // Encabezados de tabla con fondo
        float yHeader = 700;
        contentStream.setNonStrokingColor(headerColorR, headerColorG, headerColorB);
        contentStream.addRect(50, yHeader - 15, 500, 20);
        contentStream.fill();
        
        contentStream.setNonStrokingColor(1.0f, 1.0f, 1.0f);
        contentStream.beginText();
        contentStream.setFont(fontBold, 10);
        contentStream.newLineAtOffset(55, yHeader - 5);
        contentStream.showText("ID");
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(90, yHeader - 5);
        contentStream.showText("Nombre");
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(250, yHeader - 5);
        contentStream.showText("Email");
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(400, yHeader - 5);
        contentStream.showText("Rol");
        contentStream.endText();

        // Línea separadora
        contentStream.setStrokingColor(headerColorR, headerColorG, headerColorB);
        contentStream.setLineWidth(1);
        contentStream.moveTo(50, yHeader - 15);
        contentStream.lineTo(550, yHeader - 15);
        contentStream.stroke();

        // Datos con filas alternadas
        float y = 680;
        boolean alternate = false;
        float lightGray = 248.0f / 255.0f;
        
        for (UsuarioDto usuario : usuarios) {
            if (y < 50) {
                contentStream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                y = 750;
                alternate = false;
            }

            // Fondo alternado
            if (alternate) {
                contentStream.setNonStrokingColor(lightGray, lightGray, lightGray);
                contentStream.addRect(50, y - 12, 500, 15);
                contentStream.fill();
            }
            alternate = !alternate;

            // Borde de fila
            contentStream.setStrokingColor(0.8f, 0.8f, 0.8f);
            contentStream.setLineWidth(0.5f);
            contentStream.addRect(50, y - 12, 500, 15);
            contentStream.stroke();

            contentStream.setNonStrokingColor(0.0f, 0.0f, 0.0f);
            contentStream.beginText();
            contentStream.setFont(font, 9);
            contentStream.newLineAtOffset(55, y - 5);
            contentStream.showText(String.valueOf(usuario.getId()));
            contentStream.endText();

            String nombre = usuario.getName() != null ? usuario.getName() : "N/A";
            if (nombre.length() > 25) nombre = nombre.substring(0, 22) + "...";
            contentStream.beginText();
            contentStream.newLineAtOffset(90, y - 5);
            contentStream.showText(nombre);
            contentStream.endText();

            String email = usuario.getEmail() != null ? usuario.getEmail() : "N/A";
            if (email.length() > 30) email = email.substring(0, 27) + "...";
            contentStream.beginText();
            contentStream.newLineAtOffset(250, y - 5);
            contentStream.showText(email);
            contentStream.endText();

            String rol = usuario.getRole() != null ? usuario.getRole() : "N/A";
            // Color según rol
            if ("admin".equalsIgnoreCase(rol)) {
                contentStream.setNonStrokingColor(231.0f / 255.0f, 76.0f / 255.0f, 60.0f / 255.0f);
            } else if ("empleado".equalsIgnoreCase(rol)) {
                contentStream.setNonStrokingColor(52.0f / 255.0f, 152.0f / 255.0f, 219.0f / 255.0f);
            } else {
                contentStream.setNonStrokingColor(39.0f / 255.0f, 174.0f / 255.0f, 96.0f / 255.0f);
            }
            contentStream.beginText();
            contentStream.setFont(fontBold, 9);
            contentStream.newLineAtOffset(400, y - 5);
            contentStream.showText(rol.toUpperCase());
            contentStream.endText();

            y -= 15;
        }

        // Pie de página
        y -= 20;
        contentStream.setStrokingColor(0.7f, 0.7f, 0.7f);
        contentStream.setLineWidth(1);
        contentStream.moveTo(50, y);
        contentStream.lineTo(550, y);
        contentStream.stroke();
        
        y -= 15;
        contentStream.setNonStrokingColor(0.5f, 0.5f, 0.5f);
        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.newLineAtOffset(50, y);
        contentStream.showText("Reporte generado por Technova - Sistema de Gestión");
        contentStream.endText();

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
        
        // Cargar logo
        PDImageXObject logo = cargarLogo(document);
        
        // Encabezado con fondo Technova
        float headerColorR = 39.0f / 255.0f;
        float headerColorG = 174.0f / 255.0f;
        float headerColorB = 96.0f / 255.0f;
        
        contentStream.setNonStrokingColor(headerColorR, headerColorG, headerColorB);
        contentStream.addRect(0, 750, 595, 60);
        contentStream.fill();
        
        // Dibujar logo si está disponible
        if (logo != null) {
            float logoWidth = 50;
            float logoHeight = 50;
            float logoX = 50;
            float logoY = 760;
            contentStream.drawImage(logo, logoX, logoY, logoWidth, logoHeight);
        }
        
        // Título en blanco
        contentStream.setNonStrokingColor(1.0f, 1.0f, 1.0f);
        contentStream.beginText();
        contentStream.setFont(fontBold, 20);
        contentStream.newLineAtOffset(logo != null ? 110 : 50, 775);
        contentStream.showText("REPORTE DE VENTAS");
        contentStream.endText();

        // Fecha
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        contentStream.beginText();
        contentStream.setFont(font, 11);
        contentStream.newLineAtOffset(50, 755);
        contentStream.showText("Fecha de generación: " + fecha);
        contentStream.endText();
        
        // Total de ventas y monto total
        BigDecimal totalVentas = ventas.stream()
                .filter(v -> v.getTotal() != null)
                .map(VentaDto::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        contentStream.beginText();
        contentStream.setFont(font, 11);
        contentStream.newLineAtOffset(50, 740);
        contentStream.showText("Total de ventas: " + ventas.size() + " | Monto total: $" + 
                String.format("%.0f", totalVentas.doubleValue()));
        contentStream.endText();

        // Encabezados de tabla con fondo
        float yHeader = 700;
        contentStream.setNonStrokingColor(headerColorR, headerColorG, headerColorB);
        contentStream.addRect(50, yHeader - 15, 500, 20);
        contentStream.fill();
        
        contentStream.setNonStrokingColor(1.0f, 1.0f, 1.0f);
        contentStream.beginText();
        contentStream.setFont(fontBold, 10);
        contentStream.newLineAtOffset(55, yHeader - 5);
        contentStream.showText("ID");
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(90, yHeader - 5);
        contentStream.showText("Usuario ID");
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(180, yHeader - 5);
        contentStream.showText("Fecha");
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(300, yHeader - 5);
        contentStream.showText("Items");
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.newLineAtOffset(380, yHeader - 5);
        contentStream.showText("Total");
        contentStream.endText();

        // Línea separadora
        contentStream.setStrokingColor(headerColorR, headerColorG, headerColorB);
        contentStream.setLineWidth(1);
        contentStream.moveTo(50, yHeader - 15);
        contentStream.lineTo(550, yHeader - 15);
        contentStream.stroke();

        // Datos con filas alternadas
        float y = 680;
        boolean alternate = false;
        float lightGray = 248.0f / 255.0f;
        
        for (VentaDto venta : ventas) {
            if (y < 50) {
                contentStream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                y = 750;
                alternate = false;
            }

            // Fondo alternado
            if (alternate) {
                contentStream.setNonStrokingColor(lightGray, lightGray, lightGray);
                contentStream.addRect(50, y - 12, 500, 15);
                contentStream.fill();
            }
            alternate = !alternate;

            // Borde de fila
            contentStream.setStrokingColor(0.8f, 0.8f, 0.8f);
            contentStream.setLineWidth(0.5f);
            contentStream.addRect(50, y - 12, 500, 15);
            contentStream.stroke();

            contentStream.setNonStrokingColor(0.0f, 0.0f, 0.0f);
            contentStream.beginText();
            contentStream.setFont(font, 9);
            contentStream.newLineAtOffset(55, y - 5);
            contentStream.showText(String.valueOf(venta.getVentaId() != null ? venta.getVentaId() : "N/A"));
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(90, y - 5);
            contentStream.showText(String.valueOf(venta.getUsuarioId() != null ? venta.getUsuarioId() : "N/A"));
            contentStream.endText();

            String fechaVenta = venta.getFechaVenta() != null ? venta.getFechaVenta().toString() : "N/A";
            contentStream.beginText();
            contentStream.newLineAtOffset(180, y - 5);
            contentStream.showText(fechaVenta);
            contentStream.endText();

            int items = venta.getItems() != null ? venta.getItems().size() : 0;
            contentStream.beginText();
            contentStream.newLineAtOffset(300, y - 5);
            contentStream.showText(String.valueOf(items));
            contentStream.endText();

            String total = venta.getTotal() != null ? "$" + String.format("%.0f", venta.getTotal().doubleValue()) : "N/A";
            contentStream.setNonStrokingColor(39.0f / 255.0f, 174.0f / 255.0f, 96.0f / 255.0f);
            contentStream.beginText();
            contentStream.setFont(fontBold, 9);
            contentStream.newLineAtOffset(380, y - 5);
            contentStream.showText(total);
            contentStream.endText();

            y -= 15;
        }

        // Pie de página
        y -= 20;
        contentStream.setStrokingColor(0.7f, 0.7f, 0.7f);
        contentStream.setLineWidth(1);
        contentStream.moveTo(50, y);
        contentStream.lineTo(550, y);
        contentStream.stroke();
        
        y -= 15;
        contentStream.setNonStrokingColor(0.5f, 0.5f, 0.5f);
        contentStream.beginText();
        contentStream.setFont(font, 8);
        contentStream.newLineAtOffset(50, y);
        contentStream.showText("Reporte generado por Technova - Sistema de Gestión");
        contentStream.endText();

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
        
        // Cargar logo
        PDImageXObject logo = cargarLogo(document);
        
        // Encabezado con gradiente Technova (color púrpura)
        // Fondo del encabezado
        contentStream.setNonStrokingColor(102.0f / 255.0f, 126.0f / 255.0f, 234.0f / 255.0f); // #667eea
        contentStream.addRect(0, 700, 595, 100);
        contentStream.fill();
        
        // Dibujar logo si está disponible
        if (logo != null) {
            float logoWidth = 60;
            float logoHeight = 60;
            float logoX = 50;
            float logoY = 740;
            contentStream.drawImage(logo, logoX, logoY, logoWidth, logoHeight);
        }
        
        // Título de la factura en blanco
        contentStream.setNonStrokingColor(1.0f, 1.0f, 1.0f); // Blanco
        contentStream.beginText();
        contentStream.setFont(fontBold, 24);
        contentStream.newLineAtOffset(logo != null ? 120 : 50, 750);
        contentStream.showText("FACTURA DE COMPRA");
        contentStream.endText();

        // Información de la empresa
        contentStream.beginText();
        contentStream.setFont(fontBold, 14);
        contentStream.newLineAtOffset(logo != null ? 120 : 50, 720);
        contentStream.showText("TECHNOVA");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 11);
        contentStream.newLineAtOffset(logo != null ? 120 : 50, 705);
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
