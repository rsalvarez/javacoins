package com.ejercicio.bancos.banco.services;

import com.ejercicio.bancos.banco.Estado;
import com.ejercicio.bancos.banco.Util;
import com.ejercicio.bancos.banco.dto.MensajeCompra;
import com.ejercicio.bancos.banco.entidades.Cuenta;
import com.ejercicio.bancos.banco.entidades.Cliente;
import com.ejercicio.bancos.banco.entidades.OrdenCompra;
import com.ejercicio.bancos.banco.exceptions.ExceptionClienteNoEncontrado;
import com.ejercicio.bancos.banco.repository.CuentaRepository;
import com.ejercicio.bancos.banco.repository.ClienteRepository;
import com.ejercicio.bancos.banco.repository.OrdenCompraRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import com.ejercicio.bancos.banco.dto.MensajeRta;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class BancoServiceImpl implements  BancoService{
    private static Logger logger = Logger.getLogger(BancoServiceImpl.class.getName());
    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final RabbitTemplate rabbitTemplate;
    @Value("${spring.routingmq.billetera}")
    private String routeBilletera;
    @Value("${spring.routingmq.clienteResponse}")
    private String routeCliente;


    public BancoServiceImpl(ClienteRepository usuarioRepository, CuentaRepository cuentaRepository, OrdenCompraRepository ordenCompraRepository, RabbitTemplate rabbitTemplate) {
        this.clienteRepository = usuarioRepository;
        this.cuentaRepository = cuentaRepository;
        this.ordenCompraRepository = ordenCompraRepository;
        this.rabbitTemplate = rabbitTemplate;
    }



    @Override
    @Transactional
    public boolean guardarOrdenDeCompra(OrdenCompra ordenCompra) {
        if (ordenCompra != null) {
            ordenCompraRepository.save(ordenCompra);
            cuentaRepository.save(ordenCompra.getVendedor().getCuentas());
            cuentaRepository.save(ordenCompra.getComprador().getCuentas());
        }
        return true;
    }

    @Override
    public void gprocesarMensajeRespuesta(MensajeRta mensajeRta) throws IOException, ClassNotFoundException {
        MensajeRta rta  = mensajeRta;
        if (Util.isEstadoRechazado(rta.getOrdenCompra().getEstado())) {
            rta = new MensajeRta();
            rta.setId(UUID.randomUUID().toString());
            rta.setEstado(rta.getEstado());
            rta.setStatusMsg(rta.getStatusMsg());
        } else {
            OrdenCompra ordenCompra = rta.getOrdenCompra();
            Cliente comprador = ordenCompra.getComprador();
            Cuenta cuentaComprador = comprador.getCuentas();
            // descontamos el total de la compra en dolares
            cuentaComprador.setSaldo(cuentaComprador.getSaldo() - (1+getComision(cuentaComprador)) * (ordenCompra.getCantidadComprada() * ordenCompra.getCotizacion()));
            cuentaComprador.setCantidadOperaciones(cuentaComprador.getCantidadOperaciones()+1);
            Cliente vendedor = ordenCompra.getVendedor();
            Cuenta cuVendedor = vendedor.getCuentas();
            // actualizamos saldo de dolares
            cuVendedor.setSaldo(cuVendedor.getSaldo() + (1+getComision(cuVendedor)) * (ordenCompra.getCantidadComprada() * ordenCompra.getCotizacion()));
            cuVendedor.setCantidadOperaciones(cuVendedor.getCantidadOperaciones()+1);
            rta = new MensajeRta();
            rta.setId(UUID.randomUUID().toString());
            rta.setEstado(Estado.CONFIRMADA.name());
            rta.setStatusMsg("Transaccion ejecutada correctamente: saldo JAVACOIN -> " + cuentaComprador.getSaldoCrypto());
            rta.setOrdenCompra(ordenCompra);
            if (!guardarOrdenDeCompra(ordenCompra)) {
                rta.setEstado(Estado.ERROR.name());
                rta.setStatusMsg("Error : no se pudo grabar la orden de compra");
            }

        }
        sendMessageRequest(rta, routeCliente);
    }

    private boolean validaCliente(Cliente cliente) {
       return clienteRepository.findByDni(cliente.getDni()).isPresent();
    }
    @Override
    public void procesarMensajeRequest(MensajeCompra mensajeCompra) throws IOException {
        Cliente usuarioComprador = mensajeCompra.getClienteComprador();
        Cliente usuarioVendedor = mensajeCompra.getClienteVendedor();
        MensajeRta rta = null;
        String rutaDestino = "";
        if (!validaCliente(usuarioVendedor)) {
            rta = new MensajeRta();
            rta.setId(UUID.randomUUID().toString());
            rta.setEstado(Estado.RECHAZADA.name());
            rta.setStatusMsg("Cliente vendedor, inexistente");
            rutaDestino = routeCliente;
        } else if (!validaCliente(usuarioComprador)) {
            rta = new MensajeRta();
            rta.setId(UUID.randomUUID().toString());
            rta.setEstado(Estado.RECHAZADA.name());
            rta.setStatusMsg("Cliente comprador, inexistente");
            rutaDestino = routeCliente;
        } else if (!getSaldoCuenta(mensajeCompra.getCotizacionPactada(), mensajeCompra.getCompra(), mensajeCompra.getClienteComprador().getCuentas())) {
            rta = new MensajeRta();
            rta.setId(UUID.randomUUID().toString());
            rta.setEstado(Estado.RECHAZADA.name());
            rta.setStatusMsg("Comprador sin saldo en dolares suficiente");
            rutaDestino = routeCliente;
        }  else {
            OrdenCompra ordenCompra = new OrdenCompra();
            ordenCompra.setEstado(Estado.PENDIENTE);
            ordenCompra.setComprador(mensajeCompra.getClienteComprador());
            ordenCompra.setVendedor(mensajeCompra.getClienteVendedor());
            ordenCompra.setCantidadComprada(mensajeCompra.getCompra());
            ordenCompra.setCotizacion(mensajeCompra.getCotizacionPactada());
            double total =  (getComision(mensajeCompra.getClienteComprador().getCuentas())+1)
                    * (ordenCompra.getCotizacion() * ordenCompra.getCantidadComprada());
            ordenCompra.setTotalOperacion(total);
            rta = new MensajeRta();
            rta.setId(UUID.randomUUID().toString());
            rta.setEstado(Estado.PENDIENTE.toString());
            rta.setStatusMsg("OK");
            rta.setOrdenCompra(ordenCompra);
            rutaDestino = routeBilletera;
        }
        sendMessageRequest(rta, rutaDestino);


    }

    private void sendMessageRequest(Object rta, String destino) throws IOException {
        Util.sendMessageRequest(rabbitTemplate,rta,destino);
    }
    private double getComision(Cuenta cuenta) {
        double comision = 0;
        if (cuenta.getCantidadOperaciones() < 3) {
            comision = 0.05;
        } else if (cuenta.getCantidadOperaciones() < 6 ) {
            comision = 0.03;
        }
        return comision;
    }
    private boolean getSaldoCuenta(double cotizacionPactada, double cantidadCompra, Cuenta cuenta) {
        double commision = getComision(cuenta) * 1;
        double total = commision * (cotizacionPactada * cantidadCompra);
        return cuenta.getSaldo() >= total;
    }


}
