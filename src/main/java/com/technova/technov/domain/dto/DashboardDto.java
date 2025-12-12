package com.technova.technov.domain.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO que contiene todas las estadísticas del Dashboard del administrador.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDto {
    // Estadísticas de Ventas
    private long totalVentas;
    private BigDecimal totalIngresos;
    private long ventasEsteMes;
    private BigDecimal ingresosEsteMes;
    private BigDecimal promedioVenta;
    
    // Estadísticas de Pedidos
    private long totalPedidos;
    private long pedidosEsteMes;
    private long pedidosPendientes;
    
    // Estadísticas de Productos
    private long totalProductos;
    private long productosActivos;
    private long productosStockBajo; // Stock <= 10
    private List<ProductoMasVendidoDto> productosMasVendidos;
    
    // Estadísticas de Compras
    private long totalCompras;
    private BigDecimal totalGastado;
    private BigDecimal gastosEsteMes;
    
    // Estadísticas de Usuarios
    private long totalClientes;
    private long totalEmpleados;
    
    // Tendencias y comparaciones
    private BigDecimal crecimientoVentas; // Porcentaje comparado con mes anterior
    private List<VentaPorMesDto> ventasPorMes; // Últimos 6 meses
}

