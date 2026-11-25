package org.example.servico;

import br.com.davesmartins.api.Graph;
import org.example.Algoritmos;
import org.example.AnalisaGrafo;
import org.example.Grafo;
import org.example.LeitorDot;
import org.example.VisualizadorGrafo;
import org.example.dominio.*;
import org.example.repositorio.HistoricoExecucaoRepositorio;
import org.example.repositorio.ProjetoGrafoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class GrafoWebServico {

    private final ProjetoGrafoRepositorio projetoGrafoRepositorio;
    private final HistoricoExecucaoRepositorio historicoRepositorio;

    public GrafoWebServico(ProjetoGrafoRepositorio projetoGrafoRepositorio,
                           HistoricoExecucaoRepositorio historicoRepositorio) {
        this.projetoGrafoRepositorio = projetoGrafoRepositorio;
        this.historicoRepositorio = historicoRepositorio;
    }

    public List<ProjetoGrafo> listarProjetos() {
        return projetoGrafoRepositorio.findAll();
    }

    public ProjetoGrafo buscarProjeto(Long id) {
        return projetoGrafoRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Projeto de grafo não encontrado com ID: " + id));
    }

    @Transactional
    public ProjetoGrafo criarProjeto(String nome, boolean orientado, boolean valorado) {
        String dotInicial = orientado ? "digraph G {\n}" : "graph G {\n}";
        ProjetoGrafo projeto = new ProjetoGrafo(nome, orientado, valorado, dotInicial);
        return projetoGrafoRepositorio.save(projeto);
    }

    @Transactional
    public ProjetoGrafo importarDot(String nome, String conteudoDot) {
        Grafo g = LeitorDot.lerTexto(conteudoDot);
        String dotNormalizado = g.getVertices().isEmpty()
                ? (g.isOrientado() ? "digraph G {\n}" : "graph G {\n}")
                : VisualizadorGrafo.gerarDot(g);
        ProjetoGrafo projeto = new ProjetoGrafo(nome, g.isOrientado(), g.isValorado(), dotNormalizado);
        return projetoGrafoRepositorio.save(projeto);
    }

    @Transactional
    public void excluirProjeto(Long id) {
        ProjetoGrafo projeto = buscarProjeto(id);
        projetoGrafoRepositorio.delete(projeto);
    }

    @Transactional
    public ResumoPropriedadesGrafo adicionarVertice(Long id, String nomeVertice) {
        ProjetoGrafo projeto = buscarProjeto(id);
        Grafo g = reconstruirGrafo(projeto);
        if (g.getVertices().contains(nomeVertice.trim())) {
            throw new IllegalArgumentException("O vértice '" + nomeVertice + "' já existe.");
        }
        g.adicionarVertice(nomeVertice.trim());
        atualizarDotProjeto(projeto, g);
        return gerarResumo(g, projeto);
    }

    @Transactional
    public ResumoPropriedadesGrafo removerVertice(Long id, String nomeVertice) {
        ProjetoGrafo projeto = buscarProjeto(id);
        Grafo g = reconstruirGrafo(projeto);
        if (!g.removerVertice(nomeVertice)) {
            throw new IllegalArgumentException("Não foi possível remover o vértice '" + nomeVertice + "'.");
        }
        atualizarDotProjeto(projeto, g);
        return gerarResumo(g, projeto);
    }

    @Transactional
    public ResumoPropriedadesGrafo adicionarAresta(Long id, String nomeAresta, String origem, String destino, int peso) {
        ProjetoGrafo projeto = buscarProjeto(id);
        Grafo g = reconstruirGrafo(projeto);
        if (!g.getVertices().contains(origem) || !g.getVertices().contains(destino)) {
            throw new IllegalArgumentException("Vértices de origem e/ou destino não existem.");
        }
        if (g.getArestas().contains(nomeAresta)) {
            throw new IllegalArgumentException("Já existe uma aresta com o nome '" + nomeAresta + "'.");
        }
        g.adicionarAresta(nomeAresta, origem, destino, peso);
        atualizarDotProjeto(projeto, g);
        return gerarResumo(g, projeto);
    }

    @Transactional
    public ResumoPropriedadesGrafo removerAresta(Long id, String nomeAresta) {
        ProjetoGrafo projeto = buscarProjeto(id);
        Grafo g = reconstruirGrafo(projeto);
        if (!g.removerAresta(nomeAresta)) {
            throw new IllegalArgumentException("Aresta '" + nomeAresta + "' não encontrada.");
        }
        atualizarDotProjeto(projeto, g);
        return gerarResumo(g, projeto);
    }

    public ResumoPropriedadesGrafo analisarPropriedades(Long id) {
        ProjetoGrafo projeto = buscarProjeto(id);
        Grafo g = reconstruirGrafo(projeto);
        return gerarResumo(g, projeto);
    }

    @Transactional
    public CaminhoMinimo executarDijkstra(Long id, String origem, String destino) {
        ProjetoGrafo projeto = buscarProjeto(id);
        Grafo g = reconstruirGrafo(projeto);
        Algoritmos algoritmos = new Algoritmos(g);
        Algoritmos.ResultadoCaminho res = algoritmos.dijkstra(origem, destino);

        String tabela = algoritmos.gerarTabelaExecucao(res);
        CaminhoMinimo dominio = new CaminhoMinimo(
                origem, destino, res.temCaminho(), res.getCaminho(),
                res.getCaminhoFormatado(), res.getCustoTotal(), tabela, res.getPassos()
        );

        String resumo = res.temCaminho()
                ? "Caminho: " + res.getCaminhoFormatado() + " (Custo: " + res.getCustoTotal() + ")"
                : "Não há caminho de " + origem + " até " + destino;
        historicoRepositorio.save(new HistoricoExecucaoAlgoritmo(
                projeto, "DIJKSTRA", "origem=" + origem + ", destino=" + destino, resumo
        ));

        return dominio;
    }

    @Transactional
    public ArvoreGeradoraMinima executarKruskal(Long id) {
        ProjetoGrafo projeto = buscarProjeto(id);
        Grafo g = reconstruirGrafo(projeto);
        Algoritmos.ResultadoAGM res = new Algoritmos(g).kruskal();
        ArvoreGeradoraMinima dominio = mapearAGM("KRUSKAL", res);

        historicoRepositorio.save(new HistoricoExecucaoAlgoritmo(
                projeto, "KRUSKAL", "-",
                "Componentes: " + dominio.getNumeroComponentes() + " | Peso Total: " + dominio.getPesoTotal()
        ));
        return dominio;
    }

    @Transactional
    public ArvoreGeradoraMinima executarPrim(Long id, String verticeInicial) {
        ProjetoGrafo projeto = buscarProjeto(id);
        Grafo g = reconstruirGrafo(projeto);
        Algoritmos.ResultadoAGM res = (verticeInicial == null || verticeInicial.isBlank())
                ? new Algoritmos(g).prim()
                : new Algoritmos(g).prim(verticeInicial);
        ArvoreGeradoraMinima dominio = mapearAGM("PRIM", res);

        historicoRepositorio.save(new HistoricoExecucaoAlgoritmo(
                projeto, "PRIM",
                "verticeInicial=" + (verticeInicial != null ? verticeInicial : "automático"),
                "Componentes: " + dominio.getNumeroComponentes() + " | Peso Total: " + dominio.getPesoTotal()
        ));
        return dominio;
    }

    @Transactional
    public ResultadoMalgrangeDominio executarMalgrange(Long id) {
        ProjetoGrafo projeto = buscarProjeto(id);
        Grafo g = reconstruirGrafo(projeto);
        Algoritmos.ResultadoMalgrange res = new Algoritmos(g).malgrange();

        List<ComponenteConexa> componentes = new ArrayList<>();
        List<Set<String>> comps = res.getComponentes();
        for (int i = 0; i < comps.size(); i++) {
            componentes.add(new ComponenteConexa(
                    i + 1,
                    new ArrayList<>(comps.get(i)),
                    List.of(),
                    0,
                    res.getCorComponente(i + 1)
            ));
        }

        String dotOriginal = VisualizadorGrafo.gerarDotMalgrangeOriginal(res);
        String dotReduzido = VisualizadorGrafo.gerarDotGrafoReduzido(res);

        ResultadoMalgrangeDominio dominio = new ResultadoMalgrangeDominio(
                res.getNumComponentes(), componentes, res.getTabelaExecucao(), dotOriginal, dotReduzido
        );

        historicoRepositorio.save(new HistoricoExecucaoAlgoritmo(
                projeto, "MALGRANGE", "-",
                "Componentes Fortemente Conexas: " + res.getNumComponentes()
        ));
        return dominio;
    }

    @Transactional
    public ResultadoBuscaGrafo executarBFS(Long id, String verticeInicial) {
        ProjetoGrafo projeto = buscarProjeto(id);
        Grafo g = reconstruirGrafo(projeto);
        Algoritmos.ResultadoBFS res = new Algoritmos(g).bfs(verticeInicial);

        List<Aresta> arestas = new ArrayList<>(res.getArestasArvore());
        ResultadoBuscaGrafo dominio = new ResultadoBuscaGrafo(
                "BFS", res.getVerticeInicial(), res.getOrdemVisita(),
                res.getOrdemVisitaFormatada(), res.getVerticesAlcancados(),
                res.getVerticesNaoAlcancados(), res.getDistancias(),
                res.getAntecessores(), arestas, res.getTabelaExecucao(), res.getRelatorio()
        );

        historicoRepositorio.save(new HistoricoExecucaoAlgoritmo(
                projeto, "BFS", "verticeInicial=" + verticeInicial,
                "Ordem: " + res.getOrdemVisitaFormatada()
        ));
        return dominio;
    }

    @Transactional
    public ResultadoBuscaGrafo executarDFS(Long id, String verticeInicial) {
        ProjetoGrafo projeto = buscarProjeto(id);
        Grafo g = reconstruirGrafo(projeto);
        Algoritmos.ResultadoDFS res = new Algoritmos(g).dfs(verticeInicial);

        List<Aresta> arestas = new ArrayList<>(res.getArestasArvore());
        ResultadoBuscaGrafo dominio = new ResultadoBuscaGrafo(
                "DFS", res.getVerticeInicial(), res.getOrdemVisita(),
                res.getOrdemVisitaFormatada(), res.getVerticesAlcancados(),
                res.getVerticesNaoAlcancados(), res.getProfundidades(),
                res.getAntecessores(), arestas, res.getTabelaExecucao(), res.getRelatorio()
        );

        historicoRepositorio.save(new HistoricoExecucaoAlgoritmo(
                projeto, "DFS", "verticeInicial=" + verticeInicial,
                "Ordem: " + res.getOrdemVisitaFormatada()
        ));
        return dominio;
    }

    public List<HistoricoExecucaoAlgoritmo> listarHistorico(Long id) {
        buscarProjeto(id);
        return historicoRepositorio.findByProjetoGrafoIdOrderByExecutadoEmDesc(id);
    }

    public byte[] gerarImagemPng(Long id, String modo, String verticeInicial) throws IOException {
        ProjetoGrafo projeto = buscarProjeto(id);
        Grafo g = reconstruirGrafo(projeto);
        String dot;

        if ("malgrange".equalsIgnoreCase(modo)) {
            Algoritmos.ResultadoMalgrange res = new Algoritmos(g).malgrange();
            dot = VisualizadorGrafo.gerarDotMalgrangeOriginal(res);
        } else if ("malgrange-reduzido".equalsIgnoreCase(modo)) {
            Algoritmos.ResultadoMalgrange res = new Algoritmos(g).malgrange();
            dot = VisualizadorGrafo.gerarDotGrafoReduzido(res);
        } else if ("bfs".equalsIgnoreCase(modo)) {
            Algoritmos.ResultadoBFS res = new Algoritmos(g).bfs(verticeInicial);
            dot = VisualizadorGrafo.gerarDotBFS(res);
        } else if ("dfs".equalsIgnoreCase(modo)) {
            Algoritmos.ResultadoDFS res = new Algoritmos(g).dfs(verticeInicial);
            dot = VisualizadorGrafo.gerarDotDFS(res);
        } else {
            dot = VisualizadorGrafo.gerarDot(g);
        }

        Path tempPng = Files.createTempFile("grafo-", ".png");
        try {
            Graph.createStringDotToPng(dot, tempPng.toAbsolutePath().toString());
            return Files.readAllBytes(tempPng);
        } finally {
            Files.deleteIfExists(tempPng);
        }
    }

    private Grafo reconstruirGrafo(ProjetoGrafo projeto) {
        Grafo lido = LeitorDot.lerTexto(projeto.getConteudoDot());
        Grafo g = new Grafo(projeto.isOrientado(), projeto.isValorado(), true);
        for (String v : lido.getVertices()) {
            g.adicionarVertice(v);
        }
        for (Grafo.ArestaInfo a : lido.getArestasInfo()) {
            g.adicionarAresta(a.getNome(), a.getOrigem(), a.getDestino(), a.getPeso());
        }
        return g;
    }

    private void atualizarDotProjeto(ProjetoGrafo projeto, Grafo g) {
        String dot = g.getVertices().isEmpty()
                ? (g.isOrientado() ? "digraph G {\n}" : "graph G {\n}")
                : VisualizadorGrafo.gerarDot(g);
        projeto.setConteudoDot(dot);
        projetoGrafoRepositorio.save(projeto);
    }

    private ResumoPropriedadesGrafo gerarResumo(Grafo g, ProjetoGrafo projeto) {
        AnalisaGrafo analisa = new AnalisaGrafo(g);
        List<Vertice> verticesDetalhados = new ArrayList<>();
        for (String v : g.getVertices()) {
            int grauTotal = analisa.grauDoVertice(v);
            int grauEntrada = g.isOrientado() ? analisa.grauEntrada(v) : 0;
            int grauSaida = g.isOrientado() ? analisa.grauSaida(v) : 0;
            boolean folha = !g.isOrientado() && grauTotal == 1;
            boolean fonte = g.isOrientado() && grauEntrada == 0 && grauSaida > 0;
            boolean sumidouro = g.isOrientado() && grauSaida == 0 && grauEntrada > 0;
            verticesDetalhados.add(new Vertice(v, grauTotal, grauEntrada, grauSaida, folha, fonte, sumidouro));
        }

        List<Aresta> arestas = new ArrayList<>(g.getArestasInfo());

        return new ResumoPropriedadesGrafo(
                g.isOrientado(),
                g.isValorado(),
                analisa.ordemDoGrafo(),
                g.getArestas().size(),
                analisa.isSimples(),
                analisa.isRegular(),
                analisa.isCompleto(),
                g.isOrientado() && analisa.isFonteSumidouro(),
                g.possuiPesosNegativos(),
                analisa.listarLacos(),
                analisa.listarVerticesFolha(),
                g.isOrientado() ? analisa.listarFontes() : List.of(),
                g.isOrientado() ? analisa.listarSumidouros() : List.of(),
                verticesDetalhados,
                arestas,
                g.getVertices(),
                g.getArestas(),
                g.getMatriz(),
                projeto.getConteudoDot()
        );
    }

    private ArvoreGeradoraMinima mapearAGM(String nomeAlgoritmo, Algoritmos.ResultadoAGM res) {
        List<ComponenteConexa> componentes = new ArrayList<>();
        List<Algoritmos.ComponenteAGM> originais = res.getComponentes();
        for (int i = 0; i < originais.size(); i++) {
            Algoritmos.ComponenteAGM comp = originais.get(i);
            List<Aresta> arestasComp = new ArrayList<>(comp.getArestas());
            componentes.add(new ComponenteConexa(i + 1, comp.getVertices(), arestasComp, comp.getPesoTotal(), null));
        }
        List<Aresta> todasArestas = new ArrayList<>(res.getArestas());
        return new ArvoreGeradoraMinima(
                nomeAlgoritmo, res.isConexo(), res.getNumComponentes(),
                res.getPesoTotal(), todasArestas, componentes
        );
    }
}
