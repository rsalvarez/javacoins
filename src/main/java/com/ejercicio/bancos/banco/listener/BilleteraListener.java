package com.ejercicio.bancos.banco.listener;

import com.ejercicio.bancos.banco.dto.MensajeRta;
import com.ejercicio.bancos.banco.services.BilleteraService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.logging.Logger;

@Component
public class BilleteraListener {
    private static Logger logger = Logger.getLogger(BilleteraListener.class.getName());
    private final BilleteraService billeteraService;
    public BilleteraListener(BilleteraService billeteraService) {
        this.billeteraService = billeteraService;
    }

    @RabbitListener(queues = {"${spring.queues.billeteraRequest}"})
    public void getMessageBilleteraRequest(byte[] message) throws IOException, ClassNotFoundException {

        try (ByteArrayInputStream bis = new ByteArrayInputStream(message);
             ObjectInput in = new ObjectInputStream(bis);) {
            MensajeRta mensajeRta = (MensajeRta) in.readObject();;
            logger.info("<<Billetera>> Mensaje recibido : " + mensajeRta);
            billeteraService.procesarMensaje(mensajeRta);
        }

    }


}
