package org.example.controle;

import org.example.dominio.*;
import org.example.servico.GrafoWebServico;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grafos")
public class GrafoControle {

    private final GrafoWebServico grafoWebServico;

    public GrafoControle(GrafoWebServico grafoWebServico) {
        this.grafoWebServico = grafoWebServico;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listarProjetos() {
        return ResponseEntity.ok(resposta(true, "Projetos de grafos listados com sucesso", grafoWebServico.listarProjetos()));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> criarProjeto(@RequestBody Map<String, Object> req) {
        String nome = exigirTexto(req, "nome", "O nome do grafo é obrigatório");
        boolean orientado = Boolean.TRUE.equals(req.get("orientado"));
        boolean valorado = Boolean.TRUE.equals(req.get("valorado"));
        ProjetoGrafo criado = grafoWebServico.criarProjeto(nome, orientado, valorado);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resposta(true, "Grafo criado com sucesso", criado));
    }

    @PostMapping("/importar-dot")
    public ResponseEntity<Map<String, Object>> importarDot(@RequestBody Map<String, Object> req) {
        String nome = exigirTexto(req, "nome", "O nome do projeto é obrigatório");
        String conteudoDot = exigirTexto(req, "conteudoDot", "O conteúdo DOT é obrigatório");
        ProjetoGrafo importado = grafoWebServico.importarDot(nome, conteudoDot);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resposta(true, "Grafo importado do conteúdo DOT com sucesso", importado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> excluirProjeto(@PathVariable Long id) {
        grafoWebServico.excluirProjeto(id);
        return ResponseEntity.ok(resposta(true, "Projeto de grafo removido com sucesso", null));
    }

    @GetMapping("/{id}/propriedades")
    public ResponseEntity<Map<String, Object>> analisarPropriedades(@PathVariable Long id) {
        return ResponseEntity.ok(resposta(true, "Propriedades do grafo analisadas", grafoWebServico.analisarPropriedades(id)));
    }

    @PostMapping("/{id}/vertices")
    public ResponseEntity<Map<String, Object>> adicionarVertice(
            @PathVariable Long id,
            @RequestBody Map<String, Object> req) {
        String nomeVertice = exigirTexto(req, "nomeVertice", "O nome do vértice é obrigatório");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resposta(true, "Vértice adicionado com sucesso", grafoWebServico.adicionarVertice(id, nomeVertice)));
    }

    @DeleteMapping("/{id}/vertices/{nomeVertice}")
    public ResponseEntity<Map<String, Object>> removerVertice(
            @PathVariable Long id,
            @PathVariable String nomeVertice) {
        return ResponseEntity.ok(resposta(true, "Vértice removido com sucesso", grafoWebServico.removerVertice(id, nomeVertice)));
    }

    @PostMapping("/{id}/arestas")
    public ResponseEntity<Map<String, Object>> adicionarAresta(
            @PathVariable Long id,
            @RequestBody Map<String, Object> req) {
        String nomeAresta = exigirTexto(req, "nomeAresta", "O nome da aresta é obrigatório");
        String origem = exigirTexto(req, "origem", "O vértice de origem é obrigatório");
        String destino = exigirTexto(req, "destino", "O vértice de destino é obrigatório");
        int peso = req.get("peso") instanceof Number n ? n.intValue() : 1;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resposta(true, "Aresta adicionada com sucesso",
                        grafoWebServico.adicionarAresta(id, nomeAresta, origem, destino, peso)));
    }

    @DeleteMapping("/{id}/arestas/{nomeAresta}")
    public ResponseEntity<Map<String, Object>> removerAresta(
            @PathVariable Long id,
            @PathVariable String nomeAresta) {
        return ResponseEntity.ok(resposta(true, "Aresta removida com sucesso", grafoWebServico.removerAresta(id, nomeAresta)));
    }

    @GetMapping("/{id}/algoritmos/dijkstra")
    public ResponseEntity<Map<String, Object>> executarDijkstra(
            @PathVariable Long id,
            @RequestParam String origem,
            @RequestParam String destino) {
        return ResponseEntity.ok(resposta(true, "Dijkstra executado com sucesso", grafoWebServico.executarDijkstra(id, origem, destino)));
    }

    @GetMapping("/{id}/algoritmos/kruskal")
    public ResponseEntity<Map<String, Object>> executarKruskal(@PathVariable Long id) {
        return ResponseEntity.ok(resposta(true, "Kruskal executado com sucesso", grafoWebServico.executarKruskal(id)));
    }

    @GetMapping("/{id}/algoritmos/prim")
    public ResponseEntity<Map<String, Object>> executarPrim(
            @PathVariable Long id,
            @RequestParam(required = false) String verticeInicial) {
        return ResponseEntity.ok(resposta(true, "Prim executado com sucesso", grafoWebServico.executarPrim(id, verticeInicial)));
    }

    @GetMapping("/{id}/algoritmos/malgrange")
    public ResponseEntity<Map<String, Object>> executarMalgrange(@PathVariable Long id) {
        return ResponseEntity.ok(resposta(true, "Malgrange executado com sucesso", grafoWebServico.executarMalgrange(id)));
    }

    @GetMapping("/{id}/algoritmos/bfs")
    public ResponseEntity<Map<String, Object>> executarBFS(
            @PathVariable Long id,
            @RequestParam String verticeInicial) {
        return ResponseEntity.ok(resposta(true, "BFS executado com sucesso", grafoWebServico.executarBFS(id, verticeInicial)));
    }

    @GetMapping("/{id}/algoritmos/dfs")
    public ResponseEntity<Map<String, Object>> executarDFS(
            @PathVariable Long id,
            @RequestParam String verticeInicial) {
        return ResponseEntity.ok(resposta(true, "DFS executado com sucesso", grafoWebServico.executarDFS(id, verticeInicial)));
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<Map<String, Object>> listarHistorico(@PathVariable Long id) {
        return ResponseEntity.ok(resposta(true, "Histórico recuperado com sucesso", grafoWebServico.listarHistorico(id)));
    }

    @GetMapping(value = "/{id}/imagem", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> gerarImagem(
            @PathVariable Long id,
            @RequestParam(defaultValue = "original") String modo,
            @RequestParam(required = false) String verticeInicial) {
        try {
            byte[] png = grafoWebServico.gerarImagemPng(id, modo, verticeInicial);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(png);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> tratarArgumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(resposta(false, ex.getMessage(), null));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> tratarEstadoInvalido(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(resposta(false, ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> tratarErroGeral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(resposta(false, "Erro interno ao processar requisição: " + ex.getMessage(), null));
    }

    private String exigirTexto(Map<String, Object> req, String campo, String mensagemErro) {
        Object valor = req != null ? req.get(campo) : null;
        if (!(valor instanceof String s) || s.isBlank()) {
            throw new IllegalArgumentException(mensagemErro);
        }
        return s;
    }

    private Map<String, Object> resposta(boolean sucesso, String mensagem, Object dados) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("sucesso", sucesso);
        mapa.put("mensagem", mensagem);
        mapa.put("dados", dados);
        mapa.put("dataHora", LocalDateTime.now());
        return mapa;
    }
}
