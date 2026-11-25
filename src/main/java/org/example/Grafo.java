package org.example;

import java.util.ArrayList;
import java.util.List;

import org.example.dominio.Aresta;

public class Grafo {
    public static class ArestaInfo extends Aresta {
        public ArestaInfo(String nome, String origem, String destino, int peso) {
            super(nome, origem, destino, peso);
        }
    }

    private List<String> vertices;
    private List<String> arestas;
    private List<ArestaInfo> listaArestas;
    private List<List<Integer>> matriz;

    private final boolean orientado;
    private final boolean valorado;
    private boolean silencioso;

    public Grafo(boolean orientado, boolean valorado) {
        this(orientado, valorado, false);
    }

    public Grafo(boolean orientado, boolean valorado, boolean silencioso) {
        this.vertices = new ArrayList<>();
        this.arestas = new ArrayList<>();
        this.listaArestas = new ArrayList<>();
        this.matriz = new ArrayList<>();
        this.orientado = orientado;
        this.valorado = valorado;
        this.silencioso = silencioso;
    }

    private void imprimir(String msg) {
        if (!silencioso) {
            System.out.println(msg);
        }
    }
    public List<String> getVertices() {
        return new ArrayList<>(vertices);
    }

    public List<String> getArestas() {
        return new ArrayList<>(arestas);
    }

    public void setSilencioso(boolean silencioso) {
        this.silencioso = silencioso;
    }

    public List<List<Integer>> getMatriz() {
        List<List<Integer>> copia = new ArrayList<>();
        for (List<Integer> linha : matriz) {
            copia.add(new ArrayList<>(linha));
        }
        return copia;
    }

    public boolean isOrientado() {
        return orientado;
    }

    public boolean isValorado() {
        return valorado;
    }

    public List<ArestaInfo> getArestasInfo() {
        return new ArrayList<>(listaArestas);
    }

    public boolean possuiPesosNegativos() {
        if (!valorado) {
            return false;
        }
        for (ArestaInfo a : listaArestas) {
            if (a.getPeso() < 0) {
                return true;
            }
        }
        return false;
    }

    public void adicionarVertice(String nomeVertice) {
        if (nomeVertice == null || nomeVertice.trim().isEmpty()) {
            imprimir("-> Erro: O nome do vértice não pode ser vazio.");
            return;
        }

        if (!vertices.contains(nomeVertice)) {
            vertices.add(nomeVertice);

            List<Integer> novaLinha = new ArrayList<>();
            for (int i = 0; i < arestas.size(); i++) {
                novaLinha.add(0);
            }
            matriz.add(novaLinha);

            imprimir("-> Vértice '" + nomeVertice + "' adicionado com sucesso.");
        } else {
            imprimir("-> O vértice '" + nomeVertice + "' já existe.");
        }
    }

    public void adicionarAresta(String nomeAresta, String v1, String v2, int peso) {
        if (nomeAresta == null || nomeAresta.trim().isEmpty()) {
            imprimir("-> Erro: O nome da aresta não pode ser vazio.");
            return;
        }

        if (!vertices.contains(v1) || !vertices.contains(v2)) {
            imprimir("-> Erro: Os vértices '" + v1 + "' e/ou '" + v2 + "' não existem. Crie-os primeiro.");
            return;
        }

        if (arestas.contains(nomeAresta)) {
            imprimir("-> Erro: A aresta '" + nomeAresta + "' já existe.");
            return;
        }

        arestas.add(nomeAresta);
        int idxV1 = vertices.indexOf(v1);
        int idxV2 = vertices.indexOf(v2);

        int valorBase = this.valorado ? peso : 1;
        int valorDestino = this.orientado ? -valorBase : valorBase;

        for (int i = 0; i < vertices.size(); i++) {
            if (i == idxV1 && i == idxV2) {
                matriz.get(i).add(2 * valorBase);
            } else if (i == idxV1) {
                matriz.get(i).add(valorBase);
            } else if (i == idxV2) {
                matriz.get(i).add(valorDestino);
            } else {
                matriz.get(i).add(0);
            }
        }

        listaArestas.add(new ArestaInfo(nomeAresta, v1, v2, valorBase));
        imprimir("-> Aresta '" + nomeAresta + "' conectando (" + v1 + " para " + v2 + ") adicionada.");
    }

