# 📋 1. Requisitos Funcionais e Não Funcionais — Sistema e API Pediu Patrão

Este documento especifica os **Requisitos Funcionais (RF)** e os **Requisitos Não Funcionais (RNF)** de toda a solução **Pediu Patrão** (API REST completa de Pedidos, Clientes, Produtos, Usuários, Auditoria e Dashboard Administrativo).

---

## 1. Matriz de Perfis de Acesso do Sistema

O sistema implementa controle de acesso baseado em papéis (*RBAC — Role-Based Access Control*) com três perfis distintos:

| Módulo / Operação da API e Web | `ADMIN` | `GERENTE` | `ATENDENTE` |
| :--- | :---: | :---: | :---: |
| **Gestão de Usuários** (`/api/usuarios/**`, `/usuarios/**`) | ✅ Exclusivo | ❌ Bloqueado (`403`) | ❌ Bloqueado (`403`) |
| **Consulta de Clientes** (`GET /api/clientes/**`, `GET /clientes`) | ✅ Permitido | ✅ Permitido | ✅ Permitido |
| **Cadastro e Edição de Clientes** (`POST/PUT /api/clientes/**`) | ❌ Bloqueado (`403`) | ✅ Permitido | ✅ Permitido |
| **Exclusão de Clientes** (`DELETE /api/clientes/{id}`) | ✅ Permitido | ✅ Permitido | ❌ Bloqueado (`403`) |
| **Gestão de Produtos** (`/api/produtos/**`, `/produtos/**`) | ✅ Permitido | ✅ Permitido | ❌ Bloqueado (`403`) |
| **Consulta de Pedidos** (`GET /api/pedidos/**`, `GET /pedidos`) | ✅ Permitido | ✅ Permitido | ✅ Permitido |
| **Criação e Avanço de Status de Pedidos** (`POST /api/pedidos`, `PUT /api/pedidos/{id}/status`) | ❌ Bloqueado (`403`) | ✅ Permitido | ✅ Permitido |
| **Aplicação de Desconto Comercial (até 20%)** (`POST/PUT /api/pedidos/{id}/desconto`) | ❌ Bloqueado (`403`) | ✅ **Exclusivo** | ❌ Bloqueado (`403`) |
| **Cancelamento de Pedido com Justificativa** (`POST/PUT /api/pedidos/{id}/cancelar`) | ❌ Bloqueado (`403`) | ✅ **Exclusivo** | ❌ Bloqueado (`403`) |
| **Consulta de Auditoria** (`GET /api/auditoria/**`, `GET /auditoria`) | ✅ Permitido | ✅ Permitido | ❌ Bloqueado (`403`) |
| **Dashboard Administrativo e API de Métricas** (`GET /api/dashboard/**`, `GET /dashboard`) | ✅ Permitido | ✅ Permitido | ✅ Permitido |

---

## 2. Requisitos Funcionais (RF) — Sistema e API Completa

### 2.1. Módulo de Usuários, Autenticação e Controle de Acesso (`/api/usuarios`)

| Código | Requisito | Descrição e Regras de Negócio | Classes / Arquivos |
| :--- | :--- | :--- | :--- |
| **RF-SIS-01** | **Autenticação e Atribuição de Perfis** | O sistema deve autenticar usuários a partir da coleção `usuarios` no MongoDB e carregar dinamicamente os perfis `ROLE_ADMIN`, `ROLE_GERENTE` e `ROLE_ATENDENTE`. | [`UsuarioDetailsService.java`](../src/main/java/com/umc/pediupatrao/service/UsuarioDetailsService.java), [`SecurityConfig.java`](../src/main/java/com/umc/pediupatrao/config/SecurityConfig.java) |
| **RF-SIS-02** | **Administração Exclusiva de Usuários** | Apenas o perfil `ADMIN` pode listar, buscar, cadastrar, atualizar ou excluir usuários do sistema via `/api/usuarios` e `/usuarios`, bloqueando autoelevação de privilégios e validando perfis contra o enum `PerfilUsuario`. | [`UsuarioController.java`](../src/main/java/com/umc/pediupatrao/controller/UsuarioController.java), [`UsuarioService.java`](../src/main/java/com/umc/pediupatrao/service/UsuarioService.java), [`PerfilUsuario.java`](../src/main/java/com/umc/pediupatrao/entity/enums/PerfilUsuario.java) |
| **RF-SIS-03** | **Proteção e Sanitização de Credenciais** | As senhas devem ser armazenadas exclusivamente com hash `BCrypt` e nunca devem ser expostas nas respostas da API (substituídas por `[PROTEGIDO]`) nem nos registros de auditoria (`[REDACTED]`). | [`UsuarioController.java`](../src/main/java/com/umc/pediupatrao/controller/UsuarioController.java), [`UsuarioService.java`](../src/main/java/com/umc/pediupatrao/service/UsuarioService.java) |

