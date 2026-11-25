package org.example.integracao.stubs;

import org.example.dominio.HistoricoExecucaoAlgoritmo;
import org.example.dominio.ProjetoGrafo;

/**
 * Stub de integração criado no Passo 16 para quebrar o ciclo bidirecional
 * ProjetoGrafo <-> HistoricoExecucaoAlgoritmo, fornecendo um registro fixo
 * em memória antes da construção de HistoricoExecucaoAlgoritmo (Passo 17).
 */
public class StubHistoricoExecucaoAlgoritmo extends HistoricoExecucaoAlgoritmo {

    public StubHistoricoExecucaoAlgoritmo(ProjetoGrafo projeto) {
        super(projeto, "DIJKSTRA_STUB", "origem=A, destino=B", "custo=0 (stub)");
        setId(1L);
    }

    @Override
    public String getAlgoritmo() {
        return "DIJKSTRA_STUB";
    }

    @Override
    public String getResumoResultado() {
        return "custo=0 (stub)";
    }
}
