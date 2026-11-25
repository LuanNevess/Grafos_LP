package org.example;

import org.example.repositorio.ProjetoGrafoRepositorio;
import org.example.servico.GrafoWebServico;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class GrafosWebApplication implements CommandLineRunner {

    private final GrafoWebServico grafoWebServico;
    private final ProjetoGrafoRepositorio projetoGrafoRepositorio;

    public GrafosWebApplication(GrafoWebServico grafoWebServico, ProjetoGrafoRepositorio projetoGrafoRepositorio) {
        this.grafoWebServico = grafoWebServico;
        this.projetoGrafoRepositorio = projetoGrafoRepositorio;
    }

    public static void main(String[] args) {
        SpringApplication.run(GrafosWebApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (projetoGrafoRepositorio.count() > 0) {
            return;
        }

        importarSeExistir("01 - Dígrafo Orientado", Paths.get("dot", "01_orientado.dot"));
        importarSeExistir("02 - Grafo Não Orientado", Paths.get("dot", "02_nao_orientado.dot"));
        importarSeExistir("03 - Dígrafo com Ciclos (Malgrange)", Paths.get("dot", "03_com_ciclo.dot"));
        importarSeExistir("04 - Grafo Desconexo", Paths.get("dot", "04_desconexo.dot"));

        if (projetoGrafoRepositorio.count() == 0) {
            grafoWebServico.importarDot("Exemplo Rotas Valorado", """
                    digraph G {
                      A; B; C; D;
                      A -> B [label="4", id="e1"];
                      A -> C [label="2", id="e2"];
                      C -> B [label="1", id="e3"];
                      B -> D [label="5", id="e4"];
                      C -> D [label="8", id="e5"];
                    }
                    """);
        }
    }

    private void importarSeExistir(String nome, Path caminho) {
        try {
            if (Files.exists(caminho)) {
                String conteudo = Files.readString(caminho, StandardCharsets.UTF_8);
                grafoWebServico.importarDot(nome, conteudo);
            }
        } catch (Exception ignored) {
            // Ignora arquivos de exemplo inválidos para não impedir inicialização
        }
    }
}
