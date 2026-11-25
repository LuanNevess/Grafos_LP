package org.example;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class AnalisaGrafo {
    private final Grafo grafo;

    public AnalisaGrafo(Grafo grafo) {
        this.grafo = grafo;
    }

    public int ordemDoGrafo() {
        return grafo.getVertices().size();
    }

    public int grauDoVertice(String vertice) {
        List<String> vertices = grafo.getVertices();
        List<List<Integer>> matriz = grafo.getMatriz();
        int idx = indiceDoVertice(vertice, vertices);

        int grau = 0;
        int numArestas = numArestas(matriz);

        for (int col = 0; col < numArestas; col++) {
            if (matriz.get(idx).get(col) != 0) {
                grau += ehLaco(idx, col, matriz, vertices.size()) ? 2 : 1;
            }
        }

        return grau;
    }

    public int grauSaida(String vertice) {
        return contarGrauDirecionado(vertice, true);
    }

    public int grauEntrada(String vertice) {
        return contarGrauDirecionado(vertice, false);
    }

    public List<String> listarLacos() {
        List<String> vertices = grafo.getVertices();
        List<String> arestas = grafo.getArestas();
        List<List<Integer>> matriz = grafo.getMatriz();
        List<String> resultado = new ArrayList<>();

        int numArestas = numArestas(matriz);

        for (int col = 0; col < numArestas; col++) {
            for (int idx = 0; idx < vertices.size(); idx++) {
                if (matriz.get(idx).get(col) != 0 && ehLaco(idx, col, matriz, vertices.size())) {
                    resultado.add(arestas.get(col) + " (em " + vertices.get(idx) + ")");
                }
            }
        }

        return resultado;
    }

    public List<String> listarVerticesFolha() {
        List<String> resultado = new ArrayList<>();
        for (String vertice : grafo.getVertices()) {
            if (grauDoVertice(vertice) == 1) {
                resultado.add(vertice);
            }
        }
        return resultado;
    }

    public List<String> listarFontes() {
        List<String> resultado = new ArrayList<>();
        for (String vertice : grafo.getVertices()) {
            if (grauEntrada(vertice) == 0 && grauSaida(vertice) > 0) {
                resultado.add(vertice);
            }
        }
        return resultado;
    }

    public List<String> listarSumidouros() {
        List<String> resultado = new ArrayList<>();
        for (String vertice : grafo.getVertices()) {
            if (grauSaida(vertice) == 0 && grauEntrada(vertice) > 0) {
                resultado.add(vertice);
            }
        }
        return resultado;
    }

    private int contarGrauDirecionado(String vertice, boolean saida) {
        List<String> vertices = grafo.getVertices();
        List<List<Integer>> matriz = grafo.getMatriz();
        int idx = indiceDoVertice(vertice, vertices);

        int grau = 0;
        int numArestas = numArestas(matriz);

        for (int col = 0; col < numArestas; col++) {
            int valor = matriz.get(idx).get(col);
            if (valor == 0) continue;

            boolean laco = ehLaco(idx, col, matriz, vertices.size());

            if (laco) {
                grau += 1;
            } else if (saida && valor > 0) {
                grau += 1;
            } else if (!saida && valor < 0) {
                grau += 1;
            }
        }

        return grau;
    }

    private int indiceDoVertice(String vertice, List<String> vertices) {
        int idx = vertices.indexOf(vertice);
        if (idx == -1) {
            throw new IllegalArgumentException("-> Erro: O vértice '" + vertice + "' não existe.");
        }
        return idx;
    }

    private boolean ehLaco(int idx, int col, List<List<Integer>> matriz, int numVertices) {
        for (int linha = 0; linha < numVertices; linha++) {
            if (linha != idx && matriz.get(linha).get(col) != 0) {
                return false;
            }
        }
        return true;
    }

    private int numArestas(List<List<Integer>> matriz) {
        return matriz.isEmpty() ? 0 : matriz.get(0).size();
    }

    public boolean isSimples() {
        List<String> vertices = grafo.getVertices();
        List<List<Integer>> matriz = grafo.getMatriz();
        int numArestas = numArestas(matriz);

        List<String> paresVistos = new ArrayList<>();

        for (int col = 0; col < numArestas; col++) {
            int primeiro = -1, segundo = -1;
            for (int linha = 0; linha < vertices.size(); linha++) {
                if (matriz.get(linha).get(col) != 0) {
                    if (primeiro == -1) primeiro = linha;
                    else segundo = linha;
                }
            }

            if (segundo == -1) {
                return false;
            }

            String chave = Math.min(primeiro, segundo) + "-" + Math.max(primeiro, segundo);
            if (paresVistos.contains(chave)) {
                return false;
            }
            paresVistos.add(chave);
        }

        return true;
    }

    public boolean isRegular() {
        List<String> vertices = grafo.getVertices();
        if (vertices.isEmpty()) {
            return false;
        }

        int grauReferencia = grauDoVertice(vertices.get(0));
        for (String vertice : vertices) {
            if (grauDoVertice(vertice) != grauReferencia) {
                return false;
            }
        }
        return true;
    }

    public boolean isCompleto() {
        List<String> vertices = grafo.getVertices();
        List<List<Integer>> matriz = grafo.getMatriz();
        int n = vertices.size();

        if (n <= 1) {
            return false;
        }

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (!existeArestaEntre(i, j, matriz)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isFonteSumidouro() {
        if (!grafo.isOrientado()) {
            throw new IllegalArgumentException("-> Erro: Classificação fonte-sumidouro só se aplica a grafos orientados.");
        }

        for (String vertice : grafo.getVertices()) {
            boolean ehFonte = grauEntrada(vertice) == 0;
            boolean ehSumidouro = grauSaida(vertice) == 0;

            if (!ehFonte && !ehSumidouro) {
                return false; // vértice misto: tem entrada E saída
            }
        }
        return true;
    }

    private boolean existeArestaEntre(int idx1, int idx2, List<List<Integer>> matriz) {
        int numArestas = numArestas(matriz);
        for (int col = 0; col < numArestas; col++) {
            if (matriz.get(idx1).get(col) != 0 && matriz.get(idx2).get(col) != 0) {
                return true;
            }
        }
        return false;
    }

    public List<String> fechoTransitivoDireto(String vertice) {
        return calcularFecho(vertice, true);
    }

    public List<String> fechoTransitivoInverso(String vertice) {
        return calcularFecho(vertice, false);
    }

    private List<String> calcularFecho(String vertice, boolean direto) {
        List<String> vertices = grafo.getVertices();
        List<List<Integer>> matriz = grafo.getMatriz();
        int idxInicial = indiceDoVertice(vertice, vertices);
        int numArestas = numArestas(matriz);

        Set<Integer> visitados = new LinkedHashSet<>();
        Queue<Integer> fila = new LinkedList<>();
        fila.add(idxInicial);

        while (!fila.isEmpty()) {
            int atual = fila.poll();

            for (int col = 0; col < numArestas; col++) {
                int valorAtual = matriz.get(atual).get(col);
                boolean origemAqui = direto ? valorAtual > 0 : valorAtual < 0;

                if (origemAqui) {
                    for (int prox = 0; prox < vertices.size(); prox++) {
                        if (prox == atual) continue;
                        int valorProx = matriz.get(prox).get(col);
                        boolean destinoAli = direto ? valorProx < 0 : valorProx > 0;

                        if (destinoAli && !visitados.contains(prox)) {
                            visitados.add(prox);
                            fila.add(prox);
                        }
                    }
                }
            }
        }

        List<String> resultado = new ArrayList<>();
        for (int idx : visitados) {
            resultado.add(vertices.get(idx));
        }
        return resultado;
    }
}