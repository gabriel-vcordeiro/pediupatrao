# 📈 4. Dashboard Administrativo Funcional — Arquitetura e Guia de Operação

Este documento detalha a implementação e o funcionamento do **Dashboard Administrativo** integrado ao sistema **Pediu Patrão**, descrevendo sua arquitetura, componentes visuais, regras de negócio e integração com a API.

---

## 1. Visão Geral da Arquitetura do Dashboard

O Dashboard Administrativo foi construído seguindo uma arquitetura desacoplada entre a camada de apresentação e a camada de dados/serviços:

- **Camada de Apresentação (Frontend)**:
  - Template Thymeleaf: [`src/main/resources/templates/home.html`](../src/main/resources/templates/home.html) integrado ao layout principal [`layout.html`](../src/main/resources/templates/layout.html) e menu lateral [`fragments/sidebar.html`](../src/main/resources/templates/fragments/sidebar.html).
  - Biblioteca gráfica: **Chart.js v2.9.4** (`/plugins/chart.js/Chart.min.js`).
  - Comunicação assíncrona: `fetch('/api/dashboard/resumo')` em JavaScript puro (ES6+), atualizando o DOM sem recarregamento de página.
- **Camada de Controle (API REST e Rotas Web)**:
  - [`DashboardController.java`](../src/main/java/com/umc/pediupatrao/controller/DashboardController.java): Expõe os 6 endpoints REST sob `/api/dashboard/**`.
  - [`HomeController.java`](../src/main/java/com/umc/pediupatrao/controller/HomeController.java): Mapeia as rotas `GET /` e `GET /dashboard` para renderizar a estrutura do painel.
- **Camada de Negócio e Agregação**:
  - [`DashboardService.java`](../src/main/java/com/umc/pediupatrao/service/DashboardService.java): Executa a filtragem parametrizada, normalização de dados legados, cálculos financeiros e geração dos DTOs.
- **Camada de Objetos de Transferência de Dados (DTOs)**:
  - [`DashboardResponseDTO.java`](../src/main/java/com/umc/pediupatrao/dto/DashboardResponseDTO.java)
  - [`IndicadoresVendasDTO.java`](../src/main/java/com/umc/pediupatrao/dto/IndicadoresVendasDTO.java)
  - [`VendaPeriodoDTO.java`](../src/main/java/com/umc/pediupatrao/dto/VendaPeriodoDTO.java)
  - [`VendaCategoriaDTO.java`](../src/main/java/com/umc/pediupatrao/dto/VendaCategoriaDTO.java)
  - [`SituacaoPedidoDTO.java`](../src/main/java/com/umc/pediupatrao/dto/SituacaoPedidoDTO.java)
  - [`ProdutoMaisVendidoDTO.java`](../src/main/java/com/umc/pediupatrao/dto/ProdutoMaisVendidoDTO.java)
  - [`PedidoResumoDashboardDTO.java`](../src/main/java/com/umc/pediupatrao/dto/PedidoResumoDashboardDTO.java)

---

## 2. Mapeamento dos Requisitos Funcionais na Interface do Dashboard

### 2.1. RF01 — Indicadores Gerais de Vendas (Cards de KPI)
Localizados no topo do painel, exibem em tempo real:
1. **Faturamento Líquido (`R$`)**: Soma do `valorTotal` de todos os pedidos válidos (não cancelados) no período filtrado, exibindo também no rodapé do card o **Faturamento Bruto** (`valorSubtotal`) e o **Total em Descontos** concedidos (`-R$`).
2. **Total de Pedidos**: Quantidade total de pedidos registrados no filtro, detalhando quantos são **Válidos** e quantos foram **Cancelados**, junto com a **Taxa de Cancelamento (`%`)**.
3. **Ticket Médio (`R$`)**: Calculado pela fórmula $\text{Ticket Médio} = \frac{\text{Faturamento Líquido}}{\text{Pedidos Válidos}}$ (protegido contra divisão por zero).
4. **Itens Vendidos**: Quantidade total de unidades de produtos comercializadas nos pedidos válidos, exibindo no rodapé a quantidade de pedidos **Em Andamento** e **Concluídos (`FINALIZADO`)**.

---

### 2.2. RF02 — Análise de Vendas (Gráficos Interativos)
O painel renderiza 3 gráficos dinâmicos utilizando o **Chart.js**:
1. **Evolução de Vendas por Período (`#chartVendasPeriodo`)**:
   - Gráfico misto (*bar + line*) com dois eixos verticais:
     - Eixo esquerdo (`y-faturamento`): Barras azuis representando o **Faturamento Líquido (`R$`)** de cada dia.
     - Eixo direito (`y-pedidos`): Linha verde representando a **Quantidade de Pedidos** realizados em cada dia.
