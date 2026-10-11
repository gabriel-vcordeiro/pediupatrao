# 🍕 PediuPatrão — Sistema de Gestão de Pedidos, Controle de Acesso e Auditoria

Projeto evoluído para a **Atividade Prática M1 de Engenharia de Software**, com foco em controle rigoroso de acesso com Spring Security, gestão de fluxo e cálculo de pedidos no servidor, operações exclusivas do gerente e mecanismo de auditoria persistente no MongoDB.

---

## 🛠️ Tecnologias Utilizadas
- **Java 21 LTS**
- **Spring Boot 3.4.4**
- **Spring Security** (Autenticação, Autorização e Method Security)
- **Spring Data MongoDB** (MongoDB Atlas)
- **Thymeleaf & Thymeleaf Extras Spring Security**
- **Bootstrap 5 & Bootstrap Icons**
- **JUnit 5, Mockito & Spring Security Test**

---

## 👥 1. Perfis de Acesso e Matriz de Permissões (RF01)

O sistema implementa três perfis de acesso distintos com autorização aplicada no backend via Spring Security (`@PreAuthorize` e `SecurityConfig`):

| Funcionalidade / Operação | ADMIN | GERENTE | ATENDENTE |
| :--- | :---: | :---: | :---: |
| **Administração de Usuários e Perfis** (`/usuarios`, `/api/usuarios`) | ✅ Exclusivo | ❌ Bloqueado | ❌ Bloqueado |
| **Consulta de Clientes** (`/clientes`, `/api/clientes`) | ✅ | ✅ | ✅ |
| **Cadastro e Edição de Clientes** (`/clientes/**`) | ❌ | ✅ | ✅ |
| **Registro de Novos Pedidos** (`/pedidos/salvar`, `/api/pedidos`) | ❌ | ✅ | ✅ |
| **Consulta e Acompanhamento de Pedidos** (`/pedidos`, `/api/pedidos`) | ✅ | ✅ | ✅ |
| **Avanço do Ciclo de Vida do Pedido** (`/pedidos/*/status`) | ❌ | ✅ | ✅ |
| **Aplicação de Descontos Comerciais (até 20%)** | ❌ Bloqueado | ✅ **Exclusivo** | ❌ Bloqueado |
| **Cancelamento de Pedidos com Justificativa** | ❌ Bloqueado | ✅ **Exclusivo** | ❌ Bloqueado |
| **Consulta aos Registros de Auditoria** (`/auditoria`, `/api/auditoria`) | ✅ | ✅ | ❌ Bloqueado |

> ⚠️ **Regra Fundamental**: Somente o **GERENTE** pode cancelar pedidos e aplicar descontos, inclusive quando comparado ao **ADMIN**. O backend bloqueia tentativas de qualquer outro perfil com HTTP 403 Forbidden.

---

## 📋 2. Requisitos Funcionais Implementados

### RF01 — Controle de Usuários e Permissões
- Perfis `ADMIN`, `GERENTE` e `ATENDENTE` modelados e atribuídos dinamicamente no `UsuarioDetailsService` a partir do banco de dados (corrigida a falha anterior onde todos recebiam role `USER`).
- Proteção das APIs e rotas web com `hasRole(...)` e `@EnableMethodSecurity(prePostEnabled = true)`.
- Restrição no cadastro e edição de usuários para evitar autoelevação de privilégios: apenas usuários autenticados como `ADMIN` têm permissão para criar, editar ou alterar perfis.

### RF02 — Evolução do Cadastro de Pedidos
- Entidade `Pedido` complementada com:
  - Lista de itens (`ItemPedido`), quantidade de cada item, preço unitário do catálogo e subtotal.
  - Campos de desconto percentual, valor do desconto e valor total.
  - Registro automático do responsável (`responsavelRegistro`) e data/hora de entrada (`dataHoraEntrada`).
- **Cálculo seguro no servidor**: O valor subtotal, o desconto autorizado e o valor total são calculados estritamente no backend. Requisições HTTP não têm poder de definir arbitrariamente esses totais.

### RF03 — Controle de Entrada, Saída e Ciclo de Vida
- Ciclo de vida obrigatório implementado com validação rígida de transições:
  1. `RECEBIDO` — Registra data/hora de entrada e usuário criador.
  2. `EM_PREPARACAO` — Encaminhado à cozinha.
  3. `PRONTO` — Aguardando retirada ou entrega.
  4. `SAIU_PARA_ENTREGA` ou `RETIRADO` — Registra obrigatoriamente a data/hora e o responsável pela saída (`dataHoraSaida` e `responsavelSaida`).
  5. `FINALIZADO` — Atendimento concluído.
