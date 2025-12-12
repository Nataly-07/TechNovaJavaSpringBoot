package com.technova.technov.domain.impl;

import com.technova.technov.domain.dto.*;
import com.technova.technov.domain.entity.*;
import com.technova.technov.domain.repository.*;
import com.technova.technov.domain.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private DetalleCompraRepository detalleCompraRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardDto obtenerDashboard() {
        try {
            // Obtener todas las ventas activas
            List<Venta> todasLasVentas = ventaRepository.findByEstadoTrue();
            List<Compra> todasLasCompras = compraRepository.findAll();
            List<Producto> todosLosProductos = productoRepository.findByEstadoTrue();
            List<Usuario> todosLosUsuarios = usuarioRepository.findByEstadoTrue();

        // Fechas para cálculos
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMesActual = hoy.withDayOfMonth(1);
        LocalDate inicioMesAnterior = inicioMesActual.minusMonths(1);
        LocalDate finMesAnterior = inicioMesActual.minusDays(1);

        // ========== ESTADÍSTICAS DE VENTAS ==========
        BigDecimal totalIngresos = calcularTotalIngresos(todasLasVentas);
        long totalVentas = todasLasVentas.size();

        // Ventas del mes actual
        List<Venta> ventasEsteMes = todasLasVentas.stream()
                .filter(v -> v.getFechaVenta() != null && 
                           !v.getFechaVenta().isBefore(inicioMesActual) &&
                           !v.getFechaVenta().isAfter(hoy))
                .collect(Collectors.toList());
        long ventasEsteMesCount = ventasEsteMes.size();
        BigDecimal ingresosEsteMes = calcularTotalIngresos(ventasEsteMes);

        // Ventas del mes anterior
        List<Venta> ventasMesAnterior = todasLasVentas.stream()
                .filter(v -> v.getFechaVenta() != null &&
                           !v.getFechaVenta().isBefore(inicioMesAnterior) &&
                           !v.getFechaVenta().isAfter(finMesAnterior))
                .collect(Collectors.toList());
        BigDecimal ingresosMesAnterior = calcularTotalIngresos(ventasMesAnterior);

        // Cálculo de crecimiento
        BigDecimal crecimientoVentas = BigDecimal.ZERO;
        if (ingresosMesAnterior.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diferencia = ingresosEsteMes.subtract(ingresosMesAnterior);
            crecimientoVentas = diferencia.divide(ingresosMesAnterior, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else if (ingresosEsteMes.compareTo(BigDecimal.ZERO) > 0) {
            crecimientoVentas = BigDecimal.valueOf(100); // 100% de crecimiento si no había ventas el mes anterior
        }

        // Promedio de venta
        BigDecimal promedioVenta = totalVentas > 0 
                ? totalIngresos.divide(BigDecimal.valueOf(totalVentas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // ========== ESTADÍSTICAS DE PEDIDOS ==========
        long totalPedidos = todasLasVentas.size();
        long pedidosEsteMes = ventasEsteMesCount;
        // Para pedidos pendientes, asumimos que no hay un estado específico de "pendiente"
        // pero podríamos considerar todas las ventas como completadas
        long pedidosPendientes = 0; // Se puede ajustar según la lógica de negocio

        // ========== ESTADÍSTICAS DE PRODUCTOS ==========
        long totalProductos = todosLosProductos.size();
        long productosActivos = todosLosProductos.size();
        long productosStockBajo = todosLosProductos.stream()
                .filter(p -> p.getStock() != null && p.getStock() <= 10)
                .count();

        // Productos más vendidos
        List<ProductoMasVendidoDto> productosMasVendidos = obtenerProductosMasVendidos(todasLasVentas);

        // ========== ESTADÍSTICAS DE COMPRAS ==========
        long totalCompras = todasLasCompras.size();
        BigDecimal totalGastado = todasLasCompras.stream()
                .map(c -> c.getTotal() != null ? c.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal gastosEsteMes = todasLasCompras.stream()
                .filter(c -> c.getFechaCompra() != null &&
                           c.getFechaCompra().toLocalDate().isAfter(inicioMesActual.minusDays(1)) &&
                           !c.getFechaCompra().toLocalDate().isAfter(hoy))
                .map(c -> c.getTotal() != null ? c.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ========== ESTADÍSTICAS DE USUARIOS ==========
        long totalClientes = todosLosUsuarios.stream()
                .filter(u -> "cliente".equalsIgnoreCase(u.getRole()))
                .count();
        long totalEmpleados = todosLosUsuarios.stream()
                .filter(u -> "empleado".equalsIgnoreCase(u.getRole()))
                .count();

        // ========== VENTAS POR MES (ÚLTIMOS 6 MESES) ==========
        List<VentaPorMesDto> ventasPorMes = obtenerVentasPorUltimosMeses(todasLasVentas, 6);

            return DashboardDto.builder()
                    .totalVentas(totalVentas)
                    .totalIngresos(totalIngresos)
                    .ventasEsteMes(ventasEsteMesCount)
                    .ingresosEsteMes(ingresosEsteMes)
                    .promedioVenta(promedioVenta)
                    .totalPedidos(totalPedidos)
                    .pedidosEsteMes(pedidosEsteMes)
                    .pedidosPendientes(pedidosPendientes)
                    .totalProductos(totalProductos)
                    .productosActivos(productosActivos)
                    .productosStockBajo(productosStockBajo)
                    .productosMasVendidos(productosMasVendidos)
                    .totalCompras(totalCompras)
                    .totalGastado(totalGastado)
                    .gastosEsteMes(gastosEsteMes)
                    .totalClientes(totalClientes)
                    .totalEmpleados(totalEmpleados)
                    .crecimientoVentas(crecimientoVentas)
                    .ventasPorMes(ventasPorMes)
                    .build();
        } catch (Exception e) {
            // Log del error y retornar un dashboard vacío con valores por defecto
            System.err.println("Error al obtener dashboard: " + e.getMessage());
            e.printStackTrace();
            return DashboardDto.builder()
                    .totalVentas(0)
                    .totalIngresos(BigDecimal.ZERO)
                    .ventasEsteMes(0)
                    .ingresosEsteMes(BigDecimal.ZERO)
                    .promedioVenta(BigDecimal.ZERO)
                    .totalPedidos(0)
                    .pedidosEsteMes(0)
                    .pedidosPendientes(0)
                    .totalProductos(0)
                    .productosActivos(0)
                    .productosStockBajo(0)
                    .productosMasVendidos(new ArrayList<>())
                    .totalCompras(0)
                    .totalGastado(BigDecimal.ZERO)
                    .gastosEsteMes(BigDecimal.ZERO)
                    .totalClientes(0)
                    .totalEmpleados(0)
                    .crecimientoVentas(BigDecimal.ZERO)
                    .ventasPorMes(new ArrayList<>())
                    .build();
        }
    }

    private BigDecimal calcularTotalIngresos(List<Venta> ventas) {
        BigDecimal total = BigDecimal.ZERO;
        for (Venta v : ventas) {
            try {
                List<DetalleVenta> detalles = detalleVentaRepository.findByVenta(v);
                for (DetalleVenta dv : detalles) {
                    if (dv != null && dv.getPrecio() != null) {
                        total = total.add(dv.getPrecio());
                    }
                }
            } catch (Exception e) {
                // Log del error pero continuar procesando otras ventas
                System.err.println("Error al calcular ingresos de venta ID " + v.getId() + ": " + e.getMessage());
                continue;
            }
        }
        return total;
    }

    private List<ProductoMasVendidoDto> obtenerProductosMasVendidos(List<Venta> ventas) {
        Map<Integer, Long> cantidadPorProducto = new HashMap<>();
        Map<Integer, String> nombrePorProducto = new HashMap<>();
        Map<Integer, String> imagenPorProducto = new HashMap<>();

        for (Venta v : ventas) {
            try {
                List<DetalleVenta> detalles = detalleVentaRepository.findByVenta(v);
                for (DetalleVenta dv : detalles) {
                    if (dv == null || dv.getProducto() == null) {
                        continue; // Saltar si falta información crítica
                    }
                    
                    Integer productoId = dv.getProducto().getId();
                    if (productoId == null) {
                        continue;
                    }
                    
                    // Manejar cantidad de forma segura
                    int cantidad = 0;
                    try {
                        if (dv.getCantidad() != null && !dv.getCantidad().trim().isEmpty()) {
                            cantidad = Integer.parseInt(dv.getCantidad().trim());
                        }
                    } catch (NumberFormatException e) {
                        // Si no se puede parsear, usar 0
                        cantidad = 0;
                    }
                    
                    if (cantidad > 0) {
                        cantidadPorProducto.put(productoId, 
                            cantidadPorProducto.getOrDefault(productoId, 0L) + cantidad);
                        
                        if (!nombrePorProducto.containsKey(productoId)) {
                            String nombre = dv.getProducto().getNombre();
                            nombrePorProducto.put(productoId, nombre != null ? nombre : "Producto");
                        }
                        
                        if (!imagenPorProducto.containsKey(productoId) && dv.getProducto().getImagen() != null) {
                            imagenPorProducto.put(productoId, dv.getProducto().getImagen());
                        }
                    }
                }
            } catch (Exception e) {
                // Log del error pero continuar procesando otras ventas
                System.err.println("Error al procesar venta ID " + v.getId() + ": " + e.getMessage());
                continue;
            }
        }

        return cantidadPorProducto.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> ProductoMasVendidoDto.builder()
                        .productoId(entry.getKey())
                        .nombreProducto(nombrePorProducto.getOrDefault(entry.getKey(), "Producto"))
                        .cantidadVendida(entry.getValue())
                        .imagen(imagenPorProducto.getOrDefault(entry.getKey(), ""))
                        .build())
                .collect(Collectors.toList());
    }

    private List<VentaPorMesDto> obtenerVentasPorUltimosMeses(List<Venta> ventas, int meses) {
        LocalDate hoy = LocalDate.now();
        List<VentaPorMesDto> resultado = new ArrayList<>();

        for (int i = meses - 1; i >= 0; i--) {
            LocalDate inicioMes = hoy.minusMonths(i).withDayOfMonth(1);
            LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
            if (inicioMes.getMonthValue() == hoy.getMonthValue() && inicioMes.getYear() == hoy.getYear()) {
                finMes = hoy; // Solo hasta hoy para el mes actual
            }

            LocalDate inicioMesFinal = inicioMes;
            LocalDate finMesFinal = finMes;

            List<Venta> ventasDelMes = ventas.stream()
                    .filter(v -> v.getFechaVenta() != null &&
                               !v.getFechaVenta().isBefore(inicioMesFinal) &&
                               !v.getFechaVenta().isAfter(finMesFinal))
                    .collect(Collectors.toList());

            BigDecimal total = calcularTotalIngresos(ventasDelMes);
            long cantidad = ventasDelMes.size();

            Month mes = inicioMes.getMonth();
            String nombreMes = mes.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-ES"));
            
            resultado.add(VentaPorMesDto.builder()
                    .mes(nombreMes)
                    .anio(inicioMes.getYear())
                    .total(total)
                    .cantidad(cantidad)
                    .build());
        }

        return resultado;
    }
}