2. **Pedidos por Situação (`#chartSituacaoPedidos`)**:
   - Gráfico de rosca (*Doughnut*) exibindo a proporção de pedidos em cada etapa operacional com cores semânticas padronizadas (`RECEBIDO`, `EM_PREPARACAO`, `PRONTO`, `SAIU_PARA_ENTREGA`, `RETIRADO`, `FINALIZADO`, `CANCELADO`).
3. **Faturamento por Categoria de Produto (`#chartVendasCategoria`)**:
   - Gráfico de barras horizontais (*horizontalBar*) comparando o faturamento líquido gerado por cada categoria do cardápio (`Pizza`, `Bebida`, `Esfiha`, `Lanche`).

---

### 2.3. RF03 — Situação dos Pedidos
Composto por duas visões complementares:
1. **Painel do Ciclo de Vida (`#containerSituacaoPedidos`)**:
   - Exibe todos os 7 status do pedido com badge colorido, percentual sobre o total de pedidos (`%`), quantidade de pedidos, valor financeiro movimentado (`R$`) e barra de progresso.
   - **Interatividade**: Ao clicar sobre qualquer card de status, o Dashboard filtra automaticamente todos os dados para aquele status (e um segundo clique remove o filtro).
2. **Tabela de Pedidos Recentes (`#tabelaPedidosRecentes`)**:
   - Exibe os pedidos correspondentes aos filtros aplicados, ordenados do mais recente para o mais antigo, apresentando: Cliente, Resumo dos Itens, Quantidade Total de Itens, Status atual, Data/Hora de Entrada, Data/Hora de Saída e Valor Total (com indicação de desconto quando houver).

---

### 2.4. RF04 — Produtos Mais Vendidos
- **Tabela de Ranking (`#tabelaProdutosMaisVendidos`)**:
  - Lista os produtos comercializados em ordem decrescente de unidades vendidas e faturamento líquido.
  - Exibe posição com destaque para os 3 primeiros colocados (`1º`, `2º`, `3º`), Nome do Produto, Categoria, Quantidade Vendida (`un.`), Faturamento Gerado (`R$`) e barra visual de participação percentual (`%`) sobre a receita total de itens.

---

### 2.5. RF05 — Filtros de Consulta
Barra de filtragem completa no topo do Dashboard:
- **Atalhos Rápidos de Período**: Botões `Tudo`, `Hoje`, `7 Dias` e `30 Dias` que preenchem automaticamente as datas e disparam a consulta na API.
- **Data Inicial (`#filtroDataInicio`)** e **Data Final (`#filtroDataFim`)**: Seletores de data para consulta de períodos específicos.
- **Situação do Pedido (`#filtroStatus`)**: Permite filtrar por qualquer etapa do ciclo de vida.
- **Categoria (`#filtroCategoria`)**: Permite isolar o desempenho de uma categoria (`Pizza`, `Bebida`, `Esfiha`, `Lanche`).
- **Produto (`#filtroProdutoId`)**: Permite analisar o desempenho individual de um item do cardápio.
- **Botões `Filtrar` e `Limpar`**: Aplicam ou redefinem todos os filtros instantaneamente.

---

### 2.6. RF06 e RF07 — Integração com a API, Atualização e Tratamento de Dados
- **Consumo Assíncrono (`RF06`)**: A função `carregarDadosDashboard()` constrói a *query string* com `URLSearchParams` e realiza `fetch('/api/dashboard/resumo?...')`, reconstruindo gráficos e indicadores dinamicamente.
- **Atualização Manual e Automática (`RF07`)**:
  - Botão **Atualizar Dados** com ícone animado (*spinner*) durante a requisição.
  - Seletor **Auto** com opções `Manual`, `15s`, `30s` (padrão) e `60s` gerenciado via `setInterval`.
  - Badge **Última atualização** exibindo o timestamp exato retornado pelo servidor (`atualizadoEm`).
- **Tratamento de Dados e Erros (`RF07`)**:
  - **No Backend**: Normalização automática de pedidos antigos sem lista de itens, recálculo seguro de valores zerados/nulos, proteção contra divisão por zero e validação de `dataInicio > dataFim` (`HTTP 400`).
  - **No Frontend**: Exibição de banner de alerta (`#alertaErroApi`) com botão *Tentar novamente* caso ocorra erro de validação ou falha de comunicação, além de estados vazios (*empty states*) amigáveis nas tabelas e gráficos quando um filtro não retorna registros.

---

## 3. Como Executar e Validar o Dashboard

1. Na raiz do projeto, inicie o servidor Spring Boot:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
2. Abra o navegador em:
   - `http://localhost:8080/login`
3. Autentique-se com um dos usuários padrão inicializados pelo [`DataInitializer.java`](../src/main/java/com/umc/pediupatrao/config/DataInitializer.java):
   - **Gerente**: `gerente` / `teste123`
   - **Admin**: `admin` / `teste123`
   - **Atendente**: `atendente` / `teste123`
4. Ao efetuar login, você será direcionado automaticamente para o **Dashboard Administrativo** (`/` ou `/dashboard`).
