package org.example;

import br.com.davesmartins.api.Graph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Responsável por converter a estrutura de dados do grafo para a linguagem DOT (Graphviz)
 * e gerar a visualização gráfica em imagem PNG via Graph Api View.
 */
public class VisualizadorGrafo {

    /**
     * Paleta padrão de cores Graphviz para destacar as componentes fortemente conexas.
     */
    public static final List<String> PALETA_CORES = List.of(
            "lightblue",
            "lightgreen",
            "lightyellow",
            "lightcoral",
            "lightpink",
            "lightsalmon",
            "lightcyan",
            "plum",
            "khaki",
            "lavender",
            "wheat",
            "mistyrose",
            "palegreen",
            "powderblue",
            "gold",
            "aquamarine",
            "thistle",
            "peachpuff",
            "bisque",
            "lemonchiffon"
    );

    private final Grafo grafo;
    private final RenderizadorDotPng renderizador;

    public VisualizadorGrafo(Grafo grafo) {
        this(grafo, new RenderizadorGraphvizPadrao());
    }

    public VisualizadorGrafo(Grafo grafo, RenderizadorDotPng renderizador) {
        if (renderizador == null) {
            throw new IllegalArgumentException("-> Erro: O renderizador não pode ser nulo.");
        }
        this.grafo = grafo;
        this.renderizador = renderizador;
    }

    /**
     * Converte o estado atual do grafo para uma descrição válida na linguagem DOT.
     *
     * @return String com a descrição DOT do grafo.
     * @throws IllegalStateException se o grafo estiver vazio (sem vértices).
     */
    public String gerarDot() {
        return gerarDot(this.grafo);
    }

