package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários com Análise do Valor Limite - Dijkstra e Prim")
public class AlgoritmosValorLimiteTest {

    // =========================================================================
    // MÉTODO 1: Algoritmos.dijkstra(String origem, String destino)
    // =========================================================================

    @Test
    @DisplayName("CT-DIJ-01: Limite |V|=0 (ambos os vértices origem e destino ausentes)")
    void ctDij01_grafoVazioAmbosVerticesInexistentes() {
        Grafo grafo = new Grafo(true, true, true);
        Algoritmos algoritmos = new Algoritmos(grafo);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> algoritmos.dijkstra("A", "B"));
        assertTrue(ex.getMessage().contains("origem 'A' e de destino 'B' não existem"));
    }

    @Test
    @DisplayName("CT-DIJ-02: Limite |V|=1 (vértice de origem ausente, destino presente)")
    void ctDij02_origemInexistenteDestinoExistente() {
        Grafo grafo = new Grafo(true, true, true);
        grafo.adicionarVertice("B");
        Algoritmos algoritmos = new Algoritmos(grafo);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> algoritmos.dijkstra("A", "B"));
        assertTrue(ex.getMessage().contains("vértice de origem 'A' não existe"));
    }

    @Test
    @DisplayName("CT-DIJ-03: Limite |V|=1 (vértice de origem presente, destino ausente)")
    void ctDij03_origemExistenteDestinoInexistente() {
        Grafo grafo = new Grafo(true, true, true);
        grafo.adicionarVertice("A");
        Algoritmos algoritmos = new Algoritmos(grafo);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> algoritmos.dijkstra("A", "B"));
        assertTrue(ex.getMessage().contains("vértice de destino 'B' não existe"));
    }

    @Test
    @DisplayName("CT-DIJ-04: Limite inferior inválido de peso (peso = -1, imediatamente abaixo de 0)")
    void ctDij04_arestaComPesoLimiteNegativoMenosUm() {
        Grafo grafo = new Grafo(true, true, true);
        grafo.adicionarVertice("A");
        grafo.adicionarVertice("B");
        grafo.adicionarAresta("e1", "A", "B", -1);
        Algoritmos algoritmos = new Algoritmos(grafo);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> algoritmos.dijkstra("A", "B"));
        assertTrue(ex.getMessage().contains("pesos negativos"));
    }

    @Test
    @DisplayName("CT-DIJ-05: Limite de distância mínima nula (origem igual ao destino, |V|=2, peso=0)")
    void ctDij05_origemIgualAoDestinoComPesoZero() {
        Grafo grafo = new Grafo(true, true, true);
        grafo.adicionarVertice("A");
        grafo.adicionarVertice("B");
        grafo.adicionarAresta("e1", "A", "B", 0);
        Algoritmos algoritmos = new Algoritmos(grafo);

        Algoritmos.ResultadoCaminho res = algoritmos.dijkstra("A", "A");
        assertTrue(res.temCaminho());
        assertEquals(0, res.getCustoTotal());
        assertEquals(List.of("A"), res.getCaminho());
        assertEquals(1, res.getPassos().size());
    }

    @Test
    @DisplayName("CT-DIJ-06: Limite inferior válido de pesos (peso = 0 e peso = 1) em dígrafo com retorno ao visitado")
    void ctDij06_pesosLimitesZeroEUmEmDigrafoOrientado() {
        Grafo grafo = new Grafo(true, true, true);
        grafo.adicionarVertice("A");
        grafo.adicionarVertice("B");
        grafo.adicionarVertice("C");
        grafo.adicionarAresta("e1", "A", "B", 0);
        grafo.adicionarAresta("e2", "B", "C", 1);
        grafo.adicionarAresta("e3", "B", "A", 0); // retorno para vértice já fechado
        Algoritmos algoritmos = new Algoritmos(grafo);

        Algoritmos.ResultadoCaminho res = algoritmos.dijkstra("A", "C");
        assertTrue(res.temCaminho());
        assertEquals(1, res.getCustoTotal());
        assertEquals(List.of("A", "B", "C"), res.getCaminho());
    }

    @Test
    @DisplayName("CT-DIJ-07: Fronteira de relaxamento (custo-1 relaxa, custo igual e custo+1 mantêm) e nó obsoleto na fila")
    void ctDij07_fronteiraRelaxamentoENoObsoletoNaFila() {
        Grafo grafo = new Grafo(true, true, true);
        grafo.adicionarVertice("A");
        grafo.adicionarVertice("B");
        grafo.adicionarVertice("C");
        grafo.adicionarVertice("D");

        grafo.adicionarAresta("e1", "A", "B", 10);
        grafo.adicionarAresta("e2", "A", "C", 3);
        grafo.adicionarAresta("e3", "C", "B", 6);  // 3 + 6 = 9 (10 - 1): relaxa B p/ 9 e deixa (B,10) obsoleto
        grafo.adicionarAresta("e4", "B", "D", 15); // 9 + 15 = 24
        grafo.adicionarAresta("e5", "B", "D", 15); // empate exato 24 == 24: não relaxa
        grafo.adicionarAresta("e6", "B", "D", 16); // acima do limite 25 > 24: não relaxa

        Algoritmos algoritmos = new Algoritmos(grafo);
        Algoritmos.ResultadoCaminho res = algoritmos.dijkstra("A", "D");

        assertTrue(res.temCaminho());
        assertEquals(24, res.getCustoTotal());
        assertEquals(List.of("A", "C", "B", "D"), res.getCaminho());
    }

    @Test
    @DisplayName("CT-DIJ-08: Grafo não orientado com aresta invertida (destino==u) e aresta disjunta")
    void ctDij08_grafoNaoOrientadoArestaInvertidaEDisjunta() {
        Grafo grafo = new Grafo(false, true, true);
        grafo.adicionarVertice("A");
        grafo.adicionarVertice("B");
        grafo.adicionarVertice("C");
        grafo.adicionarVertice("D");
        grafo.adicionarVertice("E");

        grafo.adicionarAresta("e1", "B", "A", 1); // u="A" aparece como destino da aresta não orientada
        grafo.adicionarAresta("e2", "B", "C", 2);
        grafo.adicionarAresta("e3", "D", "E", 1); // aresta disjunta (nem origem nem destino iguais a u)

        Algoritmos algoritmos = new Algoritmos(grafo);
        Algoritmos.ResultadoCaminho res = algoritmos.dijkstra("A", "C");

        assertTrue(res.temCaminho());
        assertEquals(3, res.getCustoTotal());
        assertEquals(List.of("A", "B", "C"), res.getCaminho());
    }

    @Test
    @DisplayName("CT-DIJ-09: Destino inalcançável (grau de entrada = 0, fila esvazia sem atingir destino)")
    void ctDij09_destinoInalcancavelFilaEsvazia() {
        Grafo grafo = new Grafo(true, true, true);
        grafo.adicionarVertice("A");
        grafo.adicionarVertice("B");
        grafo.adicionarVertice("C");
        grafo.adicionarAresta("e1", "A", "B", 1);

        Algoritmos algoritmos = new Algoritmos(grafo);
        Algoritmos.ResultadoCaminho res = algoritmos.dijkstra("A", "C");

        assertFalse(res.temCaminho());
        assertEquals(-1, res.getCustoTotal());
        assertTrue(res.getCaminho().isEmpty());
    }

    // =========================================================================
    // MÉTODO 2: Algoritmos.prim(String verticeInicial)
    // =========================================================================

    @Test
    @DisplayName("CT-PRI-01: Restrição de domínio (grafo orientado inválido para Prim)")
    void ctPri01_grafoOrientadoLancaExcecao() {
        Grafo grafo = new Grafo(true, true, true);
        grafo.adicionarVertice("A");
        Algoritmos algoritmos = new Algoritmos(grafo);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> algoritmos.prim("A"));
        assertTrue(ex.getMessage().contains("não orientados"));
    }

    @Test
    @DisplayName("CT-PRI-02: Limite inferior inválido de ordem (|V| = 0, grafo vazio)")
    void ctPri02_grafoSemVerticesLancaExcecao() {
        Grafo grafo = new Grafo(false, true, true);
        Algoritmos algoritmos = new Algoritmos(grafo);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> algoritmos.prim(null));
        assertTrue(ex.getMessage().contains("não possui vértices"));
    }

    @Test
    @DisplayName("CT-PRI-03: Limite |V| = 1 com vértice inicial inexistente")
    void ctPri03_verticeInicialInexistenteLancaExcecao() {
        Grafo grafo = new Grafo(false, true, true);
        grafo.adicionarVertice("A");
        Algoritmos algoritmos = new Algoritmos(grafo);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> algoritmos.prim("Z"));
        assertTrue(ex.getMessage().contains("vértice inicial 'Z' não existe"));
    }

    @Test
    @DisplayName("CT-PRI-04: Limite mínimo válido (|V| = 1, |E| = 0, verticeInicial = null)")
    void ctPri04_grafoTrivialUmVerticeZeroArestas() {
        Grafo grafo = new Grafo(false, true, true);
        grafo.adicionarVertice("A");
        Algoritmos algoritmos = new Algoritmos(grafo);

        Algoritmos.ResultadoAGM res = algoritmos.prim(null);
        assertTrue(res.isConexo());
        assertEquals(1, res.getNumComponentes());
        assertEquals(0, res.getPesoTotal());
        assertTrue(res.getArestas().isEmpty());
    }

    @Test
    @DisplayName("CT-PRI-05: Pesos limites (-1, 0, 1, 2), descarte de ciclo na fila e floresta desconexa (|C| = 2)")
    void ctPri05_pesosLimitesDescarteDeCicloEFlorestaDesconexa() {
        Grafo grafo = new Grafo(false, true, true);
        grafo.adicionarVertice("A");
        grafo.adicionarVertice("B");
        grafo.adicionarVertice("C");
        grafo.adicionarVertice("D");
        grafo.adicionarVertice("E");

        // Componente 1 com ciclo e pesos na fronteira {-1, 0, 1}
        grafo.adicionarAresta("e1", "A", "B", -1);
        grafo.adicionarAresta("e2", "B", "C", 0);
        grafo.adicionarAresta("e3", "A", "C", 1); // fecha ciclo, será descartada ao sair da fila

        // Componente 2 desconexa
        grafo.adicionarAresta("e4", "D", "E", 2);

        Algoritmos algoritmos = new Algoritmos(grafo);
        Algoritmos.ResultadoAGM res = algoritmos.prim("B");

        assertFalse(res.isConexo());
        assertEquals(2, res.getNumComponentes());
        assertEquals(1, res.getPesoTotal()); // (-1) + 0 + 2 = 1
        assertEquals(3, res.getArestas().size());
    }
}
