package com.ejercicio.bancos.banco.services;

import com.ejercicio.bancos.banco.Util;
import com.ejercicio.bancos.banco.dto.MensajeCompra;
import com.ejercicio.bancos.banco.dto.MensajeRta;
import com.ejercicio.bancos.banco.entidades.Cliente;
import com.ejercicio.bancos.banco.repository.ClienteRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Optional;
import java.util.Scanner;
import java.util.logging.Logger;

@Component
public class CompradorServiceImpl implements CompradorService {

    private final ClienteRepository clienteRepository;

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.routingmq.banco}")
    private String compra;

    public CompradorServiceImpl(ClienteRepository clienteRepository, RabbitTemplate rabbitTemplate) {
        this.clienteRepository = clienteRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void comprarCripto(Cliente comprador, Cliente vendedor, double cotizacionPactada, double cantidad) throws IOException {
        MensajeCompra mensajeCompra = new MensajeCompra();
        mensajeCompra.setCompra(cantidad);
        mensajeCompra.setCotizacionPactada(cotizacionPactada);
        mensajeCompra.setClienteComprador(comprador);
        mensajeCompra.setClienteVendedor(vendedor);
        Util.sendMessageRequest(rabbitTemplate,mensajeCompra,compra);
}

    public void initCompra() {
        int inChar = 0;
        double cotizacion, cantidad = 0;
        Scanner cotiza = new Scanner(System.in);
        Scanner opcion = new Scanner(System.in);
        System.out.println("Ingrese una option:");
        do {
                System.out.println("1 - Comprar");
                System.out.println("0 - Salir");
                inChar = opcion.nextInt();
                if (inChar != 0 ) {
                    System.out.println("Ingrese cotizacion de JAVACOIN");
                    cotizacion = cotiza.nextDouble();
                    System.out.println("Ingrese cantidad a comprar de JAVACOIN");
                    cantidad = cotiza.nextDouble();
                    System.out.println("Buscando clientes de ID 1 y 2");
                    try {
                        Optional<Cliente> cliComprador = clienteRepository.findById(1L);
                        Optional<Cliente> cliVendedor = clienteRepository.findById(2L);
                        this.comprarCripto(cliComprador.get(), cliVendedor.get(), cotizacion, cantidad);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        } while (inChar != 0);
        System.exit(0);
    }
}
