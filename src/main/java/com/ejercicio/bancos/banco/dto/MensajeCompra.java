package com.ejercicio.bancos.banco.dto;

import com.ejercicio.bancos.banco.entidades.Cliente;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MensajeCompra implements Serializable {
    private Cliente clienteComprador;
    private Cliente clienteVendedor;
    private double cotizacionPactada;
    private double compra;

}
