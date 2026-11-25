package org.example.integracao.stubs;

import java.util.List;

/**
 * Stub de integração criado no Passo 13 para quebrar o ciclo
 * Algoritmos <-> VisualizadorGrafo, fornecendo a paleta fixa de cores
 * usada por Algoritmos.malgrange() antes da construção de VisualizadorGrafo (Passo 14).
 */
public class StubVisualizadorGrafo {

    public static final List<String> PALETA_CORES = List.of(
            "lightblue",
            "lightgreen",
            "lightyellow",
            "lightcoral"
    );

    public static String obterCor(int indiceComponente) {
        return PALETA_CORES.get(indiceComponente % PALETA_CORES.size());
    }
}
