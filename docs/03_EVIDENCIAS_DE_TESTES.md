# 🧪 3. Evidências de Testes Realizados — API Completa e Dashboard Administrativo

Este documento apresenta os planos, cenários e evidências de execução dos testes automatizados de toda a API do sistema **Pediu Patrão** (abrangendo os módulos de **Dashboard**, **Pedidos**, **Clientes**, **Produtos**, **Usuários** e **Auditoria**).

---

## 1. Estrutura da Suíte de Testes Automatizados

Os testes foram implementados com **JUnit 5 (Jupiter)**, **Mockito** e **Spring Security Context** em duas classes de teste:

1. [`DashboardApiTest.java`](../src/test/java/com/umc/pediupatrao/DashboardApiTest.java) — **7 casos de teste** validando todos os endpoints e regras de negócio do Dashboard Administrativo (`/api/dashboard/**`, RF01 a RF07).
2. [`PediuPatraoApiTest.java`](../src/test/java/com/umc/pediupatrao/PediuPatraoApiTest.java) — **9 casos de teste** validando os endpoints operacionais da API (`/api/pedidos`, `/api/clientes`, `/api/produtos`, `/api/usuarios` e `/api/auditoria`).

---

## 2. Matriz Completa dos Casos de Teste Executados (16 Testes)

### 2.1. Testes da API do Dashboard Administrativo ([`DashboardApiTest`](../src/test/java/com/umc/pediupatrao/DashboardApiTest.java))

| ID | Caso de Teste | Endpoint / Alvo | Cenário e Dados de Entrada | Resultado Verificado (Asserções) | Status |
| :--- | :--- | :--- | :--- | :--- | :---: |
| **CT01** | `deveCalcularIndicadoresGeraisDeVendas` | `GET /api/dashboard/indicadores` (**RF01**) | 3 pedidos: `ped-01` (`FINALIZADO`, R$ 124,00), `ped-02` (`EM_PREPARACAO`, R$ 62,00 com 10% desc = R$ 55,80) e `ped-03` (`CANCELADO`, R$ 55,00). | HTTP `200 OK`; `totalPedidos = 3`, `pedidosValidos = 2`, `pedidosCancelados = 1`, `faturamentoBruto = 186.00`, `totalDescontos = 6.20`, `faturamentoTotal = 179.80`, `ticketMedio = 89.90`, `totalItensVendidos = 4`, `taxaCancelamento = 33.33%`. | ✅ **PASSOU** |
| **CT02** | `deveRetornarEvolucaoVendasPorPeriodo` | `GET /api/dashboard/vendas-periodo` (**RF02**) | Pedidos válidos distribuídos nas datas `2026-11-01` e `2026-11-02`. | HTTP `200 OK`; lista ordenada cronologicamente: `2026-11-01` (`R$ 124,00`, 1 pedido) e `2026-11-02` (`R$ 55,80`, 1 pedido). | ✅ **PASSOU** |
| **CT03** | `deveRetornarSituacaoDosPedidos` | `GET /api/dashboard/situacao-pedidos` (**RF03**) | Consulta da distribuição de todos os status do enum `StatusPedido`. | HTTP `200 OK`; retorna exatamente os 7 status (`RECEBIDO` a `CANCELADO`) com quantidades, percentuais (`33.33%`) e valores corretos. | ✅ **PASSOU** |
| **CT04** | `deveRetornarRankingProdutosMaisVendidos` | `GET /api/dashboard/produtos-mais-vendidos` (**RF04**) | Itens vendidos nos pedidos válidos: 2x Pizza Calabresa, 1x Pizza Quatro Queijos, 1x Refrigerante 2L. | HTTP `200 OK`; 1º lugar: `Pizza Calabresa` (2 un., `R$ 110,00`), 2º lugar: `Pizza Quatro Queijos` (1 un., `R$ 55,80`), 3º lugar: `Refrigerante 2L` (1 un., `R$ 14,00`). | ✅ **PASSOU** |
| **CT05** | `deveAplicarFiltrosCombinados` | `GET /api/dashboard/resumo` (**RF05 / RF06**) | Filtros: `dataInicio=2026-11-01`, `dataFim=2026-11-01`, `status=FINALIZADO`, `categoria=Bebida`. | HTTP `200 OK`; filtra exatamente 1 pedido e contabiliza apenas o item da categoria `Bebida` (`R$ 14,00` — `Refrigerante 2L`). | ✅ **PASSOU** |
| **CT06** | `deveRejeitarIntervaloDeDatasInvalido` | `GET /api/dashboard/resumo` (**RF07**) | `dataInicio=2026-11-10` posterior a `dataFim=2026-11-01`. | HTTP `400 Bad Request` com mensagem de erro validada. | ✅ **PASSOU** |
| **CT07** | `deveTratarPedidosLegadosSemListaDeItens` | `DashboardService` (**RF07**) | Pedido antigo contendo apenas atributos legados (`tipoPizza="Pizza"`, `sabor="Pizza Calabresa"`, `quantidade=2`, `itens=[]`). | Normalização automática em memória; `faturamentoTotal = 110.00` e `Pizza Calabresa` incluída no ranking sem exceções. | ✅ **PASSOU** |

