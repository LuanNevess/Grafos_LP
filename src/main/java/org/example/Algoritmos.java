package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Comparator;

public class Algoritmos {
    private final Grafo grafo;

    public Algoritmos(Grafo grafo) {
        this.grafo = grafo;
    }

    public static class PassoExecucao {
        private final String verticeFechado;
        private final Map<String, Integer> distancias;
        private final List<String> verticesAbertos;

        public PassoExecucao(String verticeFechado, Map<String, Integer> distancias, List<String> verticesAbertos) {
            this.verticeFechado = verticeFechado;
            this.distancias = new LinkedHashMap<>(distancias);
            this.verticesAbertos = new ArrayList<>(verticesAbertos);
        }

        public String getVerticeFechado() {
            return verticeFechado;
        }

        public Map<String, Integer> getDistancias() {
            return Collections.unmodifiableMap(distancias);
        }

        public List<String> getVerticesAbertos() {
            return Collections.unmodifiableList(verticesAbertos);
        }
    }

    public static class ResultadoCaminho {
        private final boolean temCaminho;
        private final List<String> caminho;
        private final int custoTotal;
        private final List<PassoExecucao> passos;

        public ResultadoCaminho(boolean temCaminho, List<String> caminho, int custoTotal, List<PassoExecucao> passos) {
            this.temCaminho = temCaminho;
            this.caminho = caminho != null ? Collections.unmodifiableList(new ArrayList<>(caminho)) : Collections.emptyList();
            this.custoTotal = custoTotal;
            this.passos = passos != null ? Collections.unmodifiableList(new ArrayList<>(passos)) : Collections.emptyList();
        }

        public boolean temCaminho() {
            return temCaminho;
        }

        public List<String> getCaminho() {
            return caminho;
        }

        public int getCustoTotal() {
            return custoTotal;
        }

        public String getCaminhoFormatado() {
            return String.join(" → ", caminho);
        }

        public List<PassoExecucao> getPassos() {
            return passos;
        }
    }

    private static class NoDijkstra implements Comparable<NoDijkstra> {
        private final String vertice;
        private final int distancia;

        public NoDijkstra(String vertice, int distancia) {
            this.vertice = vertice;
            this.distancia = distancia;
        }

        public String getVertice() {
            return vertice;
        }

        public int getDistancia() {
            return distancia;
        }

        @Override
        public int compareTo(NoDijkstra outro) {
            return Integer.compare(this.distancia, outro.distancia);
        }
    }

    public ResultadoCaminho dijkstra(String origem, String destino) {
        List<String> vertices = grafo.getVertices();
        boolean origemExiste = vertices.contains(origem);
        boolean destinoExiste = vertices.contains(destino);

        if (!origemExiste && !destinoExiste) {
            throw new IllegalArgumentException("-> Erro: Os vértices de origem '" + origem + "' e de destino '" + destino + "' não existem.");
        } else if (!origemExiste) {
            throw new IllegalArgumentException("-> Erro: O vértice de origem '" + origem + "' não existe.");
        } else if (!destinoExiste) {
            throw new IllegalArgumentException("-> Erro: O vértice de destino '" + destino + "' não existe.");
        }

        if (grafo.possuiPesosNegativos()) {
            throw new IllegalArgumentException("-> Erro: O algoritmo de Dijkstra não pode ser aplicado a grafos com pesos negativos.");
        }

        List<PassoExecucao> passos = new ArrayList<>();

        if (origem.equals(destino)) {
            Map<String, Integer> distanciasIniciais = new LinkedHashMap<>();
            for (String v : vertices) {
                distanciasIniciais.put(v, v.equals(origem) ? 0 : Integer.MAX_VALUE);
            }
            passos.add(new PassoExecucao(origem, distanciasIniciais, new ArrayList<>()));
            return new ResultadoCaminho(true, List.of(origem), 0, passos);
        }

        Map<String, Integer> distancias = new LinkedHashMap<>();
        Map<String, String> antecessores = new HashMap<>();
        Set<String> visitados = new HashSet<>();

        for (String v : vertices) {
            distancias.put(v, Integer.MAX_VALUE);
            antecessores.put(v, null);
        }
        distancias.put(origem, 0);

        PriorityQueue<NoDijkstra> pq = new PriorityQueue<>();
        pq.add(new NoDijkstra(origem, 0));

        while (!pq.isEmpty()) {
            NoDijkstra atual = pq.poll();
            String u = atual.getVertice();
            int distU = atual.getDistancia();

            if (visitados.contains(u)) {
                continue;
            }
            visitados.add(u);

            List<String> abertos = new ArrayList<>();
            for (String v : vertices) {
                if (!visitados.contains(v)) {
                    abertos.add(v);
                }
            }
            passos.add(new PassoExecucao(u, distancias, abertos));

            if (u.equals(destino)) {
                break;
            }

            for (Grafo.ArestaInfo aresta : grafo.getArestasInfo()) {
                String vizinho = null;
                int peso = aresta.getPeso();

                if (grafo.isOrientado()) {
                    if (aresta.getOrigem().equals(u)) {
                        vizinho = aresta.getDestino();
                    }
                } else {
                    if (aresta.getOrigem().equals(u)) {
                        vizinho = aresta.getDestino();
                    } else if (aresta.getDestino().equals(u)) {
                        vizinho = aresta.getOrigem();
                    }
                }

                if (vizinho != null && !visitados.contains(vizinho)) {
                    if ((long) distU + peso < distancias.get(vizinho)) {
                        distancias.put(vizinho, distU + peso);
                        antecessores.put(vizinho, u);
                        pq.add(new NoDijkstra(vizinho, distU + peso));
                    }
                }
            }
        }

        if (distancias.get(destino) == Integer.MAX_VALUE) {
            return new ResultadoCaminho(false, Collections.emptyList(), -1, passos);
        }

        LinkedList<String> caminho = new LinkedList<>();
        String passo = destino;
        while (passo != null) {
            caminho.addFirst(passo);
            passo = antecessores.get(passo);
        }

        return new ResultadoCaminho(true, caminho, distancias.get(destino), passos);
    }

