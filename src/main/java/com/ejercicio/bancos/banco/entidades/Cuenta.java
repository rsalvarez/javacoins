package com.ejercicio.bancos.banco.entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;


@Getter
@Setter
@Entity(name = "cuentas")
public class Cuenta  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", nullable = false)
    private String numero;

    @Column(name = "saldo", nullable = false)
    private double saldo;

    @Column(name = "saldo_crypto", nullable = false)
    private double saldoCrypto;
    @OneToOne
    @JoinColumn(name = "cliente_id")
    @JsonIgnoreProperties("cuentas")
    private Cliente cliente;

    @Column(name="cantidad_operaciones")
    private Integer cantidadOperaciones;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Cuenta{");
        sb.append("\t numero='").append(numero).append('\n');
        sb.append("\t saldo=").append(saldo).append('\n');
        sb.append("\t saldoCrypto=").append(saldoCrypto).append('\n');
        sb.append("\t cantidadOperaciones=").append(cantidadOperaciones).append('\n');
        sb.append('}');
        return sb.toString();
    }
}