---

### 2.2. Módulo de Clientes (`/api/clientes`) e Produtos (`/api/produtos`)

| Código | Requisito | Descrição e Regras de Negócio | Classes / Arquivos |
| :--- | :--- | :--- | :--- |
| **RF-SIS-04** | **Gestão de Clientes** | A API deve permitir listar e buscar clientes (`ADMIN`, `GERENTE`, `ATENDENTE`), cadastrar e atualizar dados cadastrais e de endereço (`GERENTE`, `ATENDENTE`) e excluir registros (`ADMIN`, `GERENTE`). | [`ClienteController.java`](../src/main/java/com/umc/pediupatrao/controller/ClienteController.java), [`ClienteService.java`](../src/main/java/com/umc/pediupatrao/service/ClienteService.java), [`Cliente.java`](../src/main/java/com/umc/pediupatrao/entity/Cliente.java) |
| **RF-SIS-05** | **Gestão do Catálogo de Produtos** | A API deve permitir listar, cadastrar, editar e remover produtos do cardápio (`Pizza`, `Bebida`, `Esfiha`, `Lanche`) com nome, categoria (`tipo`), preço unitário e indicador `ativo`. Operações de escrita são restritas a `ADMIN` e `GERENTE`. | [`ProdutoController.java`](../src/main/java/com/umc/pediupatrao/controller/ProdutoController.java), [`ProdutoService.java`](../src/main/java/com/umc/pediupatrao/service/ProdutoService.java), [`Produto.java`](../src/main/java/com/umc/pediupatrao/entity/Produto.java) |

---

### 2.3. Módulo de Pedidos, Ciclo de Vida e Operações do Gerente (`/api/pedidos`)

| Código | Requisito | Descrição e Regras de Negócio | Classes / Arquivos |
| :--- | :--- | :--- | :--- |
| **RF-SIS-06** | **Registro de Pedidos com Cálculo no Servidor** | O cadastro de pedidos (`ATENDENTE` e `GERENTE`) deve sincronizar o preço unitário dos itens diretamente do catálogo de produtos no banco de dados e calcular `valorSubtotal`, `valorDesconto` e `valorTotal` estritamente no servidor, impedindo manipulação de preços via requisição HTTP. | [`PedidoController.java`](../src/main/java/com/umc/pediupatrao/controller/PedidoController.java), [`PedidoService.java`](../src/main/java/com/umc/pediupatrao/service/PedidoService.java), [`Pedido.java`](../src/main/java/com/umc/pediupatrao/entity/Pedido.java) |
| **RF-SIS-07** | **Controle de Entrada, Saída e Ciclo de Vida** | Todo pedido inicia em `RECEBIDO` (registrando `dataHoraEntrada` e `responsavelRegistro`) e segue transições estritas: `RECEBIDO` → `EM_PREPARACAO` → `PRONTO` → (`SAIU_PARA_ENTREGA` ou `RETIRADO`) → `FINALIZADO`. Ao transitar para status de saída (`SAIU_PARA_ENTREGA` ou `RETIRADO`), o sistema registra automaticamente `dataHoraSaida` e `responsavelSaida`. Pedidos `FINALIZADO` ou `CANCELADO` são imutáveis. | [`StatusPedido.java`](../src/main/java/com/umc/pediupatrao/entity/enums/StatusPedido.java), [`PedidoService.java`](../src/main/java/com/umc/pediupatrao/service/PedidoService.java) |
| **RF-SIS-08** | **Aplicação de Desconto Exclusiva do Gerente** | Exclusivamente o perfil `GERENTE` pode conceder descontos percentuais de até **20%** sobre o pedido, e somente enquanto o pedido estiver nos status anteriores à saída (`RECEBIDO`, `EM_PREPARACAO` ou `PRONTO`). | [`PedidoController.java`](../src/main/java/com/umc/pediupatrao/controller/PedidoController.java), [`PedidoService.java`](../src/main/java/com/umc/pediupatrao/service/PedidoService.java) |
| **RF-SIS-09** | **Cancelamento de Pedido Exclusivo do Gerente** | Exclusivamente o perfil `GERENTE` pode cancelar um pedido, mediante preenchimento de **justificativa obrigatória** e apenas antes da saída para entrega/retirada (`RECEBIDO`, `EM_PREPARACAO` ou `PRONTO`). | [`PedidoController.java`](../src/main/java/com/umc/pediupatrao/controller/PedidoController.java), [`PedidoService.java`](../src/main/java/com/umc/pediupatrao/service/PedidoService.java) |