---

### 2.2. Testes da API Operacional Completa ([`PediuPatraoApiTest`](../src/test/java/com/umc/pediupatrao/PediuPatraoApiTest.java))

| ID | Caso de Teste | Endpoint / Alvo | Cenário e Dados de Entrada | Resultado Verificado (Asserções) | Status |
| :--- | :--- | :--- | :--- | :--- | :---: |
| **CT08** | `deveCriarPedidoCalculandoValoresNoServidor` | `POST /api/pedidos` | `ATENDENTE` envia pedido com 2x Pizza Calabresa tentando forçar `precoUnitario = 1.00` e `descontoPercentual = 15%`. | HTTP `201 Created`; servidor ignora preço manipulado e aplica `R$ 55,00` do catálogo (`valorSubtotal = 110.00`), zera o desconto não autorizado para `ATENDENTE` (`0.0%`), define `status = RECEBIDO` e grava auditoria. | ✅ **PASSOU** |
| **CT09** | `deveAvancarStatusDoPedidoNoFluxoValido` | `PUT /api/pedidos/{id}/status` | Avanço de `PRONTO` para `SAIU_PARA_ENTREGA` pelo `atendente`. | HTTP `200 OK`; status atualizado para `SAIU_PARA_ENTREGA`, preenchimento automático de `dataHoraSaida` e `responsavelSaida = "atendente"`, e registro na auditoria. | ✅ **PASSOU** |
| **CT10** | `deveBloquearTransicaoInvalidaDeStatus` | `PUT /api/pedidos/{id}/status` | Tentativa de pular etapas: transitar diretamente de `RECEBIDO` para `FINALIZADO`. | HTTP `400 Bad Request` bloqueando a transição ilegal. | ✅ **PASSOU** |
| **CT11** | `deveAplicarDescontoGerencialComLimiteDe20PorCento` | `POST /api/pedidos/{id}/desconto` | `GERENTE` aplica `15%` em pedido de `R$ 100,00` (`EM_PREPARACAO`), e em seguida tenta aplicar `25%`. | Para `15%`: HTTP `200 OK` (`valorDesconto = 15.00`, `valorTotal = 85.00`) + auditoria. Para `25%`: HTTP `400 Bad Request` bloqueando desconto acima de 20%. | ✅ **PASSOU** |
| **CT12** | `deveCancelarPedidoComJustificativaObrigatoria` | `POST /api/pedidos/{id}/cancelar` | `GERENTE` tenta cancelar sem justificativa (`"   "`) e depois cancela com justificativa válida. | Sem justificativa: HTTP `400 Bad Request`. Com justificativa: HTTP `200 OK`, `status = CANCELADO`, justificativa persistida e auditoria gravada. | ✅ **PASSOU** |
| **CT13** | `deveCadastrarEAtualizarClienteComAuditoria` | `POST /api/clientes` e `PUT /api/clientes/{id}` | Cadastro de cliente seguido de alteração de `telefone`, `logradouro` e `numero`. | HTTP `201 Created` na criação e `200 OK` na edição; dispara registros individuais na auditoria com `valorAnterior` e `novoValor` para cada campo alterado. | ✅ **PASSOU** |
| **CT14** | `deveGerenciarProdutosViaApi` | `POST`, `PUT`, `DELETE /api/produtos` | Criação de `Pizza Portuguesa` (`R$ 60,00`), atualização para `R$ 64,00` e exclusão. | HTTP `201 Created` (`ativo = true`), HTTP `200 OK` (`preco = 64.00`) e HTTP `204 No Content`. | ✅ **PASSOU** |
| **CT15** | `deveCriarUsuarioSanitizandoSenhaERejeitarPerfilInvalido` | `POST /api/usuarios` | `ADMIN` cria usuário com perfil `GERENTE` e depois tenta criar usuário com perfil inexistente `SUPER_ROOT`. | Para `GERENTE`: HTTP `201 Created` com senha ocultada (`[PROTEGIDO]`). Para `SUPER_ROOT`: HTTP `400 Bad Request`. | ✅ **PASSOU** |
| **CT16** | `deveConsultarAuditoriaPorEntidadeEUsuario` | `GET /api/auditoria/entidade/{e}` e `/usuario/{u}` | Consulta de trilhas de auditoria filtradas por entidade `PEDIDO` e pelo usuário `gerente`. | HTTP `200 OK` retornando os registros de auditoria correspondentes. | ✅ **PASSOU** |

