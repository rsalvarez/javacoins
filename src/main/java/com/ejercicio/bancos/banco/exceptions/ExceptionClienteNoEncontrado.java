package com.ejercicio.bancos.banco.exceptions;

public class ExceptionClienteNoEncontrado extends RuntimeException {
    public ExceptionClienteNoEncontrado(String mess) {
        super(mess);
    }
}
