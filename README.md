# Gerador de Grafos - Matriz de Incidência em Java

Este projeto implementa uma representação de grafos utilizando **Matriz de Incidência** em Java, com interface interativa via terminal, cálculo de propriedades, busca de caminhos mínimos (Dijkstra) e Árvore/Floresta Geradora Mínima (Kruskal e Prim).

---

## Como Compilar e Executar

### 1. Interface Principal (Terminal Interativo)
Execute a classe principal [Main.java](file:///src/main/java/org/example/Main.java) incluindo as dependências do Maven (`.m2/repository`) no classpath (necessárias para a geração de imagens PNG via `br.com.davesmartins:api`):

```powershell
# No Windows (PowerShell):
$jars = (Get-ChildItem -Path "$env:USERPROFILE\.m2\repository" -Recurse -Filter "*.jar" | Select-Object -ExpandProperty FullName) -join ";"
javac -encoding UTF-8 -cp "$jars;target/classes" -d target/classes (Get-ChildItem src/main/java/org/example/*.java | Select-Object -ExpandProperty FullName)
java -cp "target/classes;$jars" org.example.Main
```

```bash
# No Linux / macOS (Bash):
JARS=$(find ~/.m2/repository -name "*.jar" | tr '\n' ':')
javac -encoding UTF-8 -cp "$JARS:target/classes" -d target/classes src/main/java/org/example/*.java
java -cp "target/classes:$JARS" org.example.Main
```

No menu principal interativo, estão disponíveis as seguintes opções principais:
- **Opções 1 a 4:** Adição e remoção dinâmica de vértices e arestas
- **Opção 5:** Informações e classificações estruturais do grafo
- **Opções 6 e 7:** Fecho transitivo direto e inverso
- **Opção 8 (Algoritmos):**
  - `1. Caminho Mínimo (Dijkstra)`
  - `2. Árvore Geradora Mínima (Kruskal)`
  - `3. Árvore Geradora Mínima (Prim)`
  - `4. Componentes Fortemente Conexas e Grafo Reduzido (Malgrange)`
  - `5. Busca em Largura (BFS)`
  - `6. Busca em Profundidade (DFS)`
  - `0. Voltar`
- **Opção 9 (Visualizar Grafo - PNG):** Gera visualização gráfica do grafo em imagem PNG via Graphviz (DOT)
- **Opção 0:** Sair

---

## Visualização Gráfica do Grafo (Parte A)

O módulo [VisualizadorGrafo.java](file:///src/main/java/org/example/VisualizadorGrafo.java) é responsável por converter a estrutura atual do grafo para a linguagem **DOT** (Graphviz) e renderizar a imagem PNG correspondente utilizando a biblioteca `br.com.davesmartins:api` (Graph Api View).

### Características da Implementação:
1. **Sintaxe DOT Precisa:**
   - **Dígrafos (Grafos Orientados):** Gera declaração `digraph G { ... }` com conectores direcionados (`->`).
   - **Grafos Não Orientados:** Gera declaração `graph G { ... }` com conectores bidirecionais (`--`).
   - **Grafos Valorados:** Apresenta o peso como rótulo da aresta através do atributo `[label="peso"]`.
   - **Vértices Isolados:** Vértices sem arestas conectadas são incluídos explicitamente no arquivo DOT (ex: `v3;`), assegurando sua presença na imagem final.
   - **Identificadores com Caracteres Especiais:** Nomes com espaços ou caracteres especiais são automaticamente formatados entre aspas.
2. **Atualização Dinâmica:**
   - A geração sempre reflete o estado presente em memória. Cada solicitação compila o estado atual sem reaproveitar arquivos antigos de forma estática.
3. **Validação de Grafo Vazio:**
   - Se o grafo não contiver vértices, o sistema avisa o usuário (`"-> Erro: O grafo está vazio (sem vértices). Não é possível gerar imagem."`) em vez de gerar um PNG inconsistente.
4. **Exportação Flexível:**
   - Permite definir caminho de destino personalizado ou usar o padrão `saida/grafo.png`.
   - Suporta tanto geração direta a partir de String (`createStringDotToPng`) quanto a partir de arquivo `.dot` salvo (`salvarArquivoDot` + `createFileDotToPng`).

---

## Componentes Fortemente Conexas e Grafo Reduzido - Malgrange (Parte B)

O [Algoritmo de Malgrange](file:///src/main/java/org/example/Algoritmos.java#L470) determina todas as componentes fortemente conexas (CFCs) de um grafo orientado (dígrafo) e constrói o seu respectivo **Grafo Reduzido $Gr(S, W)$**.

### Fluxo do Algoritmo:
1. **Reaproveitamento de Fechos Transitivos:** A cada iteração sobre os vértices restantes $V'$, seleciona-se um vértice de partida $v \in V'$ e obtêm-se:
   - Fecho transitivo direto $\Gamma^+(v) = \{v\} \cup \text{fechoTransitivoDireto}(v)$, restrito a $V'$.
   - Fecho transitivo inverso $\Gamma^-(v) = \{v\} \cup \text{fechoTransitivoInverso}(v)$, restrito a $V'$.
2. **Identificação da CFC:** A componente fortemente conexa é a interseção $\text{CFC} = \Gamma^+(v) \cap \Gamma^-(v)$.
3. **Redução e Próxima Iteração:** Remove-se a componente encontrada dos vértices restantes ($V' \leftarrow V' \setminus \text{CFC}$) até que todos os vértices tenham sido particionados.
4. **Construção do Grafo Reduzido $Gr(S, W)$:**
   - **Super-vértices ($S$):** Cada CFC identificada torna-se um super-vértice rotulado pelo conjunto de vértices correspondente (ex.: `{v2, v3, v4}`).
   - **Arestas direcionadas ($W$):** Para cada aresta $u \to v$ no dígrafo original com $u \in C_i$ e $v \in C_j$ ($i \neq j$), insere-se uma aresta direcionada simples entre os super-vértices $C_i \to C_j$.

### Apresentação dos Resultados:
- **Tabela de Execução Textual:** Apresenta detalhadamente para cada iteração:
  - Vértice escolhido como ponto de partida;
  - Fecho direto $\Gamma^+(v)$ e fecho inverso $\Gamma^-(v)$;
  - CFC resultante da interseção;
  - Vértices restantes para as próximas iterações.
- **Visualização Gráfica em Duas Imagens (via Graphviz/DOT):**
  - **Grafo Original Colorido:** Destaca os vértices de uma mesma componente com a mesma cor (`style=filled` e `fillcolor` no DOT).
  - **Grafo Reduzido:** Cada super-vértice é rotulado com `{v...}` e decorado com a cor correspondente à sua CFC.

---

### 2. Testes Automatizados (JUnit 5)
Os testes unitários cobrem integralmente as regras do grafo, análises estruturais e os algoritmos:
```bash
mvn test
```
Os testes específicos encontram-se organizados em:
- [VisualizadorGrafoTest.java](file:///src/test/java/org/example/VisualizadorGrafoTest.java) – Geração de DOT e PNG (dígrafos, grafos não orientados, pesos, vértices isolados, alterações dinâmicas e validação de grafo vazio).
- [AlgoritmosTest.java](file:///src/test/java/org/example/AlgoritmosTest.java) – Dijkstra e Árvore Geradora Mínima (Kruskal e Prim).
- [AnalisaGrafoTest.java](file:///src/test/java/org/example/AnalisaGrafoTest.java) e [GrafoTest.java](file:///src/test/java/org/example/GrafoTest.java) – Propriedades estruturais e matriz de incidência.
- [MainTest.java](file:///src/test/java/org/example/MainTest.java) – Fluxos do menu interativo no terminal.

---

## Casos de Teste da Árvore Geradora Mínima (AGM)

Os testes automatizados cobrem os seguintes cenários:

1. **Grafo conexo ponderado com AGM de peso conhecido:**
   - Grafo conexo de 4 vértices e 5 arestas com peso conhecido da AGM igual a 6.
   - Kruskal e Prim são conferidos separadamente, confirmando que ambos encontram a AGM de peso 6 com 3 arestas e árvore conexa.

2. **Grafo com empate de pesos (arestas diferentes, mesmo peso total):**
   - Ciclo $C_4$ com todas as 4 arestas de peso idêntico (peso 2).
   - Kruskal escolhe as arestas `[e_ab, e_bc, e_cd]` (descartando `e_da`).
   - Prim escolhe as arestas `[e_ab, e_bc, e_da]` (descartando `e_cd`).
   - Ambos chegam ao mesmo peso total mínimo (6).

3. **Grafo com arestas paralelas de pesos diferentes entre o mesmo par de vértices:**
   - Multigrafo com arestas paralelas entre $A$ e $B$ (pesos 10 e 3) e entre $B$ e $C$ (pesos 8 e 4).
   - Kruskal e Prim selecionam unicamente as arestas de menor peso (3 e 4), ignorando as arestas paralelas mais pesadas (peso total = 7).

4. **Grafo com pelo menos um laço:**
   - Grafo com laços em $A$ (peso 1) e em $B$ (peso 8).
   - Kruskal e Prim desconsideram e descartam os laços, pois laços geram ciclos e não conectam vértices distintos.

5. **Grafo desconexo (floresta geradora mínima por componente):**
   - Grafo com 3 componentes conexas: componente 1 ($V_1, V_2$), componente 2 ($V_3, V_4, V_5$) e componente 3 ($V_6$, vértice isolado).
   - Kruskal e Prim identificam que o grafo é desconexo e apresentam a floresta geradora mínima decomposta por componente, com seus respectivos vértices, arestas e peso parcial, além do peso total da floresta.

6. **Grafo orientado (sistema informa que a funcionalidade não se aplica):**
   - Dígrafo ($A \to B$).
   - A chamada aos algoritmos de Kruskal e Prim dispara `IllegalArgumentException` informando claramente: `"-> Erro: A Árvore Geradora Mínima só se aplica a grafos não orientados."`.

7. **Grafo com apenas um vértice (AGM vazia, peso total zero):**
   - Grafo trivial com vértice único $U$ e 0 arestas.
   - Kruskal e Prim retornam 1 componente conexa com 0 arestas e peso total 0.

---

## Busca em Largura - BFS (Breadth-First Search)

O módulo de **Busca em Largura (BFS)** em [Algoritmos.java](file:///src/main/java/org/example/Algoritmos.java) implementa a exploração em largura a partir de um vértice inicial configurado pelo usuário.

### Principais Características da Implementação:
1. **Escolha Dinâmica do Vértice Inicial:**
   - O usuário escolhe o vértice de partida tanto pelo terminal quanto via chamada direta a `buscaEmLargura(verticeInicial)` ou `bfs(verticeInicial)`.
   - Vértices inexistentes ou em branco são validados com mensagens amigáveis de erro (`"-> Erro: O vértice inicial '...' não existe."`).

2. **Fila FIFO (First-In, First-Out):**
   - Utiliza uma fila (`Queue<String>`) para gerenciar a descoberta dos vértices.
   - Vértices são visitados e expandidos estritamente na ordem em que são descobertos, garantindo os níveis corretos de distância (nível/menor caminho em arestas).

3. **Respeito Estrito à Orientação do Grafo:**
   - **Dígrafos (Grafos Orientados):** Percorre unicamente arcos no sentido correto (`origem -> destino`). Arcos incidentes reversos não são percorridos.
   - **Grafos Não Orientados:** Arestas podem ser percorridas em ambos os sentidos.
   - **Tratamento de Laços e Ciclos:** Conjunto de visitados impede redescobrimento e loops infinitos.

4. **Tratamento de Grafos Desconexos e Componentes Inalcançáveis:**
   - Em grafos desconexos ou dígrafos sem caminho direcionado a partir do vértice inicial, a busca alcança apenas os vértices da componente conectada a partir da raiz.
   - O sistema informa explicitamente quais vértices foram **alcançados** (com seu nível e antecessor) e lista claramente quais vértices **NÃO foram alcançados**.

5. **Apresentação e Visualização:**
   - **Tabela de Execução Passo a Passo:** Tabela detalhada indicando o passo, vértice desenfileirado, vizinhos descobertos, fila resultante e visitados acumulados.
   - **Árvore de Busca (BFS):** Relaciona as arestas pertencentes à árvore geradora da busca.
   - **Visualização Gráfica (PNG):** Gera imagem colorida destacando a raiz (dourado), alcançados numerados com sua ordem e nível (azul claro), inalcançados (cinza) e arestas da árvore em azul de traço reforçado, sem ocultar os demais elementos do grafo.

---

## Busca em Profundidade - DFS (Depth-First Search)

O módulo de **Busca em Profundidade (DFS)** em [Algoritmos.java](file:///src/main/java/org/example/Algoritmos.java) implementa a exploração em profundidade a partir de um vértice inicial configurado pelo usuário.

### Principais Características da Implementação:
1. **Escolha Dinâmica do Vértice Inicial:**
   - O usuário escolhe o vértice de partida no terminal (menu interativo) ou via API direta `buscaEmProfundidade(verticeInicial)` / `dfs(verticeInicial)`.
   - Vértices inexistentes ou em branco são validados com mensagens amigáveis de erro (`"-> Erro: O vértice inicial '...' não existe."`).

2. **Recursão e Exploração em Profundidade:**
   - Implementado de forma recursiva, descendo pelos ramos até as folhas antes de retroceder (backtracking).
   - Registra a ordem final de visitação explícita (`1º A → 2º B → 3º C...`), profundidade de cada vértice e seu respectivo antecessor na árvore.

3. **Respeito Estrito à Orientação do Grafo:**
   - **Dígrafos (Grafos Orientados):** Percorre unicamente arcos no sentido de saída (`origem -> destino`). Arcos incidentes reversos não são percorridos.
   - **Grafos Não Orientados:** Arestas podem ser percorridas em ambos os sentidos.
   - **Tratamento de Laços e Ciclos:** Conjunto de visitados impede ciclos infinitos ou redescobrimento de nós já explorados.

4. **Tratamento de Grafos Desconexos e Componentes Inalcançáveis:**
   - Se o grafo for desconexo ou o dígrafo não possuir caminho para determinados nós a partir do vértice inicial, a busca alcança exclusivamente os vértices pertencentes à mesma componente alcançável.
   - O sistema informa explicitamente quais vértices foram **alcançados** (com sua numeração de ordem, profundidade e antecessor) e lista claramente quais vértices **NÃO foram alcançados**.

5. **Apresentação e Visualização:**
   - **Ordem Final Explícita:** Apresenta no relatório e no terminal a ordem numerada de visita (`1º, 2º, 3º...`).
   - **Tabela de Execução Passo a Passo:** Tabela detalhada indicando o passo, vértice visitado, ordem final, profundidade, antecessor, vizinhos descobertos e visitados acumulados.
   - **Árvore de Busca (DFS):** Relaciona as arestas pertencentes à árvore geradora da busca em profundidade.
   - **Visualização Gráfica (PNG):** Gera imagem colorida destacando a raiz (dourado), nós alcançados numerados com sua ordem e profundidade (azul claro), nós inalcançados (cinza) e as arestas da árvore em azul de traço reforçado, **sem ocultar os demais elementos do grafo** (demais arestas permanecem visíveis em cinza tracejado).

---


---

## Interface Web (Spring Boot + API REST)

O projeto também funciona como aplicação web completa em Java com **Spring Boot 3**, **Spring Data JPA (H2)** e interface HTML interativa em `src/main/resources/static/index.html`.

### Como Executar no Modo Web

```powershell
mvn spring-boot:run
```

Acesse no navegador:
* **Interface Visual:** `http://localhost:8080`
* **Console H2:** `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:grafosdb`, usuário: `sa`, senha vazia)

### Classes de Domínio em `org.example` e `org.example.dominio` (17 classes principais)
1. `Grafo`
2. `AnalisaGrafo`
3. `Algoritmos`
4. `LeitorDot`
5. `VisualizadorGrafo`
6. `Vertice`
7. `Aresta`
8. `MatrizIncidencia`
9. `PassoExecucaoCaminho`
10. `CaminhoMinimo`
11. `ComponenteConexa`
12. `ArvoreGeradoraMinima`
13. `ResultadoBuscaGrafo`
14. `ResultadoMalgrangeDominio`
15. `ResumoPropriedadesGrafo`
16. `ProjetoGrafo` (`@Entity`)
17. `HistoricoExecucaoAlgoritmo` (`@Entity`)
