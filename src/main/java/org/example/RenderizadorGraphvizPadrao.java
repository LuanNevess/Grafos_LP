package org.example;

import br.com.davesmartins.api.Graph;

import java.io.File;
import java.io.IOException;

/**
 * Implementação padrão de {@link RenderizadorDotPng} acoplada à biblioteca externa Graph Api View.
 */
public class RenderizadorGraphvizPadrao implements RenderizadorDotPng {

    @Override
    public void renderizar(String conteudoDot, String caminhoPng) throws IOException {
        File arquivoDestino = new File(caminhoPng);
        File diretorioPai = arquivoDestino.getParentFile();
        if (diretorioPai != null && !diretorioPai.exists()) {
            diretorioPai.mkdirs();
        }
        Graph.createStringDotToPng(conteudoDot, caminhoPng);
    }
}
