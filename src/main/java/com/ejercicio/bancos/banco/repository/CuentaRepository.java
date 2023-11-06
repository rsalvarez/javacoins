package com.ejercicio.bancos.banco.repository;

import com.ejercicio.bancos.banco.entidades.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
}
