package org.example.integracao.stubs;

import org.example.AnalisaGrafo;
import org.example.Grafo;

import java.util.List;
import java.util.Map;

/**
 * Stub de integração (Passo 3A -> substituído pela classe real no Passo 3B)
 * que simula fechos transitivos pré-computados para testar Algoritmos.malgrange().
 */
public class StubAnalisaGrafo extends AnalisaGrafo {

    private static final Map<String, List<String>> FECHO_DIRETO_FIXO = Map.of(
            "A", List.of("B", "C", "D"),
            "B", List.of("C", "A", "D"),
            "C", List.of("A", "B", "D"),
            "D", List.of()
    );

    private static final Map<String, List<String>> FECHO_INVERSO_FIXO = Map.of(
            "A", List.of("C", "B"),
            "B", List.of("A", "C"),
            "C", List.of("B", "A"),
            "D", List.of("A", "B", "C")
    );

    public StubAnalisaGrafo(Grafo grafo) {
        super(grafo);
    }

    @Override
    public List<String> fechoTransitivoDireto(String vertice) {
        return FECHO_DIRETO_FIXO.getOrDefault(vertice, List.of());
    }

    @Override
    public List<String> fechoTransitivoInverso(String vertice) {
        return FECHO_INVERSO_FIXO.getOrDefault(vertice, List.of());
    }
}
