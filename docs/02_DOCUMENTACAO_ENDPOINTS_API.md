# 🔌 2. Documentação Completa dos Endpoints da API — Pediu Patrão

Este documento detalha **todos os endpoints REST** da API do sistema **Pediu Patrão**, abrangendo os 6 controladores da aplicação:
1. **Dashboard Administrativo** (`/api/dashboard`)
2. **Pedidos** (`/api/pedidos`)
3. **Clientes** (`/api/clientes`)
4. **Produtos** (`/api/produtos`)
5. **Usuários** (`/api/usuarios`)
6. **Auditoria** (`/api/auditoria`)

---

## Sumário Geral de Endpoints

| Módulo | Método | Endpoint | Perfis Autorizados | Descrição Resumida |
| :--- | :---: | :--- | :--- | :--- |
| **Dashboard** | `GET` | `/api/dashboard` ou `/api/dashboard/resumo` | `ADMIN`, `GERENTE`, `ATENDENTE` | Dados consolidados do Dashboard (RF01 a RF07) |
| **Dashboard** | `GET` | `/api/dashboard/indicadores` | `ADMIN`, `GERENTE`, `ATENDENTE` | Indicadores gerais de vendas — KPIs (RF01) |
| **Dashboard** | `GET` | `/api/dashboard/vendas-periodo` | `ADMIN`, `GERENTE`, `ATENDENTE` | Evolução diária de vendas para gráficos (RF02) |
| **Dashboard** | `GET` | `/api/dashboard/vendas-categoria` | `ADMIN`, `GERENTE`, `ATENDENTE` | Vendas agrupadas por categoria de produto (RF02) |
| **Dashboard** | `GET` | `/api/dashboard/situacao-pedidos` | `ADMIN`, `GERENTE`, `ATENDENTE` | Quantidade, percentual e valor por status (RF03) |
| **Dashboard** | `GET` | `/api/dashboard/produtos-mais-vendidos` | `ADMIN`, `GERENTE`, `ATENDENTE` | Ranking de produtos mais vendidos (RF04) |
| **Pedidos** | `POST` | `/api/pedidos` | `GERENTE`, `ATENDENTE` | Cria um novo pedido com cálculo seguro no servidor |
| **Pedidos** | `GET` | `/api/pedidos` | `ADMIN`, `GERENTE`, `ATENDENTE` | Lista todos os pedidos cadastrados |
| **Pedidos** | `GET` | `/api/pedidos/{id}` | `ADMIN`, `GERENTE`, `ATENDENTE` | Busca um pedido específico pelo ID |
| **Pedidos** | `PUT` | `/api/pedidos/{id}/status` | `GERENTE`, `ATENDENTE` | Avança o status do pedido no ciclo de vida |
| **Pedidos** | `POST` / `PUT` | `/api/pedidos/{id}/desconto` | `GERENTE` (**Exclusivo**) | Aplica desconto comercial (máx. 20%, antes da saída) |
| **Pedidos** | `POST` / `PUT` | `/api/pedidos/{id}/cancelar` | `GERENTE` (**Exclusivo**) | Cancela pedido mediante justificativa obrigatória |
| **Clientes** | `GET` | `/api/clientes` | `ADMIN`, `GERENTE`, `ATENDENTE` | Lista todos os clientes |
| **Clientes** | `GET` | `/api/clientes/{id}` | `ADMIN`, `GERENTE`, `ATENDENTE` | Busca cliente por ID |
| **Clientes** | `POST` | `/api/clientes` | `GERENTE`, `ATENDENTE` | Cadastra novo cliente |
| **Clientes** | `PUT` | `/api/clientes/{id}` | `GERENTE`, `ATENDENTE` | Atualiza cliente existente (audita cada campo alterado) |
| **Clientes** | `DELETE` | `/api/clientes/{id}` | `ADMIN`, `GERENTE` | Remove cliente por ID |
| **Produtos** | `GET` | `/api/produtos` | `ADMIN`, `GERENTE` | Lista produtos do catálogo |
| **Produtos** | `POST` | `/api/produtos` | `ADMIN`, `GERENTE` | Cadastra novo produto (JSON ou Form) |
| **Produtos** | `PUT` | `/api/produtos/{id}` | `ADMIN`, `GERENTE` | Atualiza produto existente |
| **Produtos** | `DELETE` | `/api/produtos/{id}` | `ADMIN`, `GERENTE` | Exclui produto por ID |
| **Usuários** | `GET` | `/api/usuarios` | `ADMIN` (**Exclusivo**) | Lista todos os usuários (com senha sanitizada) |
| **Usuários** | `GET` | `/api/usuarios/{id}` | `ADMIN` (**Exclusivo**) | Busca usuário por ID |
| **Usuários** | `POST` | `/api/usuarios` | `ADMIN` (**Exclusivo**) | Cria novo usuário com validação de perfil |
| **Usuários** | `PUT` | `/api/usuarios/{id}` | `ADMIN` (**Exclusivo**) | Atualiza dados/perfil/senha de um usuário |
| **Usuários** | `DELETE` | `/api/usuarios/{id}` | `ADMIN` (**Exclusivo**) | Exclui usuário por ID |
| **Auditoria** | `GET` | `/api/auditoria` | `ADMIN`, `GERENTE` | Lista toda a trilha de auditoria (mais recentes primeiro) |
| **Auditoria** | `GET` | `/api/auditoria/entidade/{entidade}` | `ADMIN`, `GERENTE` | Filtra auditoria por entidade (`PEDIDO`, `CLIENTE`, `USUARIO`) |
| **Auditoria** | `GET` | `/api/auditoria/entidade-id/{id}` | `ADMIN`, `GERENTE` | Filtra auditoria pelo ID do registro afetado |
| **Auditoria** | `GET` | `/api/auditoria/usuario/{usuario}` | `ADMIN`, `GERENTE` | Filtra auditoria pelo username do responsável |

