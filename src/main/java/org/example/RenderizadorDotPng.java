package org.example;

import java.io.IOException;

/**
 * Abstração do motor de renderização de descrições DOT (Graphviz) para imagens PNG,
 * permitindo inversão de dependência e testes isolados com objetos Mock.
 */
public interface RenderizadorDotPng {
    void renderizar(String conteudoDot, String caminhoPng) throws IOException;
}
