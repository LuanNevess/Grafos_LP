package org.example.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArvoreGeradoraMinima {

    private final String algoritmo;
    private final boolean conexo;
    private final int numeroComponentes;
    private final int pesoTotal;
    private final List<Aresta> arestas;
    private final List<ComponenteConexa> componentes;

    public ArvoreGeradoraMinima(String algoritmo, boolean conexo, int numeroComponentes,
                                int pesoTotal, List<Aresta> arestas, List<ComponenteConexa> componentes) {
        this.algoritmo = algoritmo;
        this.conexo = conexo;
        this.numeroComponentes = numeroComponentes;
        this.pesoTotal = pesoTotal;
        this.arestas = Collections.unmodifiableList(new ArrayList<>(arestas));
        this.componentes = Collections.unmodifiableList(new ArrayList<>(componentes));
    }

    public String getAlgoritmo() { return algoritmo; }
    public boolean isConexo() { return conexo; }
    public int getNumeroComponentes() { return numeroComponentes; }
    public int getPesoTotal() { return pesoTotal; }
    public List<Aresta> getArestas() { return arestas; }
    public List<ComponenteConexa> getComponentes() { return componentes; }
}
