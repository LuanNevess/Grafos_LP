package org.example.dominio;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_execucoes_algoritmo")
public class HistoricoExecucaoAlgoritmo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_grafo_id", nullable = false)
    private ProjetoGrafo projetoGrafo;

    @Column(nullable = false, length = 50)
    private String algoritmo;

    @Column(length = 255)
    private String parametros;

    @Column(nullable = false, length = 1000)
    private String resumoResultado;

    @Column(nullable = false)
    private LocalDateTime executadoEm;

    public HistoricoExecucaoAlgoritmo() {
        this.executadoEm = LocalDateTime.now();
    }

    public HistoricoExecucaoAlgoritmo(ProjetoGrafo projetoGrafo, String algoritmo, String parametros, String resumoResultado) {
        this();
        this.projetoGrafo = projetoGrafo;
        this.algoritmo = algoritmo;
        this.parametros = parametros;
        this.resumoResultado = resumoResultado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ProjetoGrafo getProjetoGrafo() { return projetoGrafo; }
    public void setProjetoGrafo(ProjetoGrafo projetoGrafo) { this.projetoGrafo = projetoGrafo; }

    public String getAlgoritmo() { return algoritmo; }
    public void setAlgoritmo(String algoritmo) { this.algoritmo = algoritmo; }

    public String getParametros() { return parametros; }
    public void setParametros(String parametros) { this.parametros = parametros; }

    public String getResumoResultado() { return resumoResultado; }
    public void setResumoResultado(String resumoResultado) { this.resumoResultado = resumoResultado; }

    public LocalDateTime getExecutadoEm() { return executadoEm; }
    public void setExecutadoEm(LocalDateTime executadoEm) { this.executadoEm = executadoEm; }
}
