package com.technova.technov.domain.service;

import com.technova.technov.domain.dto.DashboardDto;

/**
 * Servicio para obtener estadísticas y datos del Dashboard del administrador.
 */
public interface DashboardService {
    DashboardDto obtenerDashboard();
}