---

## 1. Endpoints do Dashboard Administrativo (`/api/dashboard`)

**Controlador:** [`DashboardController.java`](../src/main/java/com/umc/pediupatrao/controller/DashboardController.java)

### Parâmetros de Consulta (Aceitos em todos os endpoints de `/api/dashboard/**`)

| Parâmetro | Local | Tipo | Obrigatório | Exemplo | Descrição |
| :--- | :---: | :--- | :---: | :--- | :--- |
| `dataInicio` | Query | `LocalDate` (`YYYY-MM-DD`) | Não | `2026-11-01` | Filtra pedidos com data de entrada $\ge$ `dataInicio`. |
| `dataFim` | Query | `LocalDate` (`YYYY-MM-DD`) | Não | `2026-11-08` | Filtra pedidos com data de entrada $\le$ `dataFim`. |
| `status` | Query | `String` | Não | `FINALIZADO` | Filtra por status (`TODOS`, `RECEBIDO`, `EM_PREPARACAO`, `PRONTO`, `SAIU_PARA_ENTREGA`, `RETIRADO`, `FINALIZADO`, `CANCELADO`). |
| `categoria` | Query | `String` | Não | `Pizza` | Filtra por categoria de produto (`TODAS`, `Pizza`, `Bebida`, `Esfiha`, `Lanche`). |
| `produtoId` | Query | `String` | Não | `prod-1` | Filtra por ID específico de produto. |

---

### 1.1. `GET /api/dashboard` ou `GET /api/dashboard/resumo`
Retorna o objeto completo `DashboardResponseDTO` contendo indicadores, séries gráficas, situação dos pedidos, ranking de produtos e pedidos recentes.

- **Exemplo de Requisição:**
  ```http
  GET /api/dashboard/resumo?dataInicio=2026-11-01&dataFim=2026-11-08&status=TODOS&categoria=TODAS HTTP/1.1
  Host: localhost:8080
  Accept: application/json
  ```
- **Exemplo de Resposta (`200 OK`):**
  ```json
  {
    "indicadores": {
      "faturamentoTotal": 179.80,
      "faturamentoBruto": 186.00,
      "totalDescontos": 6.20,
      "totalPedidos": 3,
      "pedidosValidos": 2,
      "pedidosFinalizados": 1,
      "pedidosEmAndamento": 1,
      "pedidosCancelados": 1,
      "ticketMedio": 89.90,
      "totalItensVendidos": 4,
      "taxaCancelamento": 33.33
    },
    "vendasPorPeriodo": [
      {
        "data": "2026-11-01",
        "dataFormatada": "01/11/2026",
        "quantidadePedidos": 1,
        "faturamento": 124.00,
        "itensVendidos": 3
      },
      {
        "data": "2026-11-02",
        "dataFormatada": "02/11/2026",
        "quantidadePedidos": 1,
        "faturamento": 55.80,
        "itensVendidos": 1
      }
    ],
    "situacaoPedidos": [
      { "status": "RECEBIDO", "descricao": "Recebido", "quantidade": 0, "percentual": 0.0, "valorTotal": 0.0 },
      { "status": "EM_PREPARACAO", "descricao": "Em Preparação", "quantidade": 1, "percentual": 33.33, "valorTotal": 55.80 },
      { "status": "PRONTO", "descricao": "Pronto", "quantidade": 0, "percentual": 0.0, "valorTotal": 0.0 },
      { "status": "SAIU_PARA_ENTREGA", "descricao": "Saiu para Entrega", "quantidade": 0, "percentual": 0.0, "valorTotal": 0.0 },
      { "status": "RETIRADO", "descricao": "Retirado", "quantidade": 0, "percentual": 0.0, "valorTotal": 0.0 },
      { "status": "FINALIZADO", "descricao": "Finalizado", "quantidade": 1, "percentual": 33.33, "valorTotal": 124.00 },
      { "status": "CANCELADO", "descricao": "Cancelado", "quantidade": 1, "percentual": 33.33, "valorTotal": 55.00 }
    ],
    "produtosMaisVendidos": [
      {
        "posicao": 1,
        "produtoId": "prod-1",
        "nomeProduto": "Pizza Calabresa",
        "categoria": "Pizza",
        "quantidadeVendida": 2,
        "faturamentoTotal": 110.00,
        "percentualVendas": 61.18
      }
    ],
    "vendasPorCategoria": [
      { "categoria": "Pizza", "quantidadeVendida": 3, "faturamentoTotal": 165.80, "percentual": 92.21 },
      { "categoria": "Bebida", "quantidadeVendida": 1, "faturamentoTotal": 14.00, "percentual": 7.79 }
    ],
    "pedidosRecentes": [
      {
        "id": "ped-02",
        "nomeCliente": "Ana Paula",
        "resumoItens": "1x Pizza Quatro Queijos",
        "quantidadeItens": 1,
        "status": "EM_PREPARACAO",
        "descricaoStatus": "Em Preparação",
        "valorSubtotal": 62.00,
        "valorDesconto": 6.20,
        "valorTotal": 55.80,
        "dataHoraEntrada": "02/11/2026 20:00",
        "dataHoraSaida": "—",
        "responsavelRegistro": "atendente"
      }
    ],
    "categoriasDisponiveis": ["Pizza", "Bebida"],
    "atualizadoEm": "08/11/2026 09:32:00"
  }
  ```

### 1.2. `GET /api/dashboard/indicadores`
Retorna apenas os indicadores gerais de vendas (`IndicadoresVendasDTO` — RF01).
- **Resposta (`200 OK`):** Objeto `indicadores` idêntico ao bloco acima.

### 1.3. `GET /api/dashboard/vendas-periodo`
Retorna a lista `List<VendaPeriodoDTO>` com a evolução diária de faturamento e pedidos (RF02).

### 1.4. `GET /api/dashboard/vendas-categoria`
Retorna a lista `List<VendaCategoriaDTO>` com faturamento e volume por categoria de produto (RF02).

### 1.5. `GET /api/dashboard/situacao-pedidos`
Retorna a lista `List<SituacaoPedidoDTO>` com a distribuição pelos 7 status do pedido (RF03).

### 1.6. `GET /api/dashboard/produtos-mais-vendidos`
Retorna o ranking `List<ProdutoMaisVendidoDTO>` ordenado por unidades vendidas e faturamento (RF04).

- **Possíveis Erros (`/api/dashboard/**`):**
  - `400 Bad Request`: Quando `dataInicio` é posterior a `dataFim`:
    ```json
    { "erro": "A data inicial não pode ser posterior à data final." }
    ```
  - `302 Found` / `401 Unauthorized`: Usuário não autenticado.

---

## 2. Endpoints de Pedidos (`/api/pedidos`)

**Controlador:** [`PedidoController.java`](../src/main/java/com/umc/pediupatrao/controller/PedidoController.java)

### 2.1. `POST /api/pedidos` — Criar Novo Pedido
- **Perfis Permitidos:** `ATENDENTE`, `GERENTE`
- **Regras:** Preços unitários são validados no catálogo do servidor. Desconto informado na criação só é aceito se o usuário autenticado for `GERENTE` (limitado a 20%).
- **Exemplo de Requisição:**
  ```http
  POST /api/pedidos HTTP/1.1
  Content-Type: application/json

  {
    "clienteId": "cli-01",
    "nomeCliente": "Carlos Eduardo Silva",
    "itens": [
      { "produtoId": "prod-1", "quantidade": 2 },
      { "produtoId": "prod-3", "quantidade": 1 }
    ],
    "descontoPercentual": 0.0
  }
  ```
- **Exemplo de Resposta (`201 Created`):**
  ```json
  {
    "id": "672df12a89c12b0012a34567",
    "clienteId": "cli-01",
    "nomeCliente": "Carlos Eduardo Silva",
    "quantidade": 3,
    "itens": [
      {
        "produtoId": "prod-1",
        "nomeProduto": "Pizza Calabresa",
        "quantidade": 2,
        "precoUnitario": 55.0,
        "subtotal": 110.0
      },
      {
        "produtoId": "prod-3",
        "nomeProduto": "Refrigerante 2L",
        "quantidade": 1,
        "precoUnitario": 14.0,
        "subtotal": 14.0
      }
    ],
    "status": "RECEBIDO",
    "valorSubtotal": 124.0,
    "descontoPercentual": 0.0,
    "valorDesconto": 0.0,
    "valorTotal": 124.0,
    "responsavelRegistro": "atendente",
    "dataHoraEntrada": "2026-11-08T09:15:00",
    "responsavelSaida": null,
    "dataHoraSaida": null,
    "justificativaCancelamento": null
  }
  ```

### 2.2. `GET /api/pedidos` — Listar Pedidos
- **Perfis Permitidos:** `ADMIN`, `GERENTE`, `ATENDENTE`
- **Resposta (`200 OK`):** Array JSON de objetos `Pedido`.

### 2.3. `GET /api/pedidos/{id}` — Buscar Pedido por ID
- **Perfis Permitidos:** `ADMIN`, `GERENTE`, `ATENDENTE`
- **Parâmetro de Rota:** `id` (`String` - ID do pedido).
- **Respostas:** `200 OK` (objeto `Pedido`) ou `404 Not Found` (se não existir).

### 2.4. `PUT /api/pedidos/{id}/status` — Avançar Status do Pedido
- **Perfis Permitidos:** `ATENDENTE`, `GERENTE`
- **Parâmetros:**
  - `id` (Path): ID do pedido.
  - `status` (Query ou Body JSON `{"status": "..."}`): Novo status (`EM_PREPARACAO`, `PRONTO`, `SAIU_PARA_ENTREGA`, `RETIRADO`, `FINALIZADO`).
