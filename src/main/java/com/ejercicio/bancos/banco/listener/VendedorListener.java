package com.ejercicio.bancos.banco.listener;

import com.ejercicio.bancos.banco.Estado;
import com.ejercicio.bancos.banco.Util;
import com.ejercicio.bancos.banco.dto.MensajeRta;
import com.ejercicio.bancos.banco.entidades.OrdenCompra;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.Random;
import java.util.logging.Logger;

@Component
public class VendedorListener {
        private static Logger logger = Logger.getLogger(VendedorListener.class.getName());


        private final RabbitTemplate rabbitTemplate;
        @Value("${spring.routingmq.billetera}")
        private String billeteraResponse;

        public VendedorListener(RabbitTemplate rabbitTemplate) {
            this.rabbitTemplate = rabbitTemplate;
        }

        @RabbitListener(queues = "${spring.queues.vendedorAutoriza}")
        public void getMessageAutorizacion(byte[] message) throws IOException, ClassNotFoundException {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(message);
                 // emulamos la desicion de autorizar compra.
                 ObjectInput in = new ObjectInputStream(bis);) {
                MensajeRta mensajeRta  = (MensajeRta)in.readObject();
                boolean bb = autorizarVenta();
                if (!bb) {
                    mensajeRta.setEstado(Estado.RECHAZADA_VENDEDOR.name());
                    OrdenCompra ordenCompra = mensajeRta.getOrdenCompra();
                    ordenCompra.setEstado(Estado.RECHAZADA_VENDEDOR);
                } else {
                    mensajeRta.setEstado(Estado.ACEPTADA_VENDEDOR.name());
                    OrdenCompra ordenCompra = mensajeRta.getOrdenCompra();
                    ordenCompra.setEstado(Estado.ACEPTADA_VENDEDOR);
                }
                logger.info("Esperando vendedor, autorice compra");
                Thread.sleep(10000);
                sendMessageRequest(mensajeRta, billeteraResponse);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        private void sendMessageRequest(Object rta, String destino) throws IOException {
            Util.sendMessageRequest(rabbitTemplate,rta,destino);
        }
        private boolean autorizarVenta() {
            Random random = new Random();
            return random.nextBoolean();
        }
}

