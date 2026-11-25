package org.example.dominio;

import org.example.Algoritmos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CaminhoMinimo {

    private final String origem;
    private final String destino;
    private final boolean temCaminho;
    private final List<String> caminho;
    private final String caminhoFormatado;
    private final int custoTotal;
    private final String tabelaExecucao;
    private final List<Algoritmos.PassoExecucao> passos;

    public CaminhoMinimo(String origem, String destino, boolean temCaminho, List<String> caminho,
                         String caminhoFormatado, int custoTotal, String tabelaExecucao,
                         List<Algoritmos.PassoExecucao> passos) {
        this.origem = origem;
        this.destino = destino;
        this.temCaminho = temCaminho;
        this.caminho = Collections.unmodifiableList(new ArrayList<>(caminho));
        this.caminhoFormatado = caminhoFormatado;
        this.custoTotal = custoTotal;
        this.tabelaExecucao = tabelaExecucao;
        this.passos = Collections.unmodifiableList(new ArrayList<>(passos));
    }

    public String getOrigem() { return origem; }
    public String getDestino() { return destino; }
    public boolean isTemCaminho() { return temCaminho; }
    public List<String> getCaminho() { return caminho; }
    public String getCaminhoFormatado() { return caminhoFormatado; }
    public int getCustoTotal() { return custoTotal; }
    public String getTabelaExecucao() { return tabelaExecucao; }
    public List<Algoritmos.PassoExecucao> getPassos() { return passos; }
}