- **Exemplo de Requisição:**
  ```http
  PUT /api/pedidos/672df12a89c12b0012a34567/status HTTP/1.1
  Content-Type: application/json

  {
    "status": "SAIU_PARA_ENTREGA"
  }
  ```
- **Respostas e Erros:**
  - `200 OK`: Retorna o `Pedido` atualizado (preenchendo `dataHoraSaida` e `responsavelSaida` quando o status é de saída).
  - `400 Bad Request`:
    - Transição inválida: `{"erro": "Transição inválida! Não é permitido transitar de [Recebido] para [Finalizado]."}`
    - Pedido já finalizado/cancelado: `{"erro": "Operação bloqueada: o pedido já está FINALIZADO."}`
    - Tentativa de cancelar por este endpoint: `{"erro": "O cancelamento deve ser realizado pela função de cancelamento com justificativa obrigatória."}`

### 2.5. `POST` ou `PUT /api/pedidos/{id}/desconto` — Aplicar Desconto Gerencial
- **Perfis Permitidos:** Exclusivo `GERENTE` (`ADMIN` e `ATENDENTE` recebem `403 Forbidden`).
- **Parâmetros:**
  - `id` (Path): ID do pedido.
  - `percentual` (Query ou Body JSON `{"percentual": 15.0}`): Valor entre `0.1` e `20.0`.
- **Exemplo de Requisição:**
  ```http
  POST /api/pedidos/672df12a89c12b0012a34567/desconto HTTP/1.1
  Content-Type: application/json

  {
    "percentual": 15.0
  }
  ```
- **Respostas e Erros:**
  - `200 OK`: Retorna o `Pedido` com `descontoPercentual`, `valorDesconto` e `valorTotal` recalculados.
  - `400 Bad Request`:
    - Percentual fora do limite (0% a 20%): `{"erro": "O desconto deve ser um percentual positivo e limitado a no máximo 20%."}`
    - Pedido já saiu para entrega/retirada ou foi concluído: `{"erro": "Desconto só pode ser aplicado antes da saída para entrega ou retirada. Status atual: Saiu para Entrega"}`
  - `403 Forbidden`: Usuário sem perfil `GERENTE`.

### 2.6. `POST` ou `PUT /api/pedidos/{id}/cancelar` — Cancelar Pedido
- **Perfis Permitidos:** Exclusivo `GERENTE` (`ADMIN` e `ATENDENTE` recebem `403 Forbidden`).
- **Parâmetros:**
  - `id` (Path): ID do pedido.
  - `justificativa` (Query ou Body JSON `{"justificativa": "..."}`): Motivo obrigatório do cancelamento.
- **Exemplo de Requisição:**
  ```http
  POST /api/pedidos/672df12a89c12b0012a34567/cancelar HTTP/1.1
  Content-Type: application/json

  {
    "justificativa": "Cliente entrou em contato solicitando o cancelamento por endereço incorreto."
  }
  ```
- **Respostas e Erros:**
  - `200 OK`: Retorna o `Pedido` com `status = "CANCELADO"` e `justificativaCancelamento` preenchida.
  - `400 Bad Request`:
    - Justificativa vazia/ausente: `{"erro": "A justificativa é obrigatória para o cancelamento do pedido."}`
    - Pedido já saiu para entrega ou foi finalizado: `{"erro": "Cancelamento não permitido: o pedido já saiu para entrega/retirada ou foi concluído. Status atual: Finalizado"}`
  - `403 Forbidden`: Usuário sem perfil `GERENTE`.

---

## 3. Endpoints de Clientes (`/api/clientes`)

**Controlador:** [`ClienteController.java`](../src/main/java/com/umc/pediupatrao/controller/ClienteController.java)