    /**
     * Método estático utilitário para converter qualquer Grafo em DOT.
     *
     * @param g Grafo a ser convertido.
     * @return String contendo a descrição DOT.
     * @throws IllegalStateException se o grafo estiver vazio (sem vértices).
     */
    public static String gerarDot(Grafo g) {
        if (g == null || g.getVertices().isEmpty()) {
            throw new IllegalStateException("-> Erro: O grafo está vazio (sem vértices). Não é possível gerar imagem.");
        }

        StringBuilder sb = new StringBuilder();
        boolean orientado = g.isOrientado();
        boolean valorado = g.isValorado();

        if (orientado) {
            sb.append("digraph G {\n");
        } else {
            sb.append("graph G {\n");
        }

        String conector = orientado ? " -> " : " -- ";

        // Todos os vértices, na ordem do grafo (preserva a ordem e os vértices isolados)
        for (String v : g.getVertices()) {
            sb.append("  ").append(formatarIdentificador(v)).append(";\n");
        }

        // Arestas: o nome vai no atributo id e, se valorado, o peso vai em label
        for (Grafo.ArestaInfo a : g.getArestasInfo()) {
            sb.append("  ")
                    .append(formatarIdentificador(a.getOrigem()))
                    .append(conector)
                    .append(formatarIdentificador(a.getDestino()))
                    .append(" [");

            if (valorado) {
                sb.append("label=\"").append(a.getPeso()).append("\", ");
            }

            sb.append("id=\"").append(a.getNome().replace("\"", "\\\"")).append("\"];\n");
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Salva a descrição DOT atual em um arquivo texto no caminho especificado.
     *
     * @param caminhoDot Caminho do arquivo .dot de destino.
     * @throws IOException em caso de erro ao escrever o arquivo.
     */
    public void salvarArquivoDot(String caminhoDot) throws IOException {
        salvarArquivoDot(this.grafo, caminhoDot);
    }

    /**
     * Salva a descrição DOT de qualquer Grafo em um arquivo texto no caminho especificado.
     *
     * @param g Grafo a ser convertido e salvo.
     * @param caminhoDot Caminho do arquivo .dot de destino.
     * @throws IOException em caso de erro ao escrever o arquivo.
     */
    public static void salvarArquivoDot(Grafo g, String caminhoDot) throws IOException {
        String dot = gerarDot(g);
        File file = new File(caminhoDot);
        garantirDiretorioPai(file);

        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write(dot);
        }
    }

    /**
     * Gera e salva imagem PNG a partir de um arquivo .dot existente, utilizando Graph.createFileDotToPng.
     *
     * @param caminhoDot Caminho do arquivo .dot de origem.
     * @param caminhoPng Caminho do arquivo PNG de destino.
     * @throws IOException em caso de erro na geração ou salvamento da imagem.
     */
    public static void gerarImagemPngDeArquivoDot(String caminhoDot, String caminhoPng) throws IOException {
        File filePng = new File(caminhoPng);
        garantirDiretorioPai(filePng);
        Graph.createFileDotToPng(caminhoDot, caminhoPng);
    }

    /**
     * Gera e salva a imagem PNG correspondente ao estado atual do grafo.
     *
     * @param caminhoPng Caminho do arquivo PNG de destino (ex: "saida/grafo.png").
     * @throws IOException em caso de erro na geração ou salvamento da imagem.
     * @throws IllegalStateException se o grafo estiver vazio (sem vértices).
     */
    public void gerarImagemPng(String caminhoPng) throws IOException {
        String dot = gerarDot(this.grafo);
        this.renderizador.renderizar(dot, caminhoPng);
    }

    /**
     * Método estático utilitário para gerar e salvar a imagem PNG de qualquer Grafo.
     *
     * @param g Grafo a ser visualizado.
     * @param caminhoPng Caminho do arquivo PNG de destino.
     * @throws IOException em caso de erro na geração ou salvamento da imagem.
     * @throws IllegalStateException se o grafo estiver vazio (sem vértices).
     */
    public static void gerarImagemPng(Grafo g, String caminhoPng) throws IOException {
        String dot = gerarDot(g);
        File filePng = new File(caminhoPng);
        garantirDiretorioPai(filePng);

        Graph.createStringDotToPng(dot, caminhoPng);
    }

    /**
     * Gera a descrição DOT do grafo original com os vértices de cada componente fortemente conexa
     * destacados com uma cor diferente através dos atributos style=filled e fillcolor.
     *
     * @param resultado Resultado da execução do Algoritmo de Malgrange.
     * @return String contendo a descrição DOT.
     */
    public static String gerarDotMalgrangeOriginal(Algoritmos.ResultadoMalgrange resultado) {
        if (resultado == null) {
            throw new IllegalArgumentException("O resultado de Malgrange não pode ser nulo.");
        }
        Grafo g = resultado.getGrafoOriginal();
        StringBuilder sb = new StringBuilder();
        sb.append("digraph G {\n");

        // 1. Destaca os vértices por cor de componente
        for (int i = 0; i < resultado.getComponentes().size(); i++) {
            Set<String> cfc = resultado.getComponentes().get(i);
            String cor = resultado.getCorComponente(i + 1);
            for (String v : cfc) {
                sb.append("  ")
                  .append(formatarIdentificador(v))
                  .append(" [style=filled, fillcolor=")
                  .append(cor)
                  .append("];\n");
            }
        }

        // 2. Adiciona as arestas originais do dígrafo
        boolean valorado = g.isValorado();
        for (Grafo.ArestaInfo a : g.getArestasInfo()) {
            sb.append("  ")
              .append(formatarIdentificador(a.getOrigem()))
              .append(" -> ")
              .append(formatarIdentificador(a.getDestino()));
            if (valorado) {
                sb.append(" [label=\"").append(a.getPeso()).append("\"]");
            }
            sb.append(";\n");
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Gera a descrição DOT do grafo reduzido Gr(S, W), onde cada super-vértice é rotulado
     * pelo conjunto de vértices que representa (ex: "{v2, v3, v4}") com a cor da componente.
     *
     * @param resultado Resultado da execução do Algoritmo de Malgrange.
     * @return String contendo a descrição DOT do grafo reduzido.
     */
    public static String gerarDotGrafoReduzido(Algoritmos.ResultadoMalgrange resultado) {
        if (resultado == null) {
            throw new IllegalArgumentException("O resultado de Malgrange não pode ser nulo.");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("digraph Gr {\n");

        List<Set<String>> componentes = resultado.getComponentes();
        for (int i = 0; i < componentes.size(); i++) {
            Set<String> cfc = componentes.get(i);
            String rotulo = "{" + String.join(", ", cfc) + "}";
            String idNo = "C" + (i + 1);
            String cor = resultado.getCorComponente(i + 1);

            sb.append("  ")
              .append(idNo)
              .append(" [label=\"")
              .append(rotulo.replace("\"", "\\\""))
              .append("\", style=filled, fillcolor=")
              .append(cor)
              .append("];\n");
        }

        Set<String> arestasVistas = new HashSet<>();
        for (Grafo.ArestaInfo a : resultado.getGrafoOriginal().getArestasInfo()) {
            int compOrigem = resultado.getIndiceComponente(a.getOrigem());
            int compDestino = resultado.getIndiceComponente(a.getDestino());

            if (compOrigem != compDestino) {
                String chave = compOrigem + "->" + compDestino;
                if (!arestasVistas.contains(chave)) {
                    arestasVistas.add(chave);
                    sb.append("  C")
                      .append(compOrigem)
                      .append(" -> C")
                      .append(compDestino)
                      .append(";\n");
                }
            }
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Gera e salva as duas imagens PNG resultantes do Algoritmo de Malgrange:
     * 1. Grafo original colorido por componente fortemente conexa.
     * 2. Grafo reduzido com super-vértices rotulados pelos conjuntos de vértices.
     *
     * @param resultado Resultado da execução de Malgrange.
     * @param caminhoPngOriginal Caminho do PNG do grafo original colorido.
     * @param caminhoPngReduzido Caminho do PNG do grafo reduzido.
     * @throws IOException em caso de erro ao gerar ou salvar as imagens.
     */
    public static void gerarImagensMalgrange(Algoritmos.ResultadoMalgrange resultado,
                                            String caminhoPngOriginal,
                                            String caminhoPngReduzido) throws IOException {
        String dotOriginal = gerarDotMalgrangeOriginal(resultado);
        File fileOrig = new File(caminhoPngOriginal);
        garantirDiretorioPai(fileOrig);
        Graph.createStringDotToPng(dotOriginal, caminhoPngOriginal);

        String dotReduzido = gerarDotGrafoReduzido(resultado);
        File fileRed = new File(caminhoPngReduzido);
        garantirDiretorioPai(fileRed);
        Graph.createStringDotToPng(dotReduzido, caminhoPngReduzido);
    }

    /**
     * Versão de instância para gerar as imagens do Algoritmo de Malgrange.
     */
    public void gerarImagensMalgrangeInstancia(Algoritmos.ResultadoMalgrange resultado,
                                             String caminhoPngOriginal,
                                             String caminhoPngReduzido) throws IOException {
        String dotOriginal = gerarDotMalgrangeOriginal(resultado);
        this.renderizador.renderizar(dotOriginal, caminhoPngOriginal);

        String dotReduzido = gerarDotGrafoReduzido(resultado);
        this.renderizador.renderizar(dotReduzido, caminhoPngReduzido);
    }

    /**
     * Gera a descrição DOT da busca em largura (BFS), destacando a raiz (em dourado),
     * os vértices alcançados (com seu respectivo nível na busca em azul claro),
     * os vértices inalcançados (em cinza claro) e as arestas que formam a árvore BFS.
     *
     * @param resultado Resultado da execução do algoritmo BFS.
     * @return String contendo a descrição DOT.
     */
    public static String gerarDotBFS(Algoritmos.ResultadoBFS resultado) {
        if (resultado == null) {
            throw new IllegalArgumentException("-> Erro: O resultado da busca em largura não pode ser nulo.");
        }
        Grafo g = resultado.getGrafoOriginal();
        StringBuilder sb = new StringBuilder();
        boolean orientado = g.isOrientado();

        if (orientado) {
            sb.append("digraph BFS {\n");
        } else {
            sb.append("graph BFS {\n");
        }

        String conector = orientado ? " -> " : " -- ";
        String raiz = resultado.getVerticeInicial();

        // 1. Configura os nós (vértices)
        for (String v : g.getVertices()) {
            sb.append("  ").append(formatarIdentificador(v));
            if (v.equals(raiz)) {
                sb.append(" [style=filled, fillcolor=gold, penwidth=2.0, label=\"")
                  .append(v)
                  .append(" (1º - Raiz, d=0)\"]");
            } else if (resultado.isAlcancado(v)) {
                sb.append(" [style=filled, fillcolor=lightblue, label=\"")
                  .append(v)
                  .append(" (")
                  .append(resultado.getOrdemVisita(v))
                  .append("º, d=")
                  .append(resultado.getNivel(v))
                  .append(")\"]");
            } else {
                sb.append(" [style=filled, fillcolor=\"#d3d3d3\", fontcolor=\"#555555\", label=\"")
                  .append(v)
                  .append(" (Inalcançado)\"]");
            }
            sb.append(";\n");
        }

        // 2. Configura as arestas
        Set<String> nomesArestasArvore = new HashSet<>();
        for (Grafo.ArestaInfo a : resultado.getArestasArvore()) {
            nomesArestasArvore.add(a.getNome());
        }

        boolean valorado = g.isValorado();
        for (Grafo.ArestaInfo a : g.getArestasInfo()) {
            sb.append("  ")
              .append(formatarIdentificador(a.getOrigem()))
              .append(conector)
              .append(formatarIdentificador(a.getDestino()));

            boolean ehArvore = nomesArestasArvore.contains(a.getNome());
            sb.append(" [");
            if (ehArvore) {
                sb.append("color=blue, penwidth=2.5");
            } else {
                sb.append("color=gray, style=dashed");
            }
            if (valorado) {
                sb.append(", label=\"").append(a.getPeso()).append("\"");
            }
            sb.append("];\n");
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Gera e salva a imagem PNG da busca em largura (BFS).
     *
     * @param resultado Resultado da execução do BFS.
     * @param caminhoPng Caminho do arquivo PNG de destino.
     * @throws IOException em caso de erro na geração ou salvamento da imagem.
     */
    public static void gerarImagemBFS(Algoritmos.ResultadoBFS resultado, String caminhoPng) throws IOException {
        String dot = gerarDotBFS(resultado);
        File filePng = new File(caminhoPng);
        garantirDiretorioPai(filePng);
        Graph.createStringDotToPng(dot, caminhoPng);
    }

    /**
     * Versão de instância para gerar imagem da busca em largura (BFS).
     */
    public void gerarImagemBFSInstancia(Algoritmos.ResultadoBFS resultado, String caminhoPng) throws IOException {
        String dot = gerarDotBFS(resultado);
        this.renderizador.renderizar(dot, caminhoPng);
    }

    /**
     * Gera a descrição DOT da busca em profundidade (DFS), destacando a raiz (em dourado),
     * os vértices alcançados numerados com sua ordem final de visitação e profundidade (em azul claro),
     * os vértices inalcançados (em cinza) e as arestas da árvore DFS (em azul reforçado),
     * sem ocultar os demais elementos do grafo (as demais arestas permanecem visíveis em cinza tracejado).
     *
     * @param resultado Resultado da execução do algoritmo DFS.
     * @return String contendo a descrição DOT.
     */
    public static String gerarDotDFS(Algoritmos.ResultadoDFS resultado) {
        if (resultado == null) {
            throw new IllegalArgumentException("-> Erro: O resultado da busca em profundidade não pode ser nulo.");
        }
        Grafo g = resultado.getGrafoOriginal();
        StringBuilder sb = new StringBuilder();
        boolean orientado = g.isOrientado();

        if (orientado) {
            sb.append("digraph DFS {\n");
        } else {
            sb.append("graph DFS {\n");
        }

        String conector = orientado ? " -> " : " -- ";
        String raiz = resultado.getVerticeInicial();

        // 1. Configura os nós (vértices) com a numeração explícita da ordem de visita
        for (String v : g.getVertices()) {
            sb.append("  ").append(formatarIdentificador(v));
            if (v.equals(raiz)) {
                sb.append(" [style=filled, fillcolor=gold, penwidth=2.0, label=\"")
                  .append(v)
                  .append(" (1º - Raiz, p=0)\"]");
            } else if (resultado.isAlcancado(v)) {
                sb.append(" [style=filled, fillcolor=lightblue, label=\"")
                  .append(v)
                  .append(" (")
                  .append(resultado.getOrdemVisita(v))
                  .append("º, p=")
                  .append(resultado.getProfundidade(v))
                  .append(")\"]");
            } else {
                sb.append(" [style=filled, fillcolor=\"#d3d3d3\", fontcolor=\"#555555\", label=\"")
                  .append(v)
                  .append(" (Inalcançado)\"]");
            }
            sb.append(";\n");
        }

        // 2. Configura todas as arestas do grafo sem ocultar nenhuma
        Set<String> nomesArestasArvore = new HashSet<>();
        for (Grafo.ArestaInfo a : resultado.getArestasArvore()) {
            nomesArestasArvore.add(a.getNome());
        }

        boolean valorado = g.isValorado();
        for (Grafo.ArestaInfo a : g.getArestasInfo()) {
            sb.append("  ")
              .append(formatarIdentificador(a.getOrigem()))
              .append(conector)
              .append(formatarIdentificador(a.getDestino()));

            boolean ehArvore = nomesArestasArvore.contains(a.getNome());
            sb.append(" [");
            if (ehArvore) {
                sb.append("color=blue, penwidth=2.5");
            } else {
                sb.append("color=gray, style=dashed");
            }
            if (valorado) {
                sb.append(", label=\"").append(a.getPeso()).append("\"");
            }
            sb.append("];\n");
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Gera e salva a imagem PNG da busca em profundidade (DFS).
     *
     * @param resultado Resultado da execução do DFS.
     * @param caminhoPng Caminho do arquivo PNG de destino.
     * @throws IOException em caso de erro na geração ou salvamento da imagem.
     */
    public static void gerarImagemDFS(Algoritmos.ResultadoDFS resultado, String caminhoPng) throws IOException {
        String dot = gerarDotDFS(resultado);
        File filePng = new File(caminhoPng);
        garantirDiretorioPai(filePng);
        Graph.createStringDotToPng(dot, caminhoPng);
    }

    /**
     * Versão de instância para gerar imagem da busca em profundidade (DFS).
     */
    public void gerarImagemDFSInstancia(Algoritmos.ResultadoDFS resultado, String caminhoPng) throws IOException {
        String dot = gerarDotDFS(resultado);
        this.renderizador.renderizar(dot, caminhoPng);
    }

    private static void garantirDiretorioPai(File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    private static String formatarIdentificador(String id) {
        if (id != null
                && id.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")
                && !PALAVRAS_RESERVADAS_DOT.contains(id.toLowerCase())) {
            return id;
        }
        return "\"" + (id == null ? "" : id.replace("\"", "\\\"")) + "\"";
    }

    private static final Set<String> PALAVRAS_RESERVADAS_DOT =
            Set.of("node", "edge", "graph", "digraph", "subgraph", "strict");
    }