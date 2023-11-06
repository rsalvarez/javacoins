package com.ejercicio.bancos.banco.listener;

import com.ejercicio.bancos.banco.dto.MensajeRta;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.logging.Logger;

@Component
public class CompradorListener {
    private static Logger logger = Logger.getLogger(CompradorListener.class.getName());
    @RabbitListener(queues = "${spring.queues.clienteResponse}")
    public void getCompradorResponse(byte[] message) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(message);
             ObjectInput in = new ObjectInputStream(bis);) {
            MensajeRta rta = (MensajeRta) in.readObject();
            if (rta.getEstado() != null) {
                logger.info("\nEstado : " + rta.getEstado() + "\n");
                logger.info("Mensaje " + rta.getStatusMsg()+ "\n");
                logger.info("Estado OK " + rta.getOrdenCompra().getEstado()+ "\n");
                logger.info("DATA OC  " + rta.getOrdenCompra()+ "\n");
                logger.info("Data comprador : " + rta.getOrdenCompra().getComprador()+ "\n");
                logger.info("Data Vendedor : " + rta.getOrdenCompra().getVendedor()+ "\n");
            }
        }
    }
}
