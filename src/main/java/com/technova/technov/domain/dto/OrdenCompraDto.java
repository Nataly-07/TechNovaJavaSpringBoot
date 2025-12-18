package com.technova.technov.domain.dto;

import lombok.*;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCompraDto {
    private Integer id;
    private Integer proveedorId;
    private String proveedorNombre; // Para mostrar en la lista
    private Date fecha;
    private Double total;
    private String estado;
    private List<DetalleOrdenDto> detalles;
}
