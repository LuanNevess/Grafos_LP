package org.example.dominio;

public class Vertice {

    private String nome;
    private int grauTotal;
    private int grauEntrada;
    private int grauSaida;
    private boolean folha;
    private boolean fonte;
    private boolean sumidouro;

    public Vertice() {}

    public Vertice(String nome, int grauTotal, int grauEntrada, int grauSaida,
                   boolean folha, boolean fonte, boolean sumidouro) {
        this.nome = nome;
        this.grauTotal = grauTotal;
        this.grauEntrada = grauEntrada;
        this.grauSaida = grauSaida;
        this.folha = folha;
        this.fonte = fonte;
        this.sumidouro = sumidouro;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getGrauTotal() { return grauTotal; }
    public void setGrauTotal(int grauTotal) { this.grauTotal = grauTotal; }

    public int getGrauEntrada() { return grauEntrada; }
    public void setGrauEntrada(int grauEntrada) { this.grauEntrada = grauEntrada; }

    public int getGrauSaida() { return grauSaida; }
    public void setGrauSaida(int grauSaida) { this.grauSaida = grauSaida; }

    public boolean isFolha() { return folha; }
    public void setFolha(boolean folha) { this.folha = folha; }

    public boolean isFonte() { return fonte; }
    public void setFonte(boolean fonte) { this.fonte = fonte; }

    public boolean isSumidouro() { return sumidouro; }
    public void setSumidouro(boolean sumidouro) { this.sumidouro = sumidouro; }
}
