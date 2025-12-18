package com.technova.technov.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;
import com.technova.technov.domain.entity.Proveedor;
import com.technova.technov.domain.entity.DetalleOrden;

@Entity
@Table(name = "orden_compra")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_Proveedor")
    private Proveedor proveedor;

    @Temporal(TemporalType.DATE)
    @Column(name = "Fecha")
    private Date fecha;

    @Column(name = "Total")
    private Double total;

    @Column(name = "Estado")
    private String estado; // PENDIENTE, RECIBIDA

    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleOrden> detalles;
}