---

## 3. Saída Real da Execução dos Testes (`.\mvnw.cmd test`)

Comando executado na raiz do projeto:

```powershell
.\mvnw.cmd test
```

Log gerado pelo Maven Surefire Plugin:

```text
[INFO] Scanning for projects...
[INFO] 
[INFO] ------------------------< com.umc:pediupatrao >-------------------------
[INFO] Building pediupatrao 1.0-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- surefire:3.5.2:test (default-test) @ pediupatrao ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.umc.pediupatrao.DashboardApiTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.829 s -- in com.umc.pediupatrao.DashboardApiTest
[INFO] Running com.umc.pediupatrao.PediuPatraoApiTest
09:32:02.533 [main] INFO com.umc.pediupatrao.service.AuditoriaService -- Auditoria registrada: [MUDANÇA DE STATUS] status em PEDIDO (ped-101) por atendente [ATENDENTE]
09:32:02.544 [main] INFO com.umc.pediupatrao.service.AuditoriaService -- Auditoria registrada: [CRIAÇÃO DE PEDIDO] status em PEDIDO (ped-100) por atendente [ATENDENTE]
09:32:02.615 [main] INFO com.umc.pediupatrao.service.AuditoriaService -- Auditoria registrada: [CADASTRO DE USUÁRIO] role em USUARIO (usr-10) por admin [ADMIN]
09:32:02.619 [main] INFO com.umc.pediupatrao.service.AuditoriaService -- Auditoria registrada: [APLICAÇÃO DE DESCONTO] descontoPercentual em PEDIDO (ped-103) por gerente [GERENTE]
09:32:02.621 [main] INFO com.umc.pediupatrao.service.AuditoriaService -- Auditoria registrada: [CANCELAMENTO DE PEDIDO] status em PEDIDO (ped-104) por gerente [GERENTE]
09:32:02.624 [main] INFO com.umc.pediupatrao.service.AuditoriaService -- Auditoria registrada: [CADASTRO DE CLIENTE] nome em CLIENTE (cli-50) por atendente [ATENDENTE]
09:32:02.625 [main] INFO com.umc.pediupatrao.service.AuditoriaService -- Auditoria registrada: [ALTERAÇÃO DE CLIENTE] telefone em CLIENTE (cli-50) por atendente [ATENDENTE]
09:32:02.625 [main] INFO com.umc.pediupatrao.service.AuditoriaService -- Auditoria registrada: [ALTERAÇÃO DE CLIENTE] logradouro em CLIENTE (cli-50) por atendente [ATENDENTE]
09:32:02.625 [main] INFO com.umc.pediupatrao.service.AuditoriaService -- Auditoria registrada: [ALTERAÇÃO DE CLIENTE] numero em CLIENTE (cli-50) por atendente [ATENDENTE]
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.241 s -- in com.umc.pediupatrao.PediuPatraoApiTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  6.043 s
[INFO] Finished at: 2026-11-08T09:32:02-03:00
[INFO] ------------------------------------------------------------------------
```