### 3.1. `GET /api/clientes` — Listar Clientes
- **Perfis Permitidos:** `ADMIN`, `GERENTE`, `ATENDENTE`
- **Resposta (`200 OK`):** Lista de clientes cadastrados.

### 3.2. `GET /api/clientes/{id}` — Buscar Cliente por ID
- **Perfis Permitidos:** `ADMIN`, `GERENTE`, `ATENDENTE`
- **Respostas:** `200 OK` ou `404 Not Found`.

### 3.3. `POST /api/clientes` — Cadastrar Cliente
- **Perfis Permitidos:** `ATENDENTE`, `GERENTE`
- **Exemplo de Requisição:**
  ```http
  POST /api/clientes HTTP/1.1
  Content-Type: application/json

  {
    "nome": "Maria Oliveira",
    "telefone": "(11) 98765-1234",
    "cep": "08780-000",
    "logradouro": "Rua das Flores",
    "numero": "240",
    "complemento": "Apto 12",
    "bairro": "Centro",
    "cidade": "Mogi das Cruzes",
    "estado": "SP"
  }
  ```
- **Resposta (`201 Created`):** Objeto `Cliente` persistido com `id` gerado e auditoria de criação registrada.

### 3.4. `PUT /api/clientes/{id}` — Atualizar Cliente
- **Perfis Permitidos:** `ATENDENTE`, `GERENTE`
- **Descrição:** Atualiza os dados do cliente e registra na auditoria cada campo individual que sofreu modificação (`valorAnterior` $\rightarrow$ `novoValor`).
- **Respostas:**
  - `200 OK`: Objeto `Cliente` atualizado.
  - `400 Bad Request`: `{"erro": "Cliente não encontrado para atualização."}`
  - `403 Forbidden`: Caso invocado por `ADMIN`.

### 3.5. `DELETE /api/clientes/{id}` — Excluir Cliente
- **Perfis Permitidos:** `ADMIN`, `GERENTE`
- **Resposta:** `204 No Content`.

---

## 4. Endpoints de Produtos (`/api/produtos`)

**Controlador:** [`ProdutoController.java`](../src/main/java/com/umc/pediupatrao/controller/ProdutoController.java)

### 4.1. `GET /api/produtos` — Listar Produtos
- **Perfis Permitidos:** `ADMIN`, `GERENTE`
- **Resposta (`200 OK`):**
  ```json
  [
    {
      "id": "prod-1",
      "nome": "Pizza Calabresa",
      "tipo": "Pizza",
      "preco": 55.0,
      "ativo": true
    }
  ]
  ```

### 4.2. `POST /api/produtos` — Cadastrar Produto
- **Perfis Permitidos:** `ADMIN`, `GERENTE`
- **Content-Types Aceitos:** `application/json` ou `application/x-www-form-urlencoded`
- **Exemplo de Requisição:**
  ```http
  POST /api/produtos HTTP/1.1
  Content-Type: application/json

  {
    "nome": "Pizza Portuguesa",
    "tipo": "Pizza",
    "preco": 60.0,
    "ativo": true
  }
  ```
- **Resposta (`201 Created`):** Objeto `Produto` criado.

### 4.3. `PUT /api/produtos/{id}` — Atualizar Produto
- **Perfis Permitidos:** `ADMIN`, `GERENTE`
- **Resposta (`200 OK`):** Objeto `Produto` atualizado.

### 4.4. `DELETE /api/produtos/{id}` — Excluir Produto
- **Perfis Permitidos:** `ADMIN`, `GERENTE`
- **Resposta:** `204 No Content`.

---

## 5. Endpoints de Usuários (`/api/usuarios`)

**Controlador:** [`UsuarioController.java`](../src/main/java/com/umc/pediupatrao/controller/UsuarioController.java)  
**Permissão:** Exclusivo `ADMIN` (qualquer outro perfil recebe `403 Forbidden`).

### 5.1. `GET /api/usuarios` — Listar Todos os Usuários
- **Exemplo de Resposta (`200 OK`):**
  ```json
  [
    { "id": "usr-1", "username": "admin", "password": "[PROTEGIDO]", "role": "ADMIN" },
    { "id": "usr-2", "username": "gerente", "password": "[PROTEGIDO]", "role": "GERENTE" },
    { "id": "usr-3", "username": "atendente", "password": "[PROTEGIDO]", "role": "ATENDENTE" }
  ]
  ```

### 5.2. `GET /api/usuarios/{id}` — Buscar Usuário por ID
- **Respostas:** `200 OK` (com senha `[PROTEGIDO]`) ou `404 Not Found`.

### 5.3. `POST /api/usuarios` — Criar Novo Usuário
- **Exemplo de Requisição:**
  ```http
  POST /api/usuarios HTTP/1.1
  Content-Type: application/json

  {
    "username": "atendente_noite",
    "password": "senhaSegura123",
    "role": "ATENDENTE"
  }
  ```
- **Respostas e Erros:**
  - `201 Created`:
    ```json
    {
      "id": "usr-4",
      "username": "atendente_noite",
      "password": "[PROTEGIDO]",
      "role": "ATENDENTE"
    }
    ```
  - `400 Bad Request`:
    - `{"erro": "Username é obrigatório."}`
    - `{"erro": "Perfil inválido. Perfis permitidos: ADMIN, GERENTE, ATENDENTE."}`
    - `{"erro": "A senha é obrigatória para cadastro."}`

### 5.4. `PUT /api/usuarios/{id}` — Atualizar Usuário
- **Respostas:** `200 OK` ou `400 Bad Request` (`{"erro": "Usuário não encontrado para atualização."}`).

### 5.5. `DELETE /api/usuarios/{id}` — Remover Usuário
- **Resposta:** `204 No Content`.

---

## 6. Endpoints de Auditoria (`/api/auditoria`)

**Controlador:** [`AuditoriaController.java`](../src/main/java/com/umc/pediupatrao/controller/AuditoriaController.java)  
**Perfis Permitidos:** `ADMIN`, `GERENTE` (`ATENDENTE` recebe `403 Forbidden`).

### 6.1. `GET /api/auditoria` — Listar Todos os Registros
Retorna todos os registros de auditoria em ordem cronológica decrescente (`dataHora DESC`).

- **Exemplo de Resposta (`200 OK`):**
  ```json
  [
    {
      "id": "aud-101",
      "usuario": "gerente",
      "perfil": "GERENTE",
      "operacao": "CANCELAMENTO DE PEDIDO",
      "entidade": "PEDIDO",
      "entidadeId": "ped-03",
      "campoAlterado": "status",
      "valorAnterior": "RECEBIDO",
      "novoValor": "CANCELADO",
      "alteracoes": [
        {
          "campo": "status",
          "valorAnterior": "RECEBIDO",
          "novoValor": "CANCELADO"
        }
      ],
      "justificativa": "Cliente desistiu do pedido",
      "dataHora": "2026-11-08T09:20:00"
    }
  ]
  ```

### 6.2. `GET /api/auditoria/entidade/{entidade}`
- **Parâmetro de Rota:** `entidade` (`PEDIDO`, `CLIENTE` ou `USUARIO`).
- **Exemplo:** `GET /api/auditoria/entidade/PEDIDO`

### 6.3. `GET /api/auditoria/entidade-id/{id}`
- **Parâmetro de Rota:** `id` (Identificador do pedido, cliente ou usuário auditado).
- **Exemplo:** `GET /api/auditoria/entidade-id/ped-03`

### 6.4. `GET /api/auditoria/usuario/{usuario}`
- **Parâmetro de Rota:** `usuario` (`username` do operador que realizou a ação).
- **Exemplo:** `GET /api/auditoria/usuario/gerente`