- `CANCELADO` é um estado excepcional, permitido apenas ao GERENTE enquanto o pedido ainda não tiver saído para entrega ou retirada.
- Bloqueio de transições inválidas (ex.: finalizar pedido em preparação) e de qualquer alteração de status após cancelamento ou finalização.

### RF04 — Cancelamentos e Descontos Exclusivos do Gerente
- **Cancelamento**: Exclusivo do perfil GERENTE (`@PreAuthorize("hasRole('GERENTE')")`). Exige justificativa obrigatória, preserva os dados históricos e impede transições posteriores.
- **Desconto**: Exclusivo do perfil GERENTE. Limitado a no máximo 20% do valor do pedido e só pode ser aplicado antes da saída para entrega ou retirada (`RECEBIDO`, `EM_PREPARACAO`, `PRONTO`).

### RF05 — Auditoria Persistente no MongoDB
- Coleção `auditorias` com persistência de todas as operações sensíveis:
  - Criação de pedidos, mudanças de status, aplicação de descontos e cancelamentos.
  - Cadastro de clientes, alterações cadastrais (especialmente logradouro, número, telefone) e exclusões.
  - Criação, alteração e exclusão de usuários (com proteção de senhas).
- Cada registro armazena:
  - Identificação do usuário autenticado e perfil (`usuario`, `perfil`);
  - Data e hora exatas (`dataHora`);
  - Tipo de operação e entidade afetada (`operacao`, `entidade`, `entidadeId`);
  - Campo alterado, valor anterior e novo valor (`campoAlterado`, `valorAnterior`, `novoValor`);
  - Justificativa quando exigida pela regra de negócio.
- Consulta restrita a **ADMIN** e **GERENTE**.
- Registros imutáveis (sem endpoints de alteração ou exclusão).

---

## 🔑 3. Usuários Padrão para Demonstração

| Usuário | Senha | Perfil | Finalidade |
| :--- | :--- | :--- | :--- |
| `admin` | `teste123` | **ADMIN** | Gestão de usuários, consulta de pedidos e auditoria |
| `gerente` | `teste123` | **GERENTE** | Cancelamento com justificativa, desconto (até 20%), pedidos e auditoria |
| `atendente` | `teste123` | **ATENDENTE** | Cadastro de clientes, criação de pedidos e avanço do fluxo |

---

## 🚀 4. Como Executar a Aplicação

   ```bash
   ./mvnw spring-boot:run
   ```
   *(No Windows PowerShell: `.\mvnw.cmd spring-boot:run`)*

   - URL: [http://localhost:8080](http://localhost:8080)
   - Efetue login com um dos usuários padrão (`admin`, `gerente` ou `atendente`).

---

## 📹 5. Roteiro Sugerido para Gravação do Vídeo de Demonstração Individual

1. **Autenticação dos 3 perfis**:
   - Login como `atendente`: mostrar visualização da tela de pedidos e clientes.
   - Login como `gerente`: mostrar opções exclusivas de desconto, cancelamento e auditoria.
   - Login como `admin`: mostrar gestão de usuários e auditoria.
2. **Entrada e fluxo de pedidos (ATENDENTE/GERENTE)**:
   - Cadastrar um novo pedido (ex.: Pizza Calabresa).
   - Demonstrar o avanço de status: `RECEBIDO` ➡️ `EM_PREPARACAO` ➡️ `PRONTO` ➡️ `SAIU_PARA_ENTREGA` (verificar preenchimento automático da data/hora e do responsável pela saída).
3. **Desconto e Cancelamento exclusivos do GERENTE (RF04)**:
   - Criar um novo pedido.
   - Com o `gerente`, aplicar desconto de 15% (mostrar o recálculo do valor total).
   - Cancelar o pedido informando a justificativa obrigatória.
   - Tentar cancelar ou aplicar desconto com outro perfil (ou via API) e demonstrar o bloqueio HTTP 403.
4. **Alteração de dados de cliente (RF05)**:
   - Acessar `/clientes`, editar um cliente existente, alterando o logradouro e o telefone.
5. **Consulta aos registros de auditoria (RF05)**:
   - Acessar `/auditoria` como `admin` ou `gerente`.
   - Mostrar os registros gerados: criação do pedido, aplicação do desconto, cancelamento com justificativa e alteração cadastral do cliente com valor anterior e novo valor.
