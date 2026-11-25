package org.example.dominio;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projetos_grafo")
public class ProjetoGrafo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false)
    private boolean orientado;

    @Column(nullable = false)
    private boolean valorado;

    @Lob
    @Column(nullable = false)
    private String conteudoDot;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private LocalDateTime atualizadoEm;

    @JsonIgnore
    @OneToMany(mappedBy = "projetoGrafo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistoricoExecucaoAlgoritmo> historicoExecucoes = new ArrayList<>();

    public ProjetoGrafo() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    public ProjetoGrafo(String nome, boolean orientado, boolean valorado, String conteudoDot) {
        this();
        this.nome = nome;
        this.orientado = orientado;
        this.valorado = valorado;
        this.conteudoDot = conteudoDot;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public boolean isOrientado() { return orientado; }
    public void setOrientado(boolean orientado) { this.orientado = orientado; }

    public boolean isValorado() { return valorado; }
    public void setValorado(boolean valorado) { this.valorado = valorado; }

    public String getConteudoDot() { return conteudoDot; }
    public void setConteudoDot(String conteudoDot) {
        this.conteudoDot = conteudoDot;
        this.atualizadoEm = LocalDateTime.now();
    }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }

    public List<HistoricoExecucaoAlgoritmo> getHistoricoExecucoes() { return historicoExecucoes; }
    public void setHistoricoExecucoes(List<HistoricoExecucaoAlgoritmo> historicoExecucoes) { this.historicoExecucoes = historicoExecucoes; }
}
