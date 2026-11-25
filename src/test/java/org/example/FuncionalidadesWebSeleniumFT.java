package org.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Testes Funcionais de Interface Web (Selenium WebDriver) - Importação DOT e Análises")
public class FuncionalidadesWebSeleniumFT {

    @LocalServerPort
    private int porta;

    private static WebDriver driver;
    private WebDriverWait wait;

    private static final Path PASTA_PRINTS = Paths.get("target", "screenshots-telas");

    @BeforeAll
    static void iniciarNavegador() throws IOException {
        Files.createDirectories(PASTA_PRINTS);
        if (System.getProperty("webdriver.edge.driver") == null) {
            Path driverLocal = Paths.get(System.getenv("LOCALAPPDATA"), "Programs", "edgedriver", "msedgedriver.exe");
            if (Files.exists(driverLocal)) {
                System.setProperty("webdriver.edge.driver", driverLocal.toAbsolutePath().toString());
            }
        }
        EdgeOptions opcoes = new EdgeOptions();
        opcoes.addArguments("--headless=new", "--window-size=1440,1180", "--force-device-scale-factor=1");
        driver = new EdgeDriver(opcoes);
    }

    @AfterAll
    static void encerrarNavegador() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void configurarEspera() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    private void aguardarImagemOpcional() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(6))
                    .until(ExpectedConditions.visibilityOfElementLocated(By.id("imgGrafo")));
        } catch (Exception ignored) {
        }
    }

    private void salvarPrintTela(String nomeArquivo) {
        aguardarImagemOpcional();
        try {
            Thread.sleep(500);
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
            byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Files.write(PASTA_PRINTS.resolve(nomeArquivo), bytes);
        } catch (Exception ignored) {
        }
    }

    @Test
    @Order(1)
    @DisplayName("CT-FUNC-01: Carregar tela inicial, projetos padrão e Matriz de Incidência")
    void ctFunc01_deveCarregarTelaInicialEExibirProjetosEMatriz() {
        driver.get("http://localhost:" + porta);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#listaProjetos li")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#matrizContainer table")));

        String titulo = driver.findElement(By.cssSelector("header h1")).getText();
        String resumo = driver.findElement(By.id("resumoProps")).getText();

        assertTrue(titulo.contains("Grafos Web"));
        assertTrue(resumo.contains("Ordem (|V|):"));
        salvarPrintTela("tela_01_inicial_matriz.png");
    }

    @Test
    @Order(2)
    @DisplayName("CT-FUNC-02: Importar dígrafo valorado em formato DOT válido pela interface")
    void ctFunc02_deveImportarGrafoDotValidoPelaInterfaceWeb() {
        driver.get("http://localhost:" + porta);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("dotNome")));

        WebElement inputNome = driver.findElement(By.id("dotNome"));
        WebElement inputDot = driver.findElement(By.id("dotConteudo"));
        inputNome.clear();
        inputNome.sendKeys("Rede Logística Sul");
        inputDot.clear();
        inputDot.sendKeys("""
                digraph G {
                  POA -> FLN [label="5", id="r1"];
                  FLN -> CWB [label="3", id="r2"];
                  CWB -> POA [label="4", id="r3"];
                  CWB -> SP [label="6", id="r4"];
                }
                """);

        driver.findElement(By.xpath("//button[contains(text(),'Importar DOT')]")).click();

        WebElement alerta = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#alerta.ok")));
        assertEquals("DOT importado!", alerta.getText());

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("resumoProps"), "Ordem (|V|): 4"));
        String resumo = driver.findElement(By.id("resumoProps")).getText();
        assertTrue(resumo.contains("Tamanho (|E|): 4"));
        salvarPrintTela("tela_02_importacao_dot_sucesso.png");
    }

    @Test
    @Order(3)
    @DisplayName("CT-FUNC-03: Rejeitar conteúdo DOT com erro sintático exibindo alerta de erro na tela")
    void ctFunc03_deveRejeitarDotComErroSintaticoExibindoAlerta() {
        driver.get("http://localhost:" + porta);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("dotNome")));

        WebElement inputNome = driver.findElement(By.id("dotNome"));
        WebElement inputDot = driver.findElement(By.id("dotConteudo"));
        inputNome.clear();
        inputNome.sendKeys("Grafo Corrompido");
        inputDot.clear();
        inputDot.sendKeys("digraph G { A -> B "); // falta '}'

        driver.findElement(By.xpath("//button[contains(text(),'Importar DOT')]")).click();

        WebElement alertaErro = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#alerta.error")));
        assertTrue(alertaErro.getText().contains("faltou fechar o grafo com '}'"));
        salvarPrintTela("tela_03_erro_sintatico_dot.png");
    }

    @Test
    @Order(4)
    @DisplayName("CT-FUNC-04: Criar grafo não orientado e adicionar vértices e arestas pela interface")
    void ctFunc04_deveCriarGrafoNaoOrientadoEEditarEstruturaPelaTela() {
        driver.get("http://localhost:" + porta);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("novoNome")));

        driver.findElement(By.id("novoNome")).sendKeys("Malha Urbana");
        WebElement chkOrientado = driver.findElement(By.id("novoOrientado"));
        if (chkOrientado.isSelected()) {
            chkOrientado.click();
        }
        driver.findElement(By.xpath("//button[contains(text(),'Criar Grafo')]")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("alerta"), "Grafo criado!"));

        // Adiciona 3 vértices: V1, V2, V3
        for (String v : new String[]{"V1", "V2", "V3"}) {
            WebElement campoV = driver.findElement(By.id("nomeVertice"));
            campoV.clear();
            campoV.sendKeys(v);
            driver.findElement(By.xpath("//button[contains(text(),'+ Vértice')]")).click();
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("matrizContainer"), v));
        }

        // Adiciona aresta a1 (V1 -- V2, peso 4)
        driver.findElement(By.id("nomeAresta")).sendKeys("a1");
        driver.findElement(By.id("arestaOrigem")).sendKeys("V1");
        driver.findElement(By.id("arestaDestino")).sendKeys("V2");
        WebElement peso = driver.findElement(By.id("arestaPeso"));
        peso.clear();
        peso.sendKeys("4");
        driver.findElement(By.xpath("//button[contains(text(),'+ Aresta')]")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("resumoProps"), "Tamanho (|E|): 1"));
        String resumo = driver.findElement(By.id("resumoProps")).getText();
        assertTrue(resumo.contains("Ordem (|V|): 3"));
        assertTrue(resumo.contains("Folhas: V1, V2"));
        salvarPrintTela("tela_04_edicao_malha_urbana.png");
    }

    @Test
    @Order(5)
    @DisplayName("CT-FUNC-05: Executar algoritmo de Malgrange pela interface web e exibir CFCs")
    void ctFunc05_deveExecutarMalgrangePelaTelaExibindoComponentesFortementeConexas() {
        driver.get("http://localhost:" + porta);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#listaProjetos li.active")));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//strong[text()='Rede Logística Sul']"))).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("resumoProps"), "Ordem (|V|): 4"));
        driver.findElement(By.xpath("//button[text()='Malgrange']")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("resultadoAlg"), "Componentes Fortemente Conexas (2):"));

        String textoResultado = driver.findElement(By.id("resultadoAlg")).getText();
        assertTrue(textoResultado.contains("CFC 1"));
        assertTrue(textoResultado.contains("POA, FLN, CWB"));
        salvarPrintTela("tela_05_malgrange_cfcs.png");
    }

    @Test
    @Order(6)
    @DisplayName("CT-FUNC-06: Executar Busca em Largura (BFS) pela interface web e validar níveis")
    void ctFunc06_deveExecutarBuscaEmLarguraBFSPelaTela() {
        driver.get("http://localhost:" + porta);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#listaProjetos li.active")));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//strong[text()='Rede Logística Sul']"))).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("resumoProps"), "Ordem (|V|): 4"));

        WebElement inputOrigem = driver.findElement(By.id("algOrigem"));
        inputOrigem.clear();
        inputOrigem.sendKeys("POA");

        driver.findElement(By.xpath("//button[text()='BFS']")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("resultadoAlg"), "BUSCA EM LARGURA (BFS)"));

        String textoResultado = driver.findElement(By.id("resultadoAlg")).getText();
        assertTrue(textoResultado.contains("POA → FLN → CWB → SP"));
        salvarPrintTela("tela_06_execucao_bfs.png");
    }
}
