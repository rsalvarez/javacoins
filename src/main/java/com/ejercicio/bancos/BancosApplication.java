package com.ejercicio.bancos;

import com.ejercicio.bancos.banco.services.CompradorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.transaction.RabbitTransactionManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import java.util.logging.Logger;

@SpringBootApplication
@EnableRabbit
public class BancosApplication implements CommandLineRunner {
	private static Logger logger = Logger.getLogger(BancosApplication.class.getName());

	private final CompradorService comprador;



	public BancosApplication(CompradorService comprador) {

		this.comprador = comprador;
	}


	public static void main(String[] args) {
		SpringApplication.run(BancosApplication.class, args);
	}

	public RabbitTemplate getRabbit() {
		RabbitTemplate rr = new RabbitTemplate();
		rr.setChannelTransacted(true);
		return rr;
	}

	private ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Override
	public void run(String... args) {
		comprador.initCompra();
    }
}
