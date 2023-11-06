package com.ejercicio.bancos.banco.dto;

import com.ejercicio.bancos.banco.entidades.OrdenCompra;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MensajeRta implements Serializable {
    private String id;
    private String statusMsg;
    private String estado;
    private OrdenCompra ordenCompra;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MensajeRta {");
        sb.append(" id='").append(id).append('\'');
        sb.append(" statusMsg='").append(statusMsg).append('\'');
        sb.append(" estado='").append(estado).append('\'');
        sb.append(" ordenCompra=").append(ordenCompra);
        sb.append('}');
        return sb.toString();
    }
}
