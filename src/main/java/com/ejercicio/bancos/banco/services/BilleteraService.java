package com.ejercicio.bancos.banco.services;

import com.ejercicio.bancos.banco.dto.MensajeRta;

import java.io.IOException;

public interface BilleteraService {
    void procesarMensaje(MensajeRta mensajeRta) throws IOException;
}
