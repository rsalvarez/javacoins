package com.ejercicio.bancos.banco.services;

import com.ejercicio.bancos.banco.dto.MensajeCompra;
import com.ejercicio.bancos.banco.dto.MensajeRta;
import com.ejercicio.bancos.banco.entidades.OrdenCompra;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;

public interface BancoService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    boolean guardarOrdenDeCompra(OrdenCompra ordenCompra);
    void gprocesarMensajeRespuesta(MensajeRta message) throws IOException, ClassNotFoundException;
    void procesarMensajeRequest(MensajeCompra mensajeCompra) throws IOException;
}
