describe('Testes Funcionais E2E - Grafos Web (Cypress)', () => {
  it('CT-FUNC-01: Deve carregar tela inicial, projetos padrão e Matriz de Incidência', () => {
    cy.visit('/');
    cy.get('#listaProjetos li').should('have.length.greaterThan', 0);
    cy.get('#matrizContainer table').should('be.visible');
    cy.get('header h1').should('contain', 'Grafos Web');
    cy.get('#resumoProps').should('contain', 'Ordem (|V|):');
    cy.get('#imgGrafo').should('be.visible');
    cy.screenshot('tela_01_inicial_matriz', { capture: 'viewport' });
  });

  it('CT-FUNC-02: Deve importar dígrafo valorado em formato DOT válido pela interface', () => {
    cy.visit('/');
    cy.get('#dotNome').clear().type('Rede Logística Sul');
    cy.get('#dotConteudo').clear().type(
      'digraph G {\n' +
      '  POA -> FLN [label="5", id="r1"];\n' +
      '  FLN -> CWB [label="3", id="r2"];\n' +
      '  CWB -> POA [label="4", id="r3"];\n' +
      '  CWB -> SP [label="6", id="r4"];\n' +
      '}',
      { parseSpecialCharSequences: false }
    );
    cy.contains('button', 'Importar DOT').click();
    cy.get('#alerta.ok').should('be.visible').and('have.text', 'DOT importado!');
    cy.get('#resumoProps').should('contain', 'Ordem (|V|): 4').and('contain', 'Tamanho (|E|): 4');
    cy.get('#imgGrafo').should('be.visible');
    cy.screenshot('tela_02_importacao_dot_sucesso', { capture: 'viewport' });
  });

  it('CT-FUNC-03: Deve rejeitar conteúdo DOT com erro sintático exibindo alerta de erro', () => {
    cy.visit('/');
    cy.get('#dotNome').clear().type('Grafo Corrompido');
    cy.get('#dotConteudo').clear().type('digraph G { A -> B ', { parseSpecialCharSequences: false });
    cy.contains('button', 'Importar DOT').click();
    cy.get('#alerta.error')
      .should('be.visible')
      .and('contain', "faltou fechar o grafo com '}'");
    cy.screenshot('tela_03_erro_sintatico_dot', { capture: 'viewport' });
  });

  it('CT-FUNC-04: Deve criar grafo não orientado e adicionar vértices e arestas pela tela', () => {
    cy.visit('/');
    cy.get('#novoNome').clear().type('Malha Urbana');
    cy.get('#novoOrientado').uncheck();
    cy.contains('button', 'Criar Grafo').click();
    cy.get('#alerta.ok').should('contain', 'Grafo criado!');

    ['V1', 'V2', 'V3'].forEach((v) => {
      cy.get('#nomeVertice').clear().type(v);
      cy.contains('button', '+ Vértice').click();
      cy.get('#matrizContainer').should('contain', v);
    });

    cy.get('#nomeAresta').clear().type('a1');
    cy.get('#arestaOrigem').clear().type('V1');
    cy.get('#arestaDestino').clear().type('V2');
    cy.get('#arestaPeso').clear().type('4');
    cy.contains('button', '+ Aresta').click();

    cy.get('#resumoProps')
      .should('contain', 'Ordem (|V|): 3')
      .and('contain', 'Tamanho (|E|): 1')
      .and('contain', 'Folhas: V1, V2');
    cy.get('#imgGrafo').should('be.visible');
    cy.screenshot('tela_04_edicao_malha_urbana', { capture: 'viewport' });
  });

  it('CT-FUNC-05: Deve executar algoritmo de Malgrange pela interface e exibir CFCs', () => {
    cy.visit('/');
    cy.contains('#listaProjetos strong', 'Rede Logística Sul').click();
    cy.get('#resumoProps').should('contain', 'Ordem (|V|): 4');
    cy.contains('button', /^Malgrange$/).click();
    cy.get('#resultadoAlg')
      .should('contain', 'Componentes Fortemente Conexas (2):')
      .and('contain', 'CFC 1')
      .and('contain', 'POA, FLN, CWB');
    cy.get('#imgGrafo').should('be.visible');
    cy.screenshot('tela_05_malgrange_cfcs', { capture: 'viewport' });
  });

  it('CT-FUNC-06: Deve executar Busca em Largura (BFS) pela interface e validar níveis', () => {
    cy.visit('/');
    cy.contains('#listaProjetos strong', 'Rede Logística Sul').click();
    cy.get('#resumoProps').should('contain', 'Ordem (|V|): 4');
    cy.get('#algOrigem').clear().type('POA');
    cy.contains('button', 'BFS').click();
    cy.get('#resultadoAlg')
      .should('contain', 'BUSCA EM LARGURA (BFS)')
      .and('contain', 'POA → FLN → CWB → SP');
    cy.get('#imgGrafo').should('be.visible');
    cy.screenshot('tela_06_execucao_bfs', { capture: 'viewport' });
  });
});
