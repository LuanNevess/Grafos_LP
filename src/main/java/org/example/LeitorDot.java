package org.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lê um arquivo DOT (Graphviz) e reconstrói o Grafo correspondente.
 * Qualquer problema de arquivo ou de conteúdo vira IllegalArgumentException.
 */
public class LeitorDot {

    private static final Set<String> PALAVRAS_RESERVADAS =
            Set.of("node", "edge", "graph", "digraph", "subgraph", "strict");

    private enum Tipo { ID, SIMBOLO, SETA, FIM }

    private static class Token {
        private final Tipo tipo;
        private final String texto;
        private final boolean entreAspas;
        private final int linha;

        private Token(Tipo tipo, String texto, boolean entreAspas, int linha) {
            this.tipo = tipo;
            this.texto = texto;
            this.entreAspas = entreAspas;
            this.linha = linha;
        }
    }

    private static class ArestaLida {
        private final String nome;      // null quando o DOT não traz id
        private final String origem;
        private final String destino;
        private final Integer peso;     // null quando não há label
        private final int linha;

        private ArestaLida(String nome, String origem, String destino, Integer peso, int linha) {
            this.nome = nome;
            this.origem = origem;
            this.destino = destino;
            this.peso = peso;
            this.linha = linha;
        }
    }

    private final List<Token> tokens;
    private int pos = 0;
    private boolean orientado;
    private final Set<String> vertices = new LinkedHashSet<>();
    private final List<ArestaLida> arestas = new ArrayList<>();

    private LeitorDot(List<Token> tokens) {
        this.tokens = tokens;
    }

    // ---------------------------------------------------------------- API pública

    public static Grafo lerArquivo(String caminho) {
        if (caminho == null || caminho.trim().isEmpty()) {
            throw new IllegalArgumentException("-> Erro: O caminho do arquivo não pode ser vazio.");
        }

        String limpo = caminho.trim();
        if (limpo.length() >= 2 && limpo.startsWith("\"") && limpo.endsWith("\"")) {
            limpo = limpo.substring(1, limpo.length() - 1); // caminho colado com aspas
        }

        Path path;
        try {
            path = Paths.get(limpo);
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("-> Erro: Caminho inválido: '" + limpo + "'.");
        }

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("-> Erro: Arquivo não encontrado: '" + limpo + "'.");
        }
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("-> Erro: '" + limpo + "' não é um arquivo.");
        }

        String conteudo;
        try {
            conteudo = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("-> Erro: Não foi possível ler o arquivo '" + limpo
                    + "' (verifique se ele é um texto em UTF-8).");
        }

        return lerTexto(conteudo);
    }

    public static Grafo lerTexto(String dot) {
        if (dot == null) {
            throw new IllegalArgumentException("-> Erro: O conteúdo DOT não pode ser nulo.");
        }
        if (dot.startsWith("\uFEFF")) {
            dot = dot.substring(1); // remove BOM
        }
        return new LeitorDot(tokenizar(dot)).construir();
    }

    // ---------------------------------------------------------------- tokenizador

    private static List<Token> tokenizar(String dot) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        int n = dot.length();
        int linha = 1;

        while (i < n) {
            char c = dot.charAt(i);

            if (c == '\n') {
                linha++;
                i++;
                continue;
            }
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // comentário de linha
            if (c == '/' && i + 1 < n && dot.charAt(i + 1) == '/') {
                while (i < n && dot.charAt(i) != '\n') {
                    i++;
                }
                continue;
            }

            // comentário de bloco
            if (c == '/' && i + 1 < n && dot.charAt(i + 1) == '*') {
                int fim = dot.indexOf("*/", i + 2);
                if (fim < 0) {
                    throw erro(linha, "comentário '/*' não foi fechado.");
                }
                for (int k = i; k < fim; k++) {
                    if (dot.charAt(k) == '\n') {
                        linha++;
                    }
                }
                i = fim + 2;
                continue;
            }

            // operadores de aresta: -> e --
            if (c == '-' && i + 1 < n && (dot.charAt(i + 1) == '>' || dot.charAt(i + 1) == '-')) {
                tokens.add(new Token(Tipo.SETA, dot.substring(i, i + 2), false, linha));
                i += 2;
                continue;
            }

            if ("{}[];,=:".indexOf(c) >= 0) {
                tokens.add(new Token(Tipo.SIMBOLO, String.valueOf(c), false, linha));
                i++;
                continue;
            }

            // texto entre aspas (\" vira aspas)
            if (c == '"') {
                int linhaInicio = linha;
                StringBuilder sb = new StringBuilder();
                boolean fechou = false;
                i++;
                while (i < n) {
                    char d = dot.charAt(i);
                    if (d == '\\' && i + 1 < n && dot.charAt(i + 1) == '"') {
                        sb.append('"');
                        i += 2;
                        continue;
                    }
                    if (d == '"') {
                        fechou = true;
                        i++;
                        break;
                    }
                    if (d == '\n') {
                        linha++;
                    }
                    sb.append(d);
                    i++;
                }
                if (!fechou) {
                    throw erro(linhaInicio, "aspas não foram fechadas.");
                }
                tokens.add(new Token(Tipo.ID, sb.toString(), true, linhaInicio));
                continue;
            }

            if (c == '<') {
                throw erro(linha, "strings HTML (<...>) não são suportadas.");
            }

            // identificador
            if (ehInicioId(c)) {
                int j = i + 1;
                while (j < n && ehParteId(dot.charAt(j))) {
                    j++;
                }
                tokens.add(new Token(Tipo.ID, dot.substring(i, j), false, linha));
                i = j;
                continue;
            }

            // número (ex: 12, -3, 2.5)
            boolean inicioNumero = Character.isDigit(c) || c == '.'
                    || (c == '-' && i + 1 < n
                    && (Character.isDigit(dot.charAt(i + 1)) || dot.charAt(i + 1) == '.'));
            if (inicioNumero) {
                int j = i + 1;
                while (j < n && (Character.isDigit(dot.charAt(j)) || dot.charAt(j) == '.')) {
                    j++;
                }
                tokens.add(new Token(Tipo.ID, dot.substring(i, j), false, linha));
                i = j;
                continue;
            }

            throw erro(linha, "caractere inesperado '" + c + "'.");
        }

        tokens.add(new Token(Tipo.FIM, "", false, linha));
        return tokens;
    }

    private static boolean ehInicioId(char c) {
        return Character.isLetter(c) || c == '_' || c >= 0x80;
    }

    private static boolean ehParteId(char c) {
        return ehInicioId(c) || Character.isDigit(c);
    }

    // ---------------------------------------------------------------- analisador

    private Grafo construir() {
        lerCabecalho();
        lerCorpo();
        if (atual().tipo != Tipo.FIM) {
            throw erro(atual().linha, "há conteúdo depois do '}' que fecha o grafo.");
        }
        return montarGrafo();
    }

    private void lerCabecalho() {
        Token t = proximo();
        if (ehPalavra(t, "strict")) {
            throw erro(t.linha, "grafos 'strict' não são suportados.");
        }
        if (ehPalavra(t, "digraph")) {
            orientado = true;
        } else if (ehPalavra(t, "graph")) {
            orientado = false;
        } else {
            throw erro(t.linha, "o arquivo deve começar com 'graph' ou 'digraph'.");
        }

        if (atual().tipo == Tipo.ID) {
            proximo(); // nome do grafo (opcional)
        }
        exigirSimbolo("{");
    }

    private void lerCorpo() {
        while (true) {
            Token t = atual();
            if (t.tipo == Tipo.FIM) {
                throw erro(t.linha, "faltou fechar o grafo com '}'.");
            }
            if (ehSimbolo(t, "}")) {
                proximo();
                return;
            }
            if (ehSimbolo(t, ";")) {
                proximo();
                continue;
            }
            lerComando();
        }
    }

    private void lerComando() {
        Token t = atual();

        if (ehSimbolo(t, "{") || ehPalavra(t, "subgraph")) {
            throw erro(t.linha, "subgrafos não são suportados.");
        }

        // graph [..], node [..], edge [..]: atributos padrão, ignorados
        if (ehPalavra(t, "graph") || ehPalavra(t, "node") || ehPalavra(t, "edge")) {
            proximo();
            if (!ehSimbolo(atual(), "[")) {
                throw erro(t.linha, "esperado '[' depois de '" + t.texto + "'.");
            }
            lerAtributos();
            return;
        }

        // atribuição solta, como rankdir=LR: ignorada
        if (t.tipo == Tipo.ID && ehSimbolo(espiar(1), "=")) {
            proximo();
            proximo();
            Token valor = atual();
            if (valor.tipo != Tipo.ID) {
                throw erro(valor.linha, "esperado um valor depois de '='.");
            }
            proximo();
            return;
        }

        String primeiro = lerNome();
        if (ehSimbolo(atual(), ":")) {
            throw erro(atual().linha, "portas de vértice (nome:porta) não são suportadas.");
        }

        if (atual().tipo == Tipo.SETA) {
            lerArestas(primeiro, t.linha);
            return;
        }

        registrarVertice(primeiro, t.linha);
        if (ehSimbolo(atual(), "[")) {
            lerAtributos(); // atributos de vértice (cor, rótulo...) não afetam o grafo
        }
    }

    private void lerArestas(String primeiro, int linha) {
        List<String> cadeia = new ArrayList<>();
        cadeia.add(primeiro);

        while (atual().tipo == Tipo.SETA) {
            Token seta = proximo();
            boolean setaOrientada = seta.texto.equals("->");
            if (setaOrientada != orientado) {
                throw erro(seta.linha, "o operador '" + seta.texto + "' não pode ser usado em um "
                        + (orientado ? "digraph (use '->')." : "graph (use '--')."));
            }
            cadeia.add(lerNome());
            if (ehSimbolo(atual(), ":")) {
                throw erro(atual().linha, "portas de vértice (nome:porta) não são suportadas.");
            }
        }

        Map<String, String> atributos = ehSimbolo(atual(), "[") ? lerAtributos() : new HashMap<>();
        String nome = atributos.get("id");
        String label = atributos.get("label");

        for (int i = 0; i < cadeia.size() - 1; i++) {
            String origem = cadeia.get(i);
            String destino = cadeia.get(i + 1);
            registrarVertice(origem, linha);
            registrarVertice(destino, linha);
            arestas.add(new ArestaLida(nome, origem, destino, lerPeso(label, origem, destino, linha), linha));
        }
    }

    private Map<String, String> lerAtributos() {
        Map<String, String> atributos = new HashMap<>();

        while (ehSimbolo(atual(), "[")) {
            proximo();
            while (!ehSimbolo(atual(), "]")) {
                Token chave = atual();
                if (chave.tipo != Tipo.ID) {
                    throw erro(chave.linha, "atributo inválido dentro de '[ ]' (encontrado '"
                            + descrever(chave) + "').");
                }
                proximo();

                String valor = "true";
                if (ehSimbolo(atual(), "=")) {
                    proximo();
                    Token v = atual();
                    if (v.tipo != Tipo.ID) {
                        throw erro(v.linha, "esperado um valor para o atributo '" + chave.texto + "'.");
                    }
                    valor = v.texto;
                    proximo();
                }
                atributos.put(chave.texto, valor);

                if (ehSimbolo(atual(), ";") || ehSimbolo(atual(), ",")) {
                    proximo();
                }
            }
            proximo(); // consome ']'
        }
        return atributos;
    }

    private Integer lerPeso(String label, String origem, String destino, int linha) {
        if (label == null) {
            return null;
        }
        int peso;
        try {
            peso = Integer.parseInt(label.trim());
        } catch (NumberFormatException e) {
            throw erro(linha, "o label '" + label + "' da aresta " + origem + " e " + destino
                    + " não é um peso inteiro.");
        }
        if (peso == 0) {
            throw erro(linha, "a aresta entre " + origem + " e " + destino
                    + " tem peso 0, que não é permitido (o 0 apagaria a aresta da matriz de incidência).");
        }
        return peso;
    }

    private void registrarVertice(String nome, int linha) {
        if (nome.trim().isEmpty()) {
            throw erro(linha, "nome de vértice vazio.");
        }
        vertices.add(nome);
    }

    private String lerNome() {
        Token t = atual();
        if (t.tipo != Tipo.ID) {
            throw erro(t.linha, "esperado o nome de um vértice, mas foi encontrado '" + descrever(t) + "'.");
        }
        if (ehReservada(t)) {
            throw erro(t.linha, "'" + t.texto + "' é palavra reservada do DOT; para usá-la como nome, coloque entre aspas.");
        }
        proximo();
        return t.texto;
    }

    // ---------------------------------------------------------------- montagem do grafo

    private Grafo montarGrafo() {
        boolean valorado = false;
        for (ArestaLida a : arestas) {
            if (a.peso != null) {
                valorado = true;
            }
        }

        Set<String> usados = new HashSet<>();
        for (ArestaLida a : arestas) {
            if (a.nome != null) {
                if (a.nome.trim().isEmpty()) {
                    throw erro(a.linha, "o id da aresta está vazio.");
                }
                if (!usados.add(a.nome)) {
                    throw erro(a.linha, "o id de aresta '" + a.nome + "' aparece mais de uma vez.");
                }
            }
        }

        Grafo g = new Grafo(orientado, valorado, true); // silencioso durante a carga

        for (String v : vertices) {
            g.adicionarVertice(v);
        }

        int contador = 1;
        for (ArestaLida a : arestas) {
            String nome = a.nome;
            if (nome == null) {
                do {
                    nome = "e" + contador++;
                } while (usados.contains(nome));
                usados.add(nome);
            }
            g.adicionarAresta(nome, a.origem, a.destino, a.peso != null ? a.peso : 1);
        }

        g.setSilencioso(false);
        return g;
    }

    // ---------------------------------------------------------------- utilitários

    private Token atual() {
        return tokens.get(pos);
    }

    private Token espiar(int deslocamento) {
        return tokens.get(Math.min(pos + deslocamento, tokens.size() - 1));
    }

    private Token proximo() {
        Token t = tokens.get(pos);
        if (t.tipo != Tipo.FIM) {
            pos++;
        }
        return t;
    }

    private void exigirSimbolo(String simbolo) {
        if (!ehSimbolo(atual(), simbolo)) {
            throw erro(atual().linha, "esperado '" + simbolo + "', mas foi encontrado '" + descrever(atual()) + "'.");
        }
        proximo();
    }

    private static boolean ehSimbolo(Token t, String simbolo) {
        return t.tipo == Tipo.SIMBOLO && t.texto.equals(simbolo);
    }

    private static boolean ehPalavra(Token t, String palavra) {
        return t.tipo == Tipo.ID && !t.entreAspas && t.texto.equalsIgnoreCase(palavra);
    }

    private static boolean ehReservada(Token t) {
        return t.tipo == Tipo.ID && !t.entreAspas && PALAVRAS_RESERVADAS.contains(t.texto.toLowerCase());
    }

    private static String descrever(Token t) {
        return t.tipo == Tipo.FIM ? "fim do arquivo" : t.texto;
    }

    private static IllegalArgumentException erro(int linha, String mensagem) {
        return new IllegalArgumentException("-> Erro: DOT inválido (linha " + linha + "): " + mensagem);
    }
}