package com.ejercicio.bancos.banco.services;

import com.ejercicio.bancos.banco.entidades.Cliente;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.io.IOException;

public interface CompradorService {
    public void comprarCripto(Cliente comprador, Cliente vendedor, double cotizacionPactada, double cantidad) throws IOException;
    public void initCompra();

    }
