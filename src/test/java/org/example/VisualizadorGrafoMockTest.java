package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes com Objeto Mock (Mockito) - VisualizadorGrafo")
public class VisualizadorGrafoMockTest {

    @Mock
    private RenderizadorDotPng renderizadorMock;

    @Test
    @DisplayName("CT-MOCK-01: Deve delegar geração de PNG ao objeto Mock com descrição DOT exata")
    void ctMock01_deveRenderizarGrafoValidoDelegandoAoMock() throws IOException {
        Grafo grafo = new Grafo(true, true, true);
        grafo.adicionarVertice("A");
        grafo.adicionarVertice("B");
        grafo.adicionarAresta("e1", "A", "B", 5);

        VisualizadorGrafo visualizador = new VisualizadorGrafo(grafo, renderizadorMock);
        visualizador.gerarImagemPng("saida/teste_mock.png");

        ArgumentCaptor<String> captorDot = ArgumentCaptor.forClass(String.class);
        verify(renderizadorMock, times(1)).renderizar(captorDot.capture(), eq("saida/teste_mock.png"));

        String dotGerado = captorDot.getValue();
        assertTrue(dotGerado.startsWith("digraph G {"));
        assertTrue(dotGerado.contains("A -> B [label=\"5\", id=\"e1\"];"));
    }

    @Test
    @DisplayName("CT-MOCK-02: Não deve acionar o objeto Mock quando o grafo estiver vazio")
    void ctMock02_naoDeveAcionarMockQuandoGrafoVazio() {
        Grafo grafoVazio = new Grafo(false, false, true);
        VisualizadorGrafo visualizador = new VisualizadorGrafo(grafoVazio, renderizadorMock);

        assertThrows(IllegalStateException.class,
                () -> visualizador.gerarImagemPng("saida/vazio.png"));

        verifyNoInteractions(renderizadorMock);
    }

    @Test
    @DisplayName("CT-MOCK-03: Deve invocar o objeto Mock duas vezes ao renderizar Malgrange (original e reduzido)")
    void ctMock03_deveInvocarMockDuasVezesNoMalgrange() throws IOException {
        Grafo digrafo = new Grafo(true, false, true);
        digrafo.adicionarVertice("V1");
        digrafo.adicionarVertice("V2");
        digrafo.adicionarAresta("e1", "V1", "V2", 1);
        digrafo.adicionarAresta("e2", "V2", "V1", 1);

        Algoritmos.ResultadoMalgrange resMalgrange = new Algoritmos(digrafo).malgrange();
        VisualizadorGrafo visualizador = new VisualizadorGrafo(digrafo, renderizadorMock);

        visualizador.gerarImagensMalgrangeInstancia(
                resMalgrange, "saida/cfc_orig.png", "saida/cfc_red.png");

        ArgumentCaptor<String> captorDot = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> captorCaminho = ArgumentCaptor.forClass(String.class);
        verify(renderizadorMock, times(2)).renderizar(captorDot.capture(), captorCaminho.capture());

        List<String> caminhos = captorCaminho.getAllValues();
        assertEquals(List.of("saida/cfc_orig.png", "saida/cfc_red.png"), caminhos);
        assertTrue(captorDot.getAllValues().get(1).contains("digraph Gr {"));
    }

    @Test
    @DisplayName("CT-MOCK-04: Deve propagar IOException simulada pelo objeto Mock ao falhar escrita")
    void ctMock04_devePropagarIOExceptionSimuladaPeloMock() throws IOException {
        Grafo grafo = new Grafo(false, false, true);
        grafo.adicionarVertice("X");

        doThrow(new IOException("Falha simulada de disco no motor Graphviz"))
                .when(renderizadorMock).renderizar(anyString(), eq("saida/erro.png"));

        VisualizadorGrafo visualizador = new VisualizadorGrafo(grafo, renderizadorMock);

        IOException ex = assertThrows(IOException.class,
                () -> visualizador.gerarImagemPng("saida/erro.png"));
        assertEquals("Falha simulada de disco no motor Graphviz", ex.getMessage());
        verify(renderizadorMock, times(1)).renderizar(anyString(), eq("saida/erro.png"));
    }
}