    public String gerarTabelaExecucao(ResultadoCaminho resultado) {
        if (resultado.getPassos().isEmpty()) {
            return "Nenhum passo executado.";
        }

        List<String> vertices = new ArrayList<>(resultado.getPassos().get(0).getDistancias().keySet());

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%-6s %-18s", "Passo", "Vértice Fechado"));
        for (String v : vertices) {
            sb.append(String.format("%-8s", v));
        }
        sb.append("Abertos\n");

        int numPasso = 1;
        for (PassoExecucao p : resultado.getPassos()) {
            sb.append(String.format("%-6d %-18s", numPasso, p.getVerticeFechado()));
            for (String v : vertices) {
                int dist = p.getDistancias().get(v);
                String distStr = (dist == Integer.MAX_VALUE) ? "∞" : String.valueOf(dist);
                sb.append(String.format("%-8s", distStr));
            }
            sb.append(String.join(", ", p.getVerticesAbertos()));
            sb.append("\n");
            numPasso++;
        }

        return sb.toString();
    }

    public static class ComponenteAGM {
        private final List<String> vertices;
        private final List<Grafo.ArestaInfo> arestas;
        private final int pesoTotal;

        public ComponenteAGM(List<String> vertices, List<Grafo.ArestaInfo> arestas) {
            this.vertices = Collections.unmodifiableList(new ArrayList<>(vertices));
            this.arestas = Collections.unmodifiableList(new ArrayList<>(arestas));
            int soma = 0;
            for (Grafo.ArestaInfo a : arestas) {
                soma += a.getPeso();
            }
            this.pesoTotal = soma;
        }

        public List<String> getVertices() {
            return vertices;
        }

        public List<Grafo.ArestaInfo> getArestas() {
            return arestas;
        }

        public int getPesoTotal() {
            return pesoTotal;
        }
    }

    public static class ResultadoAGM {
        private final List<ComponenteAGM> componentes;
        private final List<Grafo.ArestaInfo> arestas;
        private final int pesoTotal;

        public ResultadoAGM(List<ComponenteAGM> componentes) {
            this.componentes = Collections.unmodifiableList(new ArrayList<>(componentes));

            List<Grafo.ArestaInfo> todas = new ArrayList<>();
            int soma = 0;
            for (ComponenteAGM c : componentes) {
                todas.addAll(c.getArestas());
                soma += c.getPesoTotal();
            }
            this.arestas = Collections.unmodifiableList(todas);
            this.pesoTotal = soma;
        }

        public List<ComponenteAGM> getComponentes() {
            return componentes;
        }

        public List<Grafo.ArestaInfo> getArestas() {
            return arestas;
        }

        public int getPesoTotal() {
            return pesoTotal;
        }

        public int getNumComponentes() {
            return componentes.size();
        }

        public boolean isConexo() {
            return componentes.size() <= 1;
        }
    }

    private int encontrarRaiz(int[] pai, int x) {
        while (pai[x] != x) {
            pai[x] = pai[pai[x]];
            x = pai[x];
        }
        return x;
    }

    public ResultadoAGM kruskal() {
        if (grafo.isOrientado()) {
            throw new IllegalArgumentException("-> Erro: A Árvore Geradora Mínima só se aplica a grafos não orientados.");
        }

        if (grafo.getVertices().isEmpty()) {
            throw new IllegalArgumentException("-> Erro: O grafo não possui vértices.");
        }

        List<String> vertices = grafo.getVertices();
        int n = vertices.size();

        List<Grafo.ArestaInfo> ordenadas = new ArrayList<>(grafo.getArestasInfo());
        ordenadas.sort(Comparator.comparingInt(Grafo.ArestaInfo::getPeso));

        int[] pai = new int[n];
        for (int i = 0; i < n; i++) {
            pai[i] = i;
        }

        List<Grafo.ArestaInfo> escolhidas = new ArrayList<>();

        for (Grafo.ArestaInfo aresta : ordenadas) {
            int raizA = encontrarRaiz(pai, vertices.indexOf(aresta.getOrigem()));
            int raizB = encontrarRaiz(pai, vertices.indexOf(aresta.getDestino()));

            if (raizA != raizB) {
                pai[raizA] = raizB;
                escolhidas.add(aresta);
            }
        }

        // Agrupa vértices e arestas escolhidas pela raiz final de cada componente
        Map<Integer, List<String>> verticesPorRaiz = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            verticesPorRaiz.computeIfAbsent(encontrarRaiz(pai, i), k -> new ArrayList<>()).add(vertices.get(i));
        }

        Map<Integer, List<Grafo.ArestaInfo>> arestasPorRaiz = new HashMap<>();
        for (Grafo.ArestaInfo aresta : escolhidas) {
            int raiz = encontrarRaiz(pai, vertices.indexOf(aresta.getOrigem()));
            arestasPorRaiz.computeIfAbsent(raiz, k -> new ArrayList<>()).add(aresta);
        }

        List<ComponenteAGM> componentes = new ArrayList<>();
        for (Map.Entry<Integer, List<String>> entrada : verticesPorRaiz.entrySet()) {
            List<Grafo.ArestaInfo> doComponente = arestasPorRaiz.getOrDefault(entrada.getKey(), new ArrayList<>());
            componentes.add(new ComponenteAGM(entrada.getValue(), doComponente));
        }

