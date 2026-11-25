package org.example.integracao.stubs;

import org.example.dominio.ProjetoGrafo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Stub de integração (Passo 4 -> substituído por ProjetoGrafoRepositorio JPA/H2 no Passo 5)
 * fornecendo persistência determinística em memória.
 */
public class StubProjetoGrafoRepositorio {

    private final Map<Long, ProjetoGrafo> bancoEmMemoria = new LinkedHashMap<>();
    private final AtomicLong sequenciaId = new AtomicLong(1L);

    public ProjetoGrafo save(ProjetoGrafo entidade) {
        if (entidade.getId() == null) {
            entidade.setId(sequenciaId.getAndIncrement());
        }
        bancoEmMemoria.put(entidade.getId(), entidade);
        return entidade;
    }

    public Optional<ProjetoGrafo> findById(Long id) {
        return Optional.ofNullable(bancoEmMemoria.get(id));
    }

    public List<ProjetoGrafo> findAll() {
        return new ArrayList<>(bancoEmMemoria.values());
    }

    public void delete(ProjetoGrafo entidade) {
        if (entidade != null && entidade.getId() != null) {
            bancoEmMemoria.remove(entidade.getId());
        }
    }
}
