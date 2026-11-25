package org.example.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComponenteConexa {

    private final int indice;
    private final List<String> vertices;
    private final List<Aresta> arestas;
    private final int pesoTotal;
    private final String cor;

    public ComponenteConexa(int indice, List<String> vertices, List<Aresta> arestas, int pesoTotal, String cor) {
        this.indice = indice;
        this.vertices = Collections.unmodifiableList(new ArrayList<>(vertices));
        this.arestas = arestas != null ? Collections.unmodifiableList(new ArrayList<>(arestas)) : Collections.emptyList();
        this.pesoTotal = pesoTotal;
        this.cor = cor;
    }

    public int getIndice() { return indice; }
    public List<String> getVertices() { return vertices; }
    public List<Aresta> getArestas() { return arestas; }
    public int getPesoTotal() { return pesoTotal; }
    public String getCor() { return cor; }
}
