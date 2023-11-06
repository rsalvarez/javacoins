package com.ejercicio.bancos.banco.repository;

import com.ejercicio.bancos.banco.entidades.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    public Optional<Cliente> findById(Long id);
    public Optional<Cliente> findByNombre(String nombre);

    public Optional<Cliente> findByDni(String dni);
}