        return new ResultadoAGM(componentes);
    }

    private static class CandidataPrim implements Comparable<CandidataPrim> {
        private final Grafo.ArestaInfo aresta;
        private final String novoVertice;

        public CandidataPrim(Grafo.ArestaInfo aresta, String novoVertice) {
            this.aresta = aresta;
            this.novoVertice = novoVertice;
        }

        @Override
        public int compareTo(CandidataPrim outra) {
            return Integer.compare(this.aresta.getPeso(), outra.aresta.getPeso());
        }
    }

    private void empilharCandidatas(String vertice, List<Grafo.ArestaInfo> todas,
                                    Set<String> naArvore, PriorityQueue<CandidataPrim> fila) {
        for (Grafo.ArestaInfo aresta : todas) {
            String outro;

            if (aresta.getOrigem().equals(vertice)) {
                outro = aresta.getDestino();
            } else if (aresta.getDestino().equals(vertice)) {
                outro = aresta.getOrigem();
            } else {
                continue;
            }

            if (!naArvore.contains(outro)) {
                fila.add(new CandidataPrim(aresta, outro));
            }
        }
    }

    public ResultadoAGM prim() {
        return prim(null);
    }

    public ResultadoAGM prim(String verticeInicial) {
        if (grafo.isOrientado()) {
            throw new IllegalArgumentException("-> Erro: A Árvore Geradora Mínima só se aplica a grafos não orientados.");
        }

        if (grafo.getVertices().isEmpty()) {
            throw new IllegalArgumentException("-> Erro: O grafo não possui vértices.");
        }

        List<String> vertices = grafo.getVertices();
        if (verticeInicial != null && !vertices.contains(verticeInicial)) {
            throw new IllegalArgumentException("-> Erro: O vértice inicial '" + verticeInicial + "' não existe.");
        }

        List<Grafo.ArestaInfo> todas = grafo.getArestasInfo();

        Set<String> naArvore = new HashSet<>();
        List<ComponenteAGM> componentes = new ArrayList<>();

        List<String> raizes = new ArrayList<>();
        if (verticeInicial != null) {
            raizes.add(verticeInicial);
        }
        for (String v : vertices) {
            if (!raizes.contains(v)) {
                raizes.add(v);
            }
        }

        for (String raiz : raizes) {
            if (naArvore.contains(raiz)) {
                continue;
            }

            List<String> verticesDoComponente = new ArrayList<>();
            List<Grafo.ArestaInfo> arestasDoComponente = new ArrayList<>();

            naArvore.add(raiz);
            verticesDoComponente.add(raiz);

            PriorityQueue<CandidataPrim> fila = new PriorityQueue<>();
            empilharCandidatas(raiz, todas, naArvore, fila);

            while (!fila.isEmpty()) {
                CandidataPrim melhor = fila.poll();

                if (naArvore.contains(melhor.novoVertice)) {
                    continue;
                }

                naArvore.add(melhor.novoVertice);
                verticesDoComponente.add(melhor.novoVertice);
                arestasDoComponente.add(melhor.aresta);

                empilharCandidatas(melhor.novoVertice, todas, naArvore, fila);
            }

            // Mantém os vértices na ordem do grafo, igual ao Kruskal, para facilitar a comparação
            verticesDoComponente.sort(Comparator.comparingInt(vertices::indexOf));
            componentes.add(new ComponenteAGM(verticesDoComponente, arestasDoComponente));
        }

        return new ResultadoAGM(componentes);
    }

    public static class PassoMalgrange {
        private final int iteracao;
        private final String verticeEscolhido;
        private final Set<String> fechoDireto;
        private final Set<String> fechoInverso;
        private final Set<String> cfc;
        private final Set<String> verticesRestantes;

        public PassoMalgrange(int iteracao, String verticeEscolhido,
                              Set<String> fechoDireto, Set<String> fechoInverso,
                              Set<String> cfc, Set<String> verticesRestantes) {
            this.iteracao = iteracao;
            this.verticeEscolhido = verticeEscolhido;
            this.fechoDireto = Collections.unmodifiableSet(new LinkedHashSet<>(fechoDireto));
            this.fechoInverso = Collections.unmodifiableSet(new LinkedHashSet<>(fechoInverso));
            this.cfc = Collections.unmodifiableSet(new LinkedHashSet<>(cfc));
            this.verticesRestantes = Collections.unmodifiableSet(new LinkedHashSet<>(verticesRestantes));
        }

        public int getIteracao() {
            return iteracao;
        }

        public String getVerticeEscolhido() {
            return verticeEscolhido;
        }

        public Set<String> getFechoDireto() {
            return fechoDireto;
        }

        public Set<String> getFechoInverso() {
            return fechoInverso;
        }

        public Set<String> getCfc() {
            return cfc;
        }

        public Set<String> getVerticesRestantes() {
            return verticesRestantes;
        }
    }

    public static class ResultadoMalgrange {
        private final List<PassoMalgrange> passos;
        private final List<Set<String>> componentes;
        private final Grafo grafoOriginal;
        private final Grafo grafoReduzido;
        private final Map<String, Integer> verticeParaComponente;
        private final List<String> coresComponentes;

        public ResultadoMalgrange(List<PassoMalgrange> passos,
                                 List<Set<String>> componentes,
                                 Grafo grafoOriginal,
                                 Grafo grafoReduzido,
                                 Map<String, Integer> verticeParaComponente,
                                 List<String> coresComponentes) {
            this.passos = Collections.unmodifiableList(new ArrayList<>(passos));
            this.componentes = Collections.unmodifiableList(new ArrayList<>(componentes));
            this.grafoOriginal = grafoOriginal;
            this.grafoReduzido = grafoReduzido;
            this.verticeParaComponente = Collections.unmodifiableMap(new LinkedHashMap<>(verticeParaComponente));
            this.coresComponentes = Collections.unmodifiableList(new ArrayList<>(coresComponentes));
        }

        public List<PassoMalgrange> getPassos() {
            return passos;
        }

        public List<Set<String>> getComponentes() {
            return componentes;
        }

        public int getNumComponentes() {
            return componentes.size();
        }

        public Grafo getGrafoOriginal() {
            return grafoOriginal;
        }

        public Grafo getGrafoReduzido() {
            return grafoReduzido;
        }

        public Map<String, Integer> getVerticeParaComponente() {
            return verticeParaComponente;
        }

        public int getIndiceComponente(String vertice) {
            Integer idx = verticeParaComponente.get(vertice);
            if (idx == null) {
                throw new IllegalArgumentException("Vértice '" + vertice + "' não encontrado no grafo.");
            }
            return idx;
        }

        public List<String> getCoresComponentes() {
            return coresComponentes;
        }

        public String getCorComponente(int indiceComponente1Based) {
            if (indiceComponente1Based < 1 || indiceComponente1Based > coresComponentes.size()) {
                return "lightgray";
            }
            return coresComponentes.get(indiceComponente1Based - 1);
        }

        public String getTabelaExecucao() {
            if (passos.isEmpty()) {
                return "Nenhum passo executado.\n";
            }

            int wIter = "ITERAÇÃO".length();
            int wVert = "VÉRTICE (v)".length();
            int wDir = "Γ⁺(v) [Fecho Direto]".length();
            int wInv = "Γ⁻(v) [Fecho Inverso]".length();
            int wCfc = "CFC = Γ⁺ ∩ Γ⁻".length();
            int wRest = "VÉRTICES RESTANTES".length();

            List<String[]> linhas = new ArrayList<>();
            for (PassoMalgrange p : passos) {
                String sIter = String.valueOf(p.getIteracao());
                String sVert = p.getVerticeEscolhido();
                String sDir = formatarConjunto(p.getFechoDireto());
                String sInv = formatarConjunto(p.getFechoInverso());
                String sCfc = formatarConjunto(p.getCfc());
                String sRest = formatarConjunto(p.getVerticesRestantes());

                wIter = Math.max(wIter, sIter.length());
                wVert = Math.max(wVert, sVert.length());
                wDir = Math.max(wDir, sDir.length());
                wInv = Math.max(wInv, sInv.length());
                wCfc = Math.max(wCfc, sCfc.length());
                wRest = Math.max(wRest, sRest.length());

                linhas.add(new String[]{sIter, sVert, sDir, sInv, sCfc, sRest});
            }

            String formato = String.format(" %%-%ds | %%-%ds | %%-%ds | %%-%ds | %%-%ds | %%-%ds\n",
                    wIter, wVert, wDir, wInv, wCfc, wRest);

            int totalLargura = wIter + wVert + wDir + wInv + wCfc + wRest + 17;
            String separador = "=".repeat(totalLargura) + "\n";

            StringBuilder sb = new StringBuilder();
            sb.append(separador);
            sb.append(String.format(formato, "ITERAÇÃO", "VÉRTICE (v)", "Γ⁺(v) [Fecho Direto]", "Γ⁻(v) [Fecho Inverso]", "CFC = Γ⁺ ∩ Γ⁻", "VÉRTICES RESTANTES"));
            sb.append(separador);

            for (String[] colunas : linhas) {
                sb.append(String.format(formato, (Object[]) colunas));
            }
            sb.append(separador);

            return sb.toString();
        }

        private static String formatarConjunto(Set<String> set) {
            return "{" + String.join(", ", set) + "}";
        }
    }

    public ResultadoMalgrange malgrange() {
        if (!grafo.isOrientado()) {
            throw new IllegalArgumentException("-> Erro: O algoritmo de Malgrange só se aplica a grafos orientados (dígrafos).");
        }
        if (grafo.getVertices().isEmpty()) {
            throw new IllegalArgumentException("-> Erro: O grafo está vazio (sem vértices).");
        }

        AnalisaGrafo analise = new AnalisaGrafo(grafo);
        List<String> todosVertices = grafo.getVertices();
        Set<String> verticesRestantes = new LinkedHashSet<>(todosVertices);

        List<PassoMalgrange> passos = new ArrayList<>();
        List<Set<String>> componentes = new ArrayList<>();
        Map<String, Integer> verticeParaComponente = new LinkedHashMap<>();

        int iteracao = 1;
        while (!verticesRestantes.isEmpty()) {
            String v = verticesRestantes.iterator().next();

            // Fecho transitivo direto: {v} união com fechoTransitivoDireto(v), restrito aos vértices restantes
            Set<String> fechoDireto = new LinkedHashSet<>();
            fechoDireto.add(v);
            fechoDireto.addAll(analise.fechoTransitivoDireto(v));
            fechoDireto.retainAll(verticesRestantes);

            // Fecho transitivo inverso: {v} união com fechoTransitivoInverso(v), restrito aos vértices restantes
            Set<String> fechoInverso = new LinkedHashSet<>();
            fechoInverso.add(v);
            fechoInverso.addAll(analise.fechoTransitivoInverso(v));
            fechoInverso.retainAll(verticesRestantes);

            // Componente Fortemente Conexa = Γ⁺(v) ∩ Γ⁻(v)
            Set<String> cfc = new LinkedHashSet<>();
            for (String vert : todosVertices) {
                if (fechoDireto.contains(vert) && fechoInverso.contains(vert)) {
                    cfc.add(vert);
                }
            }

            verticesRestantes.removeAll(cfc);

            passos.add(new PassoMalgrange(
                    iteracao,
                    v,
                    fechoDireto,
                    fechoInverso,
                    cfc,
                    new LinkedHashSet<>(verticesRestantes)
            ));

            componentes.add(cfc);
            int compIdx = componentes.size();
            for (String vert : cfc) {
                verticeParaComponente.put(vert, compIdx);
            }

            iteracao++;
        }

        List<String> cores = atribuirCores(componentes.size());
        Grafo grafoReduzido = montarGrafoReduzido(componentes, verticeParaComponente);

        return new ResultadoMalgrange(passos, componentes, grafo, grafoReduzido, verticeParaComponente, cores);
    }

    private Grafo montarGrafoReduzido(List<Set<String>> componentes, Map<String, Integer> verticeParaComponente) {
        Grafo reduzido = new Grafo(true, false, true);

        List<String> nomesSuperVertices = new ArrayList<>();
        for (Set<String> comp : componentes) {
            String nome = "{" + String.join(", ", comp) + "}";
            reduzido.adicionarVertice(nome);
            nomesSuperVertices.add(nome);
        }

        Set<String> arestasVistas = new HashSet<>();
        int arestaContador = 1;

        for (Grafo.ArestaInfo a : grafo.getArestasInfo()) {
            Integer compOrigem = verticeParaComponente.get(a.getOrigem());
            Integer compDestino = verticeParaComponente.get(a.getDestino());

            if (compOrigem != null && compDestino != null && !compOrigem.equals(compDestino)) {
                String chave = compOrigem + "->" + compDestino;
                if (!arestasVistas.contains(chave)) {
                    arestasVistas.add(chave);
                    String nomeOrigem = nomesSuperVertices.get(compOrigem - 1);
                    String nomeDestino = nomesSuperVertices.get(compDestino - 1);
                    reduzido.adicionarAresta("w" + arestaContador++, nomeOrigem, nomeDestino, 1);
                }
            }
        }

        return reduzido;
    }

    private static List<String> atribuirCores(int numComponentes) {
        List<String> cores = new ArrayList<>();
        for (int i = 0; i < numComponentes; i++) {
            cores.add(VisualizadorGrafo.PALETA_CORES.get(i % VisualizadorGrafo.PALETA_CORES.size()));
        }
        return cores;
    }

    public static class PassoBFS {
        private final int passo;
        private final String verticeAtual;
        private final List<String> filaAposExpansao;
        private final List<String> vizinhosDescobertos;
        private final List<String> visitadosAteAgora;

        public PassoBFS(int passo, String verticeAtual,
                        List<String> filaAposExpansao,
                        List<String> vizinhosDescobertos,
                        List<String> visitadosAteAgora) {
            this.passo = passo;
            this.verticeAtual = verticeAtual;
            this.filaAposExpansao = Collections.unmodifiableList(new ArrayList<>(filaAposExpansao));
            this.vizinhosDescobertos = Collections.unmodifiableList(new ArrayList<>(vizinhosDescobertos));
            this.visitadosAteAgora = Collections.unmodifiableList(new ArrayList<>(visitadosAteAgora));
        }

        public int getPasso() {
            return passo;
        }

        public String getVerticeAtual() {
            return verticeAtual;
        }

        public List<String> getFilaAposExpansao() {
            return filaAposExpansao;
        }

        public List<String> getVizinhosDescobertos() {
            return vizinhosDescobertos;
        }

        public List<String> getVisitadosAteAgora() {
            return visitadosAteAgora;
        }
    }

    public static class ResultadoBFS {
        private final Grafo grafoOriginal;
        private final String verticeInicial;
        private final List<String> ordemVisita;
        private final List<String> verticesAlcancados;
        private final List<String> verticesNaoAlcancados;
        private final Map<String, Integer> distancias;
        private final Map<String, String> antecessores;
        private final List<Grafo.ArestaInfo> arestasArvore;
        private final List<PassoBFS> passos;

        public ResultadoBFS(Grafo grafoOriginal,
                            String verticeInicial,
                            List<String> ordemVisita,
                            List<String> verticesAlcancados,
                            List<String> verticesNaoAlcancados,
                            Map<String, Integer> distancias,
                            Map<String, String> antecessores,
                            List<Grafo.ArestaInfo> arestasArvore,
                            List<PassoBFS> passos) {
            this.grafoOriginal = grafoOriginal;
            this.verticeInicial = verticeInicial;
            this.ordemVisita = Collections.unmodifiableList(new ArrayList<>(ordemVisita));
            this.verticesAlcancados = Collections.unmodifiableList(new ArrayList<>(verticesAlcancados));
            this.verticesNaoAlcancados = Collections.unmodifiableList(new ArrayList<>(verticesNaoAlcancados));
            this.distancias = Collections.unmodifiableMap(new LinkedHashMap<>(distancias));
            this.antecessores = Collections.unmodifiableMap(new LinkedHashMap<>(antecessores));
            this.arestasArvore = Collections.unmodifiableList(new ArrayList<>(arestasArvore));
            this.passos = Collections.unmodifiableList(new ArrayList<>(passos));
        }

        public Grafo getGrafoOriginal() {
            return grafoOriginal;
        }

        public String getVerticeInicial() {
            return verticeInicial;
        }

        public List<String> getOrdemVisita() {
            return ordemVisita;
        }

        public List<String> getVerticesAlcancados() {
            return verticesAlcancados;
        }

        public List<String> getVerticesNaoAlcancados() {
            return verticesNaoAlcancados;
        }

        public Map<String, Integer> getDistancias() {
            return distancias;
        }

        public Map<String, Integer> getNiveis() {
            return distancias;
        }

        public int getDistancia(String vertice) {
            Integer dist = distancias.get(vertice);
            return (dist != null) ? dist : -1;
        }

        public int getNivel(String vertice) {
            return getDistancia(vertice);
        }

        public Map<String, String> getAntecessores() {
            return antecessores;
        }

        public String getAntecessor(String vertice) {
            return antecessores.get(vertice);
        }

        public List<Grafo.ArestaInfo> getArestasArvore() {
            return arestasArvore;
        }

        public List<PassoBFS> getPassos() {
            return passos;
        }

        public boolean isAlcancado(String vertice) {
            return verticesAlcancados.contains(vertice);
        }

        public boolean isTodosAlcancados() {
            return verticesNaoAlcancados.isEmpty();
        }

        public int getTotalAlcancados() {
            return verticesAlcancados.size();
        }

        public int getTotalNaoAlcancados() {
            return verticesNaoAlcancados.size();
        }

        public String getOrdemVisitaFormatada() {
            return String.join(" → ", ordemVisita);
        }

        public int getOrdemVisita(String vertice) {
            int idx = ordemVisita.indexOf(vertice);
            return idx >= 0 ? (idx + 1) : -1;
        }

        public String getOrdemVisitaNumerada() {
            List<String> numerados = new ArrayList<>();
            for (int i = 0; i < ordemVisita.size(); i++) {
                numerados.add((i + 1) + "º " + ordemVisita.get(i));
            }
            return String.join(" → ", numerados);
        }

        public String getTabelaExecucao() {
            if (passos.isEmpty()) {
                return "Nenhum passo executado.\n";
            }

            int wPasso = "Passo".length();
            int wVert = "Vértice Desenfileirado".length();
            int wViz = "Vizinhos Descobertos".length();
            int wFila = "Fila Resultante".length();
            int wVis = "Vértices Descobertos (Visitados)".length();

            List<String[]> linhas = new ArrayList<>();
            for (PassoBFS p : passos) {
                String sPasso = String.valueOf(p.getPasso());
                String sVert = p.getVerticeAtual();
                String sViz = p.getVizinhosDescobertos().toString();
                String sFila = p.getFilaAposExpansao().toString();
                String sVis = p.getVisitadosAteAgora().toString();

                wPasso = Math.max(wPasso, sPasso.length());
                wVert = Math.max(wVert, sVert.length());
                wViz = Math.max(wViz, sViz.length());
                wFila = Math.max(wFila, sFila.length());
                wVis = Math.max(wVis, sVis.length());

                linhas.add(new String[]{sPasso, sVert, sViz, sFila, sVis});
            }

            String formato = String.format(" %%-%ds | %%-%ds | %%-%ds | %%-%ds | %%-%ds\n",
                    wPasso, wVert, wViz, wFila, wVis);
            int totalLargura = wPasso + wVert + wViz + wFila + wVis + 14;
            String separador = "=".repeat(totalLargura) + "\n";

            StringBuilder sb = new StringBuilder();
            sb.append(separador);
            sb.append(String.format(formato, "Passo", "Vértice Desenfileirado", "Vizinhos Descobertos", "Fila Resultante", "Vértices Descobertos (Visitados)"));
            sb.append(separador);

            for (String[] col : linhas) {
                sb.append(String.format(formato, (Object[]) col));
            }
            sb.append(separador);

            return sb.toString();
        }

        public String getRelatorio() {
            StringBuilder sb = new StringBuilder();
            sb.append("=======================================================\n");
            sb.append("              BUSCA EM LARGURA (BFS)\n");
            sb.append("=======================================================\n");
            sb.append("Vértice inicial: ").append(verticeInicial).append("\n");
            sb.append("Ordem de visitação: ").append(getOrdemVisitaFormatada()).append("\n\n");
            sb.append("Vértices alcançados (").append(getTotalAlcancados()).append("):\n");
            for (String v : verticesAlcancados) {
                String ant = antecessores.get(v);
                String antStr = (ant == null) ? "raiz" : ant;
                sb.append("  - ").append(v).append(" (nível: ").append(getNivel(v))
                  .append(", antecessor: ").append(antStr).append(")\n");
            }
            sb.append("\nVértices NÃO alcançados:\n");
            if (verticesNaoAlcancados.isEmpty()) {
                sb.append("  Nenhum (todos os vértices do grafo foram alcançados).\n");
            } else {
                sb.append("  Total não alcançados: ").append(getTotalNaoAlcancados()).append("\n");
                for (String v : verticesNaoAlcancados) {
                    sb.append("  - ").append(v).append("\n");
                }
            }
            if (!arestasArvore.isEmpty()) {
                sb.append("\nArestas da árvore de busca:\n");
                boolean orientado = grafoOriginal.isOrientado();
                String sep = orientado ? " -> " : " - ";
                for (Grafo.ArestaInfo a : arestasArvore) {
                    sb.append("  - ").append(a.getNome()).append(": ").append(a.getOrigem())
                      .append(sep).append(a.getDestino()).append("\n");
                }
            }
            sb.append("=======================================================\n");
            return sb.toString();
        }
    }

    public ResultadoBFS buscaEmLargura(String verticeInicial) {
        if (grafo.getVertices().isEmpty()) {
            throw new IllegalArgumentException("-> Erro: O grafo está vazio (sem vértices).");
        }
        if (verticeInicial == null || verticeInicial.trim().isEmpty()) {
            throw new IllegalArgumentException("-> Erro: O nome do vértice inicial não pode ser vazio.");
        }
        List<String> todosVertices = grafo.getVertices();
        if (!todosVertices.contains(verticeInicial)) {
            throw new IllegalArgumentException("-> Erro: O vértice inicial '" + verticeInicial + "' não existe.");
        }

        Queue<String> fila = new LinkedList<>();
        Set<String> visitados = new LinkedHashSet<>();
        List<String> ordemVisita = new ArrayList<>();
        Map<String, Integer> distancias = new LinkedHashMap<>();
        Map<String, String> antecessores = new LinkedHashMap<>();
        List<Grafo.ArestaInfo> arestasArvore = new ArrayList<>();
        List<PassoBFS> passos = new ArrayList<>();

        for (String v : todosVertices) {
            distancias.put(v, -1);
            antecessores.put(v, null);
        }

        fila.add(verticeInicial);
        visitados.add(verticeInicial);
        distancias.put(verticeInicial, 0);

        int numPasso = 1;

        while (!fila.isEmpty()) {
            String u = fila.poll();
            ordemVisita.add(u);

            List<String> vizinhosDescobertosNestePasso = new ArrayList<>();

            for (Grafo.ArestaInfo aresta : grafo.getArestasInfo()) {
                String vizinho = null;

                if (grafo.isOrientado()) {
                    if (aresta.getOrigem().equals(u)) {
                        vizinho = aresta.getDestino();
                    }
                } else {
                    if (aresta.getOrigem().equals(u)) {
                        vizinho = aresta.getDestino();
                    } else if (aresta.getDestino().equals(u)) {
                        vizinho = aresta.getOrigem();
                    }
                }

                if (vizinho != null && !visitados.contains(vizinho)) {
                    visitados.add(vizinho);
                    distancias.put(vizinho, distancias.get(u) + 1);
                    antecessores.put(vizinho, u);
                    arestasArvore.add(aresta);
                    fila.add(vizinho);
                    vizinhosDescobertosNestePasso.add(vizinho);
                }
            }

            passos.add(new PassoBFS(
                    numPasso++,
                    u,
                    new ArrayList<>(fila),
                    vizinhosDescobertosNestePasso,
                    new ArrayList<>(visitados)
            ));
        }

        List<String> verticesAlcancados = new ArrayList<>(ordemVisita);
        List<String> verticesNaoAlcancados = new ArrayList<>();
        for (String v : todosVertices) {
            if (!visitados.contains(v)) {
                verticesNaoAlcancados.add(v);
            }
        }

        return new ResultadoBFS(
                grafo,
                verticeInicial,
                ordemVisita,
                verticesAlcancados,
                verticesNaoAlcancados,
                distancias,
                antecessores,
                arestasArvore,
                passos
        );
    }

    public ResultadoBFS bfs(String verticeInicial) {
        return buscaEmLargura(verticeInicial);
    }

    public static class PassoDFS {
        private final int passo;
        private final String verticeAtual;
        private final int profundidade;
        private final String antecessor;
        private final List<String> vizinhosDescobertos;
        private final List<String> visitadosAteAgora;

        public PassoDFS(int passo, String verticeAtual, int profundidade,
                        String antecessor, List<String> vizinhosDescobertos,
                        List<String> visitadosAteAgora) {
            this.passo = passo;
            this.verticeAtual = verticeAtual;
            this.profundidade = profundidade;
            this.antecessor = antecessor;
            this.vizinhosDescobertos = Collections.unmodifiableList(new ArrayList<>(vizinhosDescobertos));
            this.visitadosAteAgora = Collections.unmodifiableList(new ArrayList<>(visitadosAteAgora));
        }

        public int getPasso() {
            return passo;
        }

        public String getVerticeAtual() {
            return verticeAtual;
        }

        public int getProfundidade() {
            return profundidade;
        }

        public String getAntecessor() {
            return antecessor;
        }

        public List<String> getVizinhosDescobertos() {
            return vizinhosDescobertos;
        }

        public List<String> getVisitadosAteAgora() {
            return visitadosAteAgora;
        }
    }

    public static class ResultadoDFS {
        private final Grafo grafoOriginal;
        private final String verticeInicial;
        private final List<String> ordemVisita;
        private final List<String> verticesAlcancados;
        private final List<String> verticesNaoAlcancados;
        private final Map<String, Integer> profundidades;
        private final Map<String, String> antecessores;
        private final List<Grafo.ArestaInfo> arestasArvore;
        private final List<PassoDFS> passos;

        public ResultadoDFS(Grafo grafoOriginal,
                            String verticeInicial,
                            List<String> ordemVisita,
                            List<String> verticesAlcancados,
                            List<String> verticesNaoAlcancados,
                            Map<String, Integer> profundidades,
                            Map<String, String> antecessores,
                            List<Grafo.ArestaInfo> arestasArvore,
                            List<PassoDFS> passos) {
            this.grafoOriginal = grafoOriginal;
            this.verticeInicial = verticeInicial;
            this.ordemVisita = Collections.unmodifiableList(new ArrayList<>(ordemVisita));
            this.verticesAlcancados = Collections.unmodifiableList(new ArrayList<>(verticesAlcancados));
            this.verticesNaoAlcancados = Collections.unmodifiableList(new ArrayList<>(verticesNaoAlcancados));
            this.profundidades = Collections.unmodifiableMap(new LinkedHashMap<>(profundidades));
            this.antecessores = Collections.unmodifiableMap(new LinkedHashMap<>(antecessores));
            this.arestasArvore = Collections.unmodifiableList(new ArrayList<>(arestasArvore));
            this.passos = Collections.unmodifiableList(new ArrayList<>(passos));
        }

        public Grafo getGrafoOriginal() {
            return grafoOriginal;
        }

        public String getVerticeInicial() {
            return verticeInicial;
        }

        public List<String> getOrdemVisita() {
            return ordemVisita;
        }

        public List<String> getVerticesAlcancados() {
            return verticesAlcancados;
        }

        public List<String> getVerticesNaoAlcancados() {
            return verticesNaoAlcancados;
        }

        public Map<String, Integer> getProfundidades() {
            return profundidades;
        }

        public int getProfundidade(String vertice) {
            Integer p = profundidades.get(vertice);
            return (p != null) ? p : -1;
        }

        public int getOrdemVisita(String vertice) {
            int idx = ordemVisita.indexOf(vertice);
            return idx >= 0 ? (idx + 1) : -1;
        }

        public Map<String, String> getAntecessores() {
            return antecessores;
        }

        public String getAntecessor(String vertice) {
            return antecessores.get(vertice);
        }

        public List<Grafo.ArestaInfo> getArestasArvore() {
            return arestasArvore;
        }

        public List<PassoDFS> getPassos() {
            return passos;
        }

        public boolean isAlcancado(String vertice) {
            return verticesAlcancados.contains(vertice);
        }

        public boolean isTodosAlcancados() {
            return verticesNaoAlcancados.isEmpty();
        }

        public int getTotalAlcancados() {
            return verticesAlcancados.size();
        }

        public int getTotalNaoAlcancados() {
            return verticesNaoAlcancados.size();
        }

        public String getOrdemVisitaFormatada() {
            return String.join(" → ", ordemVisita);
        }

        public String getOrdemVisitaNumerada() {
            List<String> numerados = new ArrayList<>();
            for (int i = 0; i < ordemVisita.size(); i++) {
                numerados.add((i + 1) + "º " + ordemVisita.get(i));
            }
            return String.join(" → ", numerados);
        }

        public String getTabelaExecucao() {
            if (passos.isEmpty()) {
                return "Nenhum passo executado.\n";
            }

            int wPasso = "Passo".length();
            int wVert = "Vértice Visitado".length();
            int wOrd = "Ordem Final".length();
            int wProf = "Profundidade".length();
            int wAnt = "Antecessor".length();
            int wViz = "Vizinhos Descobertos".length();
            int wVis = "Vértices Descobertos (Visitados)".length();

            List<String[]> linhas = new ArrayList<>();
            for (PassoDFS p : passos) {
                String sPasso = String.valueOf(p.getPasso());
                String sVert = p.getVerticeAtual();
                String sOrd = getOrdemVisita(p.getVerticeAtual()) + "º";
                String sProf = String.valueOf(p.getProfundidade());
                String sAnt = p.getAntecessor() != null ? p.getAntecessor() : "raiz";
                String sViz = p.getVizinhosDescobertos().toString();
                String sVis = p.getVisitadosAteAgora().toString();

                wPasso = Math.max(wPasso, sPasso.length());
                wVert = Math.max(wVert, sVert.length());
                wOrd = Math.max(wOrd, sOrd.length());
                wProf = Math.max(wProf, sProf.length());
                wAnt = Math.max(wAnt, sAnt.length());
                wViz = Math.max(wViz, sViz.length());
                wVis = Math.max(wVis, sVis.length());

                linhas.add(new String[]{sPasso, sVert, sOrd, sProf, sAnt, sViz, sVis});
            }

            String formato = String.format(" %%-%ds | %%-%ds | %%-%ds | %%-%ds | %%-%ds | %%-%ds | %%-%ds\n",
                    wPasso, wVert, wOrd, wProf, wAnt, wViz, wVis);
            int totalLargura = wPasso + wVert + wOrd + wProf + wAnt + wViz + wVis + 20;
            String separador = "=".repeat(totalLargura) + "\n";

            StringBuilder sb = new StringBuilder();
            sb.append(separador);
            sb.append(String.format(formato, "Passo", "Vértice Visitado", "Ordem Final", "Profundidade", "Antecessor", "Vizinhos Descobertos", "Vértices Descobertos (Visitados)"));
            sb.append(separador);

            for (String[] col : linhas) {
                sb.append(String.format(formato, (Object[]) col));
            }
            sb.append(separador);

            return sb.toString();
        }

        public String getRelatorio() {
            StringBuilder sb = new StringBuilder();
            sb.append("=======================================================\n");
            sb.append("            BUSCA EM PROFUNDIDADE (DFS)\n");
            sb.append("=======================================================\n");
            sb.append("Vértice inicial: ").append(verticeInicial).append("\n");
            sb.append("Ordem final de visitação: ").append(getOrdemVisitaNumerada()).append("\n\n");
            sb.append("Vértices alcançados (").append(getTotalAlcancados()).append("):\n");
            for (String v : verticesAlcancados) {
                String ant = antecessores.get(v);
                String antStr = (ant == null) ? "raiz" : ant;
                sb.append("  - ").append(getOrdemVisita(v)).append("º: ").append(v)
                  .append(" (profundidade: ").append(getProfundidade(v))
                  .append(", antecessor: ").append(antStr).append(")\n");
            }
            sb.append("\nVértices NÃO alcançados:\n");
            if (verticesNaoAlcancados.isEmpty()) {
                sb.append("  Nenhum (todos os vértices do grafo foram alcançados).\n");
            } else {
                sb.append("  Total não alcançados: ").append(getTotalNaoAlcancados()).append("\n");
                for (String v : verticesNaoAlcancados) {
                    sb.append("  - ").append(v).append("\n");
                }
            }
            if (!arestasArvore.isEmpty()) {
                sb.append("\nArestas da árvore de busca:\n");
                boolean orientado = grafoOriginal.isOrientado();
                String sep = orientado ? " -> " : " - ";
                for (Grafo.ArestaInfo a : arestasArvore) {
                    sb.append("  - ").append(a.getNome()).append(": ").append(a.getOrigem())
                      .append(sep).append(a.getDestino()).append("\n");
                }
            }
            sb.append("=======================================================\n");
            return sb.toString();
        }
    }

    public ResultadoDFS buscaEmProfundidade(String verticeInicial) {
        if (grafo.getVertices().isEmpty()) {
            throw new IllegalArgumentException("-> Erro: O grafo está vazio (sem vértices).");
        }
        if (verticeInicial == null || verticeInicial.trim().isEmpty()) {
            throw new IllegalArgumentException("-> Erro: O nome do vértice inicial não pode ser vazio.");
        }
        List<String> todosVertices = grafo.getVertices();
        if (!todosVertices.contains(verticeInicial)) {
            throw new IllegalArgumentException("-> Erro: O vértice inicial '" + verticeInicial + "' não existe.");
        }

        Set<String> visitados = new LinkedHashSet<>();
        List<String> ordemVisita = new ArrayList<>();
        Map<String, Integer> profundidades = new LinkedHashMap<>();
        Map<String, String> antecessores = new LinkedHashMap<>();
        List<Grafo.ArestaInfo> arestasArvore = new ArrayList<>();
        List<PassoDFS> passos = new ArrayList<>();

        for (String v : todosVertices) {
            profundidades.put(v, -1);
            antecessores.put(v, null);
        }

        int[] contadorPassos = new int[]{1};
        dfsRecursivo(verticeInicial, null, 0, visitados, ordemVisita, profundidades, antecessores, arestasArvore, passos, contadorPassos);

        List<String> verticesAlcancados = new ArrayList<>(ordemVisita);
        List<String> verticesNaoAlcancados = new ArrayList<>();
        for (String v : todosVertices) {
            if (!visitados.contains(v)) {
                verticesNaoAlcancados.add(v);
            }
        }

        return new ResultadoDFS(
                grafo,
                verticeInicial,
                ordemVisita,
                verticesAlcancados,
                verticesNaoAlcancados,
                profundidades,
                antecessores,
                arestasArvore,
                passos
        );
    }

    public ResultadoDFS dfs(String verticeInicial) {
        return buscaEmProfundidade(verticeInicial);
    }

    private void dfsRecursivo(String u,
                              String pai,
                              int profAtual,
                              Set<String> visitados,
                              List<String> ordemVisita,
                              Map<String, Integer> profundidades,
                              Map<String, String> antecessores,
                              List<Grafo.ArestaInfo> arestasArvore,
                              List<PassoDFS> passos,
                              int[] contadorPassos) {
        visitados.add(u);
        ordemVisita.add(u);
        profundidades.put(u, profAtual);
        antecessores.put(u, pai);

        List<String> vizinhosDescobertos = new ArrayList<>();
        List<Grafo.ArestaInfo> arestasParaRecurso = new ArrayList<>();
        List<String> destinosParaRecurso = new ArrayList<>();

        for (Grafo.ArestaInfo aresta : grafo.getArestasInfo()) {
            String vizinho = null;

            if (grafo.isOrientado()) {
                if (aresta.getOrigem().equals(u)) {
                    vizinho = aresta.getDestino();
                }
            } else {
                if (aresta.getOrigem().equals(u)) {
                    vizinho = aresta.getDestino();
                } else if (aresta.getDestino().equals(u)) {
                    vizinho = aresta.getOrigem();
                }
            }

            if (vizinho != null && !visitados.contains(vizinho) && !vizinhosDescobertos.contains(vizinho)) {
                vizinhosDescobertos.add(vizinho);
                arestasParaRecurso.add(aresta);
                destinosParaRecurso.add(vizinho);
            }
        }

        passos.add(new PassoDFS(
                contadorPassos[0]++,
                u,
                profAtual,
                pai,
                vizinhosDescobertos,
                new ArrayList<>(visitados)
        ));

        for (int i = 0; i < destinosParaRecurso.size(); i++) {
            String v = destinosParaRecurso.get(i);
            Grafo.ArestaInfo a = arestasParaRecurso.get(i);

            if (!visitados.contains(v)) {
                arestasArvore.add(a);
                dfsRecursivo(v, u, profAtual + 1, visitados, ordemVisita, profundidades, antecessores, arestasArvore, passos, contadorPassos);
            }
        }
    }
}