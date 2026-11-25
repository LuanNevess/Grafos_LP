package org.example.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResultadoMalgrangeDominio {

    private final int numeroComponentes;
    private final List<ComponenteConexa> componentes;
    private final String tabelaExecucao;
    private final String dotOriginal;
    private final String dotReduzido;

    public ResultadoMalgrangeDominio(int numeroComponentes, List<ComponenteConexa> componentes,
                                     String tabelaExecucao, String dotOriginal, String dotReduzido) {
        this.numeroComponentes = numeroComponentes;
        this.componentes = Collections.unmodifiableList(new ArrayList<>(componentes));
        this.tabelaExecucao = tabelaExecucao;
        this.dotOriginal = dotOriginal;
        this.dotReduzido = dotReduzido;
    }

    public int getNumeroComponentes() { return numeroComponentes; }
    public List<ComponenteConexa> getComponentes() { return componentes; }
    public String getTabelaExecucao() { return tabelaExecucao; }
    public String getDotOriginal() { return dotOriginal; }
    public String getDotReduzido() { return dotReduzido; }
}
