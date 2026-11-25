package org.example.repositorio;

import org.example.dominio.HistoricoExecucaoAlgoritmo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistoricoExecucaoRepositorio extends JpaRepository<HistoricoExecucaoAlgoritmo, Long> {
    List<HistoricoExecucaoAlgoritmo> findByProjetoGrafoIdOrderByExecutadoEmDesc(Long projetoGrafoId);
}
