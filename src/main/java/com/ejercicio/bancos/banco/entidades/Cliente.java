package com.ejercicio.bancos.banco.entidades;


import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;


@Getter
@Setter
@Entity(name = "clientes")
public class Cliente implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "dni", nullable = false)
    private String dni;
    @OneToOne(mappedBy = "cliente")
    private Cuenta cuentas;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Cliente{");
        sb.append(" nombre=").append(nombre).append('\n');
        sb.append("\t id=").append(id).append('\n');
        sb.append("\t dni='").append(dni).append('\n').append('\n');
        sb.append("\t cuentas=").append(cuentas.toString()).append('\n');;
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        Cliente cliente = (Cliente) o;
        return Objects.equals(dni, cliente.dni);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dni);
    }
}