    public boolean removerAresta(String nomeAresta) {
        if (nomeAresta == null || nomeAresta.trim().isEmpty()) {
            imprimir("-> Erro: O nome da aresta não pode ser vazio.");
            return false;
        }

        if (!arestas.contains(nomeAresta)) {
            imprimir("-> Erro: A aresta '" + nomeAresta + "' não existe.");
            return false;
        }

        int idxAresta = arestas.indexOf(nomeAresta);
        arestas.remove(idxAresta);
        listaArestas.remove(idxAresta);
        for (List<Integer> linha : matriz) {
            linha.remove(idxAresta);
        }

        imprimir("-> Aresta '" + nomeAresta + "' removida com sucesso.");
        return true;
    }

    public boolean removerAresta(String v1, String v2) {
        if (v1 == null || v1.trim().isEmpty() || v2 == null || v2.trim().isEmpty()) {
            imprimir("-> Erro: Os nomes dos vértices não podem ser vazios.");
            return false;
        }

        if (!vertices.contains(v1) || !vertices.contains(v2)) {
            imprimir("-> Erro: Os vértices '" + v1 + "' e/ou '" + v2 + "' não existem.");
            return false;
        }

        int idxV1 = vertices.indexOf(v1);
        int idxV2 = vertices.indexOf(v2);

        String arestaEncontrada = null;
        for (int c = 0; c < arestas.size(); c++) {
            if (idxV1 == idxV2) {
                boolean ehLaco = matriz.get(idxV1).get(c) != 0;
                if (ehLaco) {
                    for (int l = 0; l < vertices.size(); l++) {
                        if (l != idxV1 && matriz.get(l).get(c) != 0) {
                            ehLaco = false;
                            break;
                        }
                    }
                }
                if (ehLaco) {
                    arestaEncontrada = arestas.get(c);
                    break;
                }
            } else if (this.orientado) {
                if (matriz.get(idxV1).get(c) > 0 && matriz.get(idxV2).get(c) < 0) {
                    arestaEncontrada = arestas.get(c);
                    break;
                }
            } else {
                if (matriz.get(idxV1).get(c) != 0 && matriz.get(idxV2).get(c) != 0) {
                    arestaEncontrada = arestas.get(c);
                    break;
                }
            }
        }

        if (arestaEncontrada != null) {
            return removerAresta(arestaEncontrada);
        } else {
            imprimir("-> Erro: Nenhuma aresta encontrada conectando '" + v1 + "' e '" + v2 + "'.");
            return false;
        }
    }

    public boolean removerVertice(String nomeVertice) {
        if (nomeVertice == null || nomeVertice.trim().isEmpty()) {
            imprimir("-> Erro: O nome do vértice não pode ser vazio.");
            return false;
        }

        if (!vertices.contains(nomeVertice)) {
            imprimir("-> Erro: O vértice '" + nomeVertice + "' não existe.");
            return false;
        }

        int idxV = vertices.indexOf(nomeVertice);

        for (int c = arestas.size() - 1; c >= 0; c--) {
            if (matriz.get(idxV).get(c) != 0) {
                arestas.remove(c);
                listaArestas.remove(c);
                for (List<Integer> linha : matriz) {
                    linha.remove(c);
                }
            }
        }

        matriz.remove(idxV);
        vertices.remove(idxV);

        imprimir("-> Vértice '" + nomeVertice + "' removido com sucesso.");
        return true;
    }

    public void exibirEstadoAtual() {
        System.out.println("\n========================================");
        System.out.println(" GRAFO: " + (orientado ? "[Orientado]" : "[Não Orientado]") + " | " + (valorado ? "[Valorado]" : "[Não Valorado]"));
        System.out.println("========================================");

        if (vertices.isEmpty()) {
            System.out.println("        MATRIZ DE INCIDÊNCIA (Vazia)");
        } else {
            System.out.println("        MATRIZ DE INCIDÊNCIA");

            System.out.print("      ");
            for (String a : arestas) {
                System.out.printf("%5s ", a);
            }
            System.out.println();

            for (int i = 0; i < vertices.size(); i++) {
                System.out.printf("%-4s [", vertices.get(i));
                for (int val : matriz.get(i)) {
                    System.out.printf("%5d ", val);
                }
                System.out.println("]");
            }
        }
        System.out.println("========================================");
    }
}