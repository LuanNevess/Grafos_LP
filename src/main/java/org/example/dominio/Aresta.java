package org.example.dominio;

public class Aresta {

    private final String nome;
    private final String origem;
    private final String destino;
    private final int peso;

    public Aresta(String nome, String origem, String destino, int peso) {
        this.nome = nome;
        this.origem = origem;
        this.destino = destino;
        this.peso = peso;
    }

    public String getNome() { return nome; }
    public String getOrigem() { return origem; }
    public String getDestino() { return destino; }
    public int getPeso() { return peso; }
}
