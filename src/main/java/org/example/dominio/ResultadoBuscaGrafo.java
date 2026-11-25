package org.example.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResultadoBuscaGrafo {

    private final String tipoBusca; // BFS ou DFS
    private final String verticeInicial;
    private final List<String> ordemVisita;
    private final String ordemVisitaFormatada;
    private final List<String> verticesAlcancados;
    private final List<String> verticesNaoAlcancados;
    private final Map<String, Integer> niveisOuProfundidades;
    private final Map<String, String> antecessores;
    private final List<Aresta> arestasArvore;
    private final String tabelaExecucao;
    private final String relatorio;

    public ResultadoBuscaGrafo(String tipoBusca, String verticeInicial, List<String> ordemVisita,
                               String ordemVisitaFormatada, List<String> verticesAlcancados,
                               List<String> verticesNaoAlcancados, Map<String, Integer> niveisOuProfundidades,
                               Map<String, String> antecessores, List<Aresta> arestasArvore,
                               String tabelaExecucao, String relatorio) {
        this.tipoBusca = tipoBusca;
        this.verticeInicial = verticeInicial;
        this.ordemVisita = Collections.unmodifiableList(new ArrayList<>(ordemVisita));
        this.ordemVisitaFormatada = ordemVisitaFormatada;
        this.verticesAlcancados = Collections.unmodifiableList(new ArrayList<>(verticesAlcancados));
        this.verticesNaoAlcancados = Collections.unmodifiableList(new ArrayList<>(verticesNaoAlcancados));
        this.niveisOuProfundidades = Collections.unmodifiableMap(new LinkedHashMap<>(niveisOuProfundidades));
        this.antecessores = Collections.unmodifiableMap(new LinkedHashMap<>(antecessores));
        this.arestasArvore = Collections.unmodifiableList(new ArrayList<>(arestasArvore));
        this.tabelaExecucao = tabelaExecucao;
        this.relatorio = relatorio;
    }

    public String getTipoBusca() { return tipoBusca; }
    public String getVerticeInicial() { return verticeInicial; }
    public List<String> getOrdemVisita() { return ordemVisita; }
    public String getOrdemVisitaFormatada() { return ordemVisitaFormatada; }
    public List<String> getVerticesAlcancados() { return verticesAlcancados; }
    public List<String> getVerticesNaoAlcancados() { return verticesNaoAlcancados; }
    public Map<String, Integer> getNiveisOuProfundidades() { return niveisOuProfundidades; }
    public Map<String, String> getAntecessores() { return antecessores; }
    public List<Aresta> getArestasArvore() { return arestasArvore; }
    public String getTabelaExecucao() { return tabelaExecucao; }
    public String getRelatorio() { return relatorio; }
}
