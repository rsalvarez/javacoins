package com.ejercicio.bancos.banco;


import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import static com.ejercicio.bancos.banco.Constantes.EXCH_BANK;

public class Util {

    public static boolean isEstadoRechazado(Estado estado) {
        return estado.equals(Estado.RECHAZADA) ||
                estado.equals(Estado.RECHAZADA_VENDEDOR);
    }

    public static boolean isAutorizado(Estado estado) {
        return (estado.equals(Estado.ACEPTADA) || estado.equals(Estado.ACEPTADA_VENDEDOR));
    }

    public static boolean vistaPorVendedor(Estado estado) {
        return estado.equals(Estado.ACEPTADA_VENDEDOR) ||
                estado.equals(Estado.RECHAZADA_VENDEDOR);
    }

    public static void sendMessageRequest(RabbitTemplate rabbitTemplate , Object rta, String destino) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(rta);
        out.flush();
        out.close();
        byte[] byteMessage = bos.toByteArray();
        bos.close();
        Message message = MessageBuilder.withBody(byteMessage).build();
        rabbitTemplate.send (EXCH_BANK,destino, message);
    }

}
