package org.example.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResumoPropriedadesGrafo {

    private final boolean orientado;
    private final boolean valorado;
    private final int ordem;
    private final int tamanho;
    private final boolean simples;
    private final boolean regular;
    private final boolean completo;
    private final boolean fonteSumidouro;
    private final boolean possuiPesosNegativos;
    private final List<String> lacos;
    private final List<String> folhas;
    private final List<String> fontes;
    private final List<String> sumidouros;
    private final List<Vertice> vertices;
    private final List<Aresta> arestas;
    private final Map<String, Object> matrizIncidencia;
    private final String dot;

    public ResumoPropriedadesGrafo(boolean orientado, boolean valorado, int ordem, int tamanho,
                                   boolean simples, boolean regular, boolean completo,
                                   boolean fonteSumidouro, boolean possuiPesosNegativos,
                                   List<String> lacos, List<String> folhas, List<String> fontes,
                                   List<String> sumidouros, List<Vertice> vertices, List<Aresta> arestas,
                                   List<String> nomesVertices, List<String> nomesArestas,
                                   List<List<Integer>> valoresMatriz, String dot) {
        this.orientado = orientado;
        this.valorado = valorado;
        this.ordem = ordem;
        this.tamanho = tamanho;
        this.simples = simples;
        this.regular = regular;
        this.completo = completo;
        this.fonteSumidouro = fonteSumidouro;
        this.possuiPesosNegativos = possuiPesosNegativos;
        this.lacos = Collections.unmodifiableList(new ArrayList<>(lacos));
        this.folhas = Collections.unmodifiableList(new ArrayList<>(folhas));
        this.fontes = Collections.unmodifiableList(new ArrayList<>(fontes));
        this.sumidouros = Collections.unmodifiableList(new ArrayList<>(sumidouros));
        this.vertices = Collections.unmodifiableList(new ArrayList<>(vertices));
        this.arestas = Collections.unmodifiableList(new ArrayList<>(arestas));

        Map<String, Object> mat = new LinkedHashMap<>();
        mat.put("vertices", List.copyOf(nomesVertices));
        mat.put("arestas", List.copyOf(nomesArestas));
        List<List<Integer>> copiaValores = new ArrayList<>();
        for (List<Integer> linha : valoresMatriz) {
            copiaValores.add(List.copyOf(linha));
        }
        mat.put("valores", Collections.unmodifiableList(copiaValores));
        mat.put("quantidadeVertices", nomesVertices.size());
        mat.put("quantidadeArestas", nomesArestas.size());
        this.matrizIncidencia = Collections.unmodifiableMap(mat);
        this.dot = dot;
    }

    public boolean isOrientado() { return orientado; }
    public boolean isValorado() { return valorado; }
    public int getOrdem() { return ordem; }
    public int getTamanho() { return tamanho; }
    public boolean isSimples() { return simples; }
    public boolean isRegular() { return regular; }
    public boolean isCompleto() { return completo; }
    public boolean isFonteSumidouro() { return fonteSumidouro; }
    public boolean isPossuiPesosNegativos() { return possuiPesosNegativos; }
    public List<String> getLacos() { return lacos; }
    public List<String> getFolhas() { return folhas; }
    public List<String> getFontes() { return fontes; }
    public List<String> getSumidouros() { return sumidouros; }
    public List<Vertice> getVertices() { return vertices; }
    public List<Aresta> getArestas() { return arestas; }
    public Map<String, Object> getMatrizIncidencia() { return matrizIncidencia; }
    public String getDot() { return dot; }
}
