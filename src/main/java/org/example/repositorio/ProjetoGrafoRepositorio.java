package org.example.repositorio;

import org.example.dominio.ProjetoGrafo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjetoGrafoRepositorio extends JpaRepository<ProjetoGrafo, Long> {
}
