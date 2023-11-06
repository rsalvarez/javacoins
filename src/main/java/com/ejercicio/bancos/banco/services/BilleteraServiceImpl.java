package com.ejercicio.bancos.banco.services;

import com.ejercicio.bancos.banco.Estado;
import com.ejercicio.bancos.banco.Util;
import com.ejercicio.bancos.banco.dto.MensajeRta;
import com.ejercicio.bancos.banco.entidades.Cliente;
import com.ejercicio.bancos.banco.entidades.Cuenta;
import com.ejercicio.bancos.banco.entidades.OrdenCompra;
import com.ejercicio.bancos.banco.repository.ClienteRepository;
import com.ejercicio.bancos.banco.repository.CuentaRepository;
import com.ejercicio.bancos.banco.repository.OrdenCompraRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class BilleteraServiceImpl implements BilleteraService {

    private static Logger logger = Logger.getLogger(BilleteraServiceImpl.class.getName());
    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;
    private final RabbitTemplate rabbitTemplate;
    private final OrdenCompraRepository ordenCompraRepository;
    @Value("${spring.routingmq.vendedorResponse}")
    private String vendedorAut;
    @Value("${spring.routingmq.bancoResponse}")
    private String respuestaBanco;
    private Map<String, Cuenta> dataUsuarios;

    public BilleteraServiceImpl(CuentaRepository cuentaRepository, ClienteRepository clienteRepository, RabbitTemplate rabbitTemplate, OrdenCompraRepository ordenCompraRepository) {
        this.cuentaRepository = cuentaRepository;
        this.clienteRepository = clienteRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.ordenCompraRepository = ordenCompraRepository;

    }

    private Map<String, Cuenta> loadUsers() {
        List<Cliente> clientes = clienteRepository.findAll();
        return clientes.stream().collect(Collectors.toMap(Cliente::getDni, Cliente::getCuentas));
    }

    private Cuenta getCuentaByDni(String dni) {
        if (dataUsuarios == null ) {
            dataUsuarios = loadUsers();
        }
        return dataUsuarios.getOrDefault(dni,null);
    }


    @Override
    public void procesarMensaje(MensajeRta mensajeRta) throws IOException {
        logger.info("Ingresando al procesar Mensaje");
        if (getCuentaByDni(mensajeRta.getOrdenCompra().getVendedor().getDni()) != null
        && getCuentaByDni(mensajeRta.getOrdenCompra().getComprador().getDni()) != null) {
            logger.info("Procesar mensaje : tiene datos correctos.");
            if (!Util.vistaPorVendedor(mensajeRta.getOrdenCompra().getEstado())) {
                logger.info("El vendedor no la autorizo todavia, la enviamos a revisar por el vendedor");
                sendMessageRequest(mensajeRta,vendedorAut);
            } else {
                if (Util.isAutorizado(mensajeRta.getOrdenCompra().getEstado())) {
                    logger.info("transaccion autorizada por vendedor controlamos y notificamos.");
                    OrdenCompra ordenCompra = mensajeRta.getOrdenCompra();
                    if (!getSaldoCuentaJavaCoins(ordenCompra.getVendedor().getCuentas(),ordenCompra.getCantidadComprada())) {
                        mensajeRta = new MensajeRta();
                        mensajeRta.setId(UUID.randomUUID().toString());
                        mensajeRta.setEstado(Estado.RECHAZADA.name());
                        mensajeRta.setStatusMsg("Vendedor javacoins :sin saldo suficiente");
                        logger.info("rechazada por falta de fondos.");
                    } else {
                        double cantComprada = ordenCompra.getCantidadComprada();
                        ordenCompra.getVendedor().getCuentas().setSaldoCrypto(  ordenCompra.getVendedor().getCuentas().getSaldoCrypto() - cantComprada);
                        ordenCompra.getComprador().getCuentas().setSaldoCrypto(ordenCompra.getComprador().getCuentas().getSaldoCrypto() + cantComprada);
                        mensajeRta.setOrdenCompra(ordenCompra);
                        // actualizamos usuarios en memoria
                        logger.info("modificando saldos y acutalizando usuarios");
                        this.dataUsuarios.putIfAbsent(ordenCompra.getComprador().getDni(), ordenCompra.getComprador().getCuentas());
                        this.dataUsuarios.putIfAbsent(ordenCompra.getVendedor().getDni(), ordenCompra.getVendedor().getCuentas());
                    }
                }
                logger.info("notificando banco");
                sendMessageRequest(mensajeRta,respuestaBanco);
            }
        } else {
            mensajeRta.setEstado(Estado.RECHAZADA.name());
            mensajeRta.setStatusMsg("Cliente o vendedor inexistente. controle");
            mensajeRta.getOrdenCompra().setEstado(Estado.RECHAZADA);
            logger.info("rechazada por inconsistencia de datos.");
            sendMessageRequest(mensajeRta,respuestaBanco);
        }


    }

    private void sendMessageRequest(Object rta, String destino) throws IOException {
        Util.sendMessageRequest(rabbitTemplate,rta,destino);
    }

    private boolean getSaldoCuentaJavaCoins(Cuenta cuenta, double cantidadComprada) {
        return cuenta.getSaldoCrypto() >= cantidadComprada;
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

}