---

### 2.4. Módulo de Auditoria Persistente (`/api/auditoria`)

| Código | Requisito | Descrição e Regras de Negócio | Classes / Arquivos |
| :--- | :--- | :--- | :--- |
| **RF-SIS-10** | **Trilha de Auditoria Imutável** | Todas as operações críticas (criação/avanço/desconto/cancelamento de pedidos, criação/alteração campo a campo/exclusão de clientes, e criação/alteração/exclusão de usuários) devem gerar registros persistentes na coleção `auditorias`, armazenando usuário, perfil, data/hora, operação, entidade, ID, campo alterado, valor anterior, novo valor e justificativa. A consulta via `/api/auditoria` é restrita a `ADMIN` e `GERENTE`. | [`AuditoriaController.java`](../src/main/java/com/umc/pediupatrao/controller/AuditoriaController.java), [`AuditoriaService.java`](../src/main/java/com/umc/pediupatrao/service/AuditoriaService.java), [`Auditoria.java`](../src/main/java/com/umc/pediupatrao/entity/Auditoria.java) |

---

### 2.5. Módulo do Dashboard Administrativo (`/api/dashboard` e `/dashboard`)

| Código | Requisito | Descrição e Regras de Negócio | Classes / Arquivos |
| :--- | :--- | :--- | :--- |
| **RF01** | **Indicadores gerais de vendas** | Apresentar KPIs consolidados: Faturamento Líquido (`R$`), Faturamento Bruto (`R$`), Total de Descontos (`R$`), Total de Pedidos, Pedidos Válidos, Pedidos em Andamento, Pedidos Finalizados, Pedidos Cancelados, Taxa de Cancelamento (`%`), Ticket Médio (`R$`) e Total de Itens Vendidos. Pedidos `CANCELADO` não somam no faturamento líquido nem no ticket médio. | [`IndicadoresVendasDTO.java`](../src/main/java/com/umc/pediupatrao/dto/IndicadoresVendasDTO.java), [`DashboardService.java`](../src/main/java/com/umc/pediupatrao/service/DashboardService.java) |
| **RF02** | **Análise de vendas (gráficos)** | Apresentar gráficos dinâmicos para análise de tendências: (1) Evolução de vendas por período (faturamento diário e quantidade de pedidos); (2) Proporção de pedidos por situação (gráfico de rosca); e (3) Faturamento por categoria de produto (gráfico de barras horizontais). | [`VendaPeriodoDTO.java`](../src/main/java/com/umc/pediupatrao/dto/VendaPeriodoDTO.java), [`VendaCategoriaDTO.java`](../src/main/java/com/umc/pediupatrao/dto/VendaCategoriaDTO.java), [`home.html`](../src/main/resources/templates/home.html) |
| **RF03** | **Situação dos pedidos** | Exibir o quantitativo, o percentual de participação e o valor acumulado em cada um dos 7 status do ciclo de vida (`RECEBIDO`, `EM_PREPARACAO`, `PRONTO`, `SAIU_PARA_ENTREGA`, `RETIRADO`, `FINALIZADO`, `CANCELADO`), acompanhado da listagem de pedidos recentes filtrados. | [`SituacaoPedidoDTO.java`](../src/main/java/com/umc/pediupatrao/dto/SituacaoPedidoDTO.java), [`PedidoResumoDashboardDTO.java`](../src/main/java/com/umc/pediupatrao/dto/PedidoResumoDashboardDTO.java) |
| **RF04** | **Produtos mais vendidos** | Exibir o ranking de produtos mais vendidos ordenado por quantidade comercializada e faturamento líquido gerado, com posição (`1º`, `2º`, `3º`...), categoria e participação percentual nas vendas. | [`ProdutoMaisVendidoDTO.java`](../src/main/java/com/umc/pediupatrao/dto/ProdutoMaisVendidoDTO.java), [`DashboardService.java`](../src/main/java/com/umc/pediupatrao/service/DashboardService.java) |
| **RF05** | **Filtros de consulta** | Permitir filtrar todas as visões do dashboard e endpoints da API por período rápido (*Tudo*, *Hoje*, *7 Dias*, *30 Dias*), intervalo de datas (*Data Inicial* e *Data Final*), *Situação do Pedido*, *Categoria* e *Produto*. | [`DashboardController.java`](../src/main/java/com/umc/pediupatrao/controller/DashboardController.java), [`home.html`](../src/main/resources/templates/home.html) |
| **RF06** | **Integração com a API** | O painel administrativo deve consumir os dados dos endpoints REST (`/api/dashboard/**`) de forma assíncrona via `fetch()`, atualizando indicadores, gráficos e tabelas sem recarregar a página. | [`DashboardController.java`](../src/main/java/com/umc/pediupatrao/controller/DashboardController.java), [`home.html`](../src/main/resources/templates/home.html) |
| **RF07** | **Atualização e tratamento de dados** | Disponibilizar atualização manual e automática configurável (`15s`, `30s`, `60s`), exibir o horário da última sincronização, validar intervalos de datas inválidos (`400 Bad Request`), tratar valores nulos/divisões por zero e normalizar pedidos legados sem lista de itens. | [`DashboardService.java`](../src/main/java/com/umc/pediupatrao/service/DashboardService.java), [`DashboardController.java`](../src/main/java/com/umc/pediupatrao/controller/DashboardController.java) |

