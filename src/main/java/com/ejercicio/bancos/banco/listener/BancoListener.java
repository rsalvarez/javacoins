package com.ejercicio.bancos.banco.listener;

import com.ejercicio.bancos.banco.dto.MensajeCompra;
import com.ejercicio.bancos.banco.dto.MensajeRta;
import com.ejercicio.bancos.banco.services.BancoService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.logging.Logger;

@Component
public class BancoListener {
    private static Logger logger = Logger.getLogger(BancoListener.class.getName());
    private final BancoService bancoService;

    public BancoListener(BancoService bancoService) {
        this.bancoService = bancoService;
    }

    @RabbitListener(queues = "${spring.queues.bancoResponse}")
    public void getMessageResponseBanco(byte[] message) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(message);
             ObjectInput in = new ObjectInputStream(bis);) {
            MensajeRta rta = (MensajeRta) in.readObject();
            bancoService.gprocesarMensajeRespuesta(rta);
        }
    }

    @RabbitListener(queues = "${spring.queues.banco}")
    public void getMessageRequestBanco(byte[] message) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(message);
             ObjectInput in = new ObjectInputStream(bis);) {
            MensajeCompra mensajeCompra  = (MensajeCompra)in.readObject();
            bancoService.procesarMensajeRequest(mensajeCompra);
        }
    }
}
