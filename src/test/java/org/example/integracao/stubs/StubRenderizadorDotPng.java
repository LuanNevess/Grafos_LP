package org.example.integracao.stubs;

import org.example.RenderizadorDotPng;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Stub de integração (Passos 3A, 3B e 4 -> substituído por RenderizadorGraphvizPadrao no Passo 5)
 * que grava assinatura binária PNG mínima sem inicializar o motor externo Graphviz/V8.
 */
public class StubRenderizadorDotPng implements RenderizadorDotPng {

    private static final byte[] ASSINATURA_PNG_MINIMA =
            new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    private String ultimoDotRecebido;

    @Override
    public void renderizar(String conteudoDot, String caminhoPng) throws IOException {
        this.ultimoDotRecebido = conteudoDot;
        Path destino = Path.of(caminhoPng);
        if (destino.getParent() != null) {
            Files.createDirectories(destino.getParent());
        }
        Files.write(destino, ASSINATURA_PNG_MINIMA);
    }

    public String getUltimoDotRecebido() {
        return ultimoDotRecebido;
    }
}