---

## 3. Requisitos Não Funcionais (RNF)

| Código | Requisito | Categoria | Especificação Técnica |
| :--- | :--- | :--- | :--- |
| **RNF01** | **Autenticação e Autorização em Camadas** | Segurança | Aplicação de segurança em nível de rota HTTP (`SecurityFilterChain`) e em nível de método de serviço/controlador (`@EnableMethodSecurity(prePostEnabled = true)` com `@PreAuthorize`). |
| **RNF02** | **Proteção CSRF e Sessão** | Segurança | Repositório de token CSRF baseado em cookie (`CookieCsrfTokenRepository`) integrado automaticamente ao interceptador global de `window.fetch` no frontend ([`layout.html`](../src/main/resources/templates/layout.html)). |
| **RNF03** | **Criptografia de Senhas** | Segurança | Senhas de usuários cifradas com algoritmo unidirecional `BCryptPasswordEncoder`. |
| **RNF04** | **Persistência Orientada a Documentos** | Banco de Dados | Armazenamento no **MongoDB Atlas** via Spring Data MongoDB, distribuído nas coleções `usuarios`, `clientes`, `produtos`, `pedidos` e `auditorias`. |
| **RNF05** | **Imutabilidade da Auditoria** | Confiabilidade | A API de auditoria (`/api/auditoria`) expõe exclusivamente métodos de leitura (`GET`), inexistindo endpoints de edição ou exclusão de logs. |
| **RNF06** | **Precisão e Integridade Monetária** | Confiabilidade | Cálculos financeiros executados no backend com arredondamento padrão de 2 casas decimais e proteção contra divisão por zero quando não há pedidos válidos no período. |
| **RNF07** | **Retrocompatibilidade de Dados Legados** | Manutenibilidade | O modelo `Pedido` e o `DashboardService` convertem em tempo de execução documentos antigos (que possuíam apenas `tipoPizza`, `sabor` e `quantidade`) em objetos `ItemPedido` válidos. |
| **RNF08** | **Padronização RESTful e Tratamento de Erros** | Interoperabilidade | Respostas padronizadas em JSON com códigos HTTP semânticos (`200 OK`, `201 Created`, `204 No Content`, `400 Bad Request`, `403 Forbidden`, `404 Not Found`). |
| **RNF09** | **Responsividade e Visualização Gráfica** | Usabilidade | Interface responsiva baseada em Bootstrap 5, Bootstrap Icons e Chart.js v2.9.4 local (`/plugins/chart.js/Chart.min.js`). |
| **RNF10** | **Testabilidade Automatizada** | Qualidade | Cobertura dos fluxos de negócio e endpoints da API por testes unitários automatizados com JUnit 5 e Mockito. |
