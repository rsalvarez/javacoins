package com.ejercicio.bancos.banco.entidades;

import com.ejercicio.bancos.banco.Estado;
import lombok.Getter;
import lombok.Setter;


import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity(name = "ordenes_compra")
public class OrdenCompra  implements Serializable {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;
    private Estado estado = Estado.PENDIENTE;
    @OneToOne(fetch = FetchType.LAZY)
    private Cliente comprador;
    @OneToOne(fetch = FetchType.LAZY)
    private Cliente vendedor;
    private double cantidadComprada;
    private double cotizacion;
    private double totalOperacion;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("OrdenCompra{");
        sb.append(" \t id=").append(id).append('\n');
        sb.append(" \t estado=").append(estado).append('\n');
        sb.append(" \t comprador=").append(comprador).append('\n');
        sb.append(" \t vendedor=").append(vendedor).append('\n');
        sb.append(" \t ntidadComprada=").append(cantidadComprada).append('\n');
        sb.append(" \t cotizacion=").append(cotizacion).append('\n');
        sb.append(" \t totalOperacion=").append(totalOperacion).append('\n');
        sb.append('}').append('\n');
        return sb.toString();
    }
}


