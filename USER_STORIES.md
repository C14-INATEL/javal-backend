# Histórias de Usuário — JAVAL

> Formato: Como <perfil>, eu quero <ação> para que <benefício>.
> Rastreabilidade: História → Issue/PR → Teste automatizado

---

## US-01 — Cadastro de empresa

**Como** gestor de uma indústria,  
**eu quero** cadastrar minha empresa no sistema  
**para que** eu possa acessar e gerenciar os recursos de produção da minha unidade.

| Campo | Detalhe |
|-------|---------|
| Prioridade | Alta |
| Status | Entregue |
| Rastreabilidade | `CompanyServiceTest`, `CompanyControllerTest` |

### Critérios de aceitação

**Cenário 1 — Cadastro bem-sucedido**
- **Given** empresa com dados válidos e únicos no sistema
- **When** o gestor envia o cadastro
- **Then** sistema retorna 201 com os dados da empresa criada

**Cenário 2 — E-mail já cadastrado**
- **Given** e-mail informado já existe no banco
- **When** o gestor tenta cadastrar
- **Then** sistema retorna 400 com mensagem de conflito

**Cenário 3 — CNPJ já cadastrado**
- **Given** CNPJ informado já existe no banco
- **When** o gestor tenta cadastrar
- **Then** sistema retorna 400 com mensagem de conflito

---

## US-02 — Login da empresa

**Como** gestor de uma empresa cadastrada,  
**eu quero** fazer login com e-mail e senha  
**para que** eu acesse o painel de gestão com os dados exclusivos da minha empresa.

| Campo | Detalhe |
|-------|---------|
| Prioridade | Alta |
| Status | Entregue |
| Rastreabilidade | `CompanyServiceTest`, `CompanyControllerTest` |

### Critérios de aceitação

**Cenário 1 — Login com credenciais válidas**
- **Given** empresa cadastrada com credenciais corretas
- **When** o gestor envia e-mail e senha
- **Then** sistema retorna 200 com token JWT e companyId

**Cenário 2 — Credenciais inválidas**
- **Given** senha incorreta
- **When** o gestor tenta fazer login
- **Then** sistema retorna 401

**Cenário 3 — Acesso sem token**
- **Given** gestor não autenticado
- **When** tenta acessar endpoint protegido
- **Then** sistema retorna 403

---

## US-03 — Cadastro de máquina de produção

**Como** gestor logado no sistema,  
**eu quero** cadastrar máquinas de produção vinculadas à minha empresa  
**para que** eu possa gerenciar o parque de equipamentos da minha unidade sem misturar com dados de outras empresas.

| Campo | Detalhe |
|-------|---------|
| Prioridade | Alta |
| Status | Entregue |
| Rastreabilidade | `MaquinaServiceTest`, `MaquinaControllerTest`, `MaquinaRepositoryTest` |

### Critérios de aceitação

**Cenário 1 — Cadastro bem-sucedido**
- **Given** gestor autenticado com dados válidos de máquina
- **When** envia o formulário de cadastro
- **Then** máquina criada com status ATIVA vinculada à empresa, retorna 201

**Cenário 2 — Nome ausente**
- **Given** campo nome vazio
- **When** o gestor tenta cadastrar
- **Then** sistema retorna 400

**Cenário 3 — Isolamento por empresa**
- **Given** existem máquinas de outras empresas no banco
- **When** o gestor lista suas máquinas
- **Then** sistema retorna apenas máquinas da empresa autenticada

---

## US-04 — Gerenciamento de status da máquina

**Como** gestor logado no sistema,  
**eu quero** alterar o status de uma máquina (ATIVA, INATIVA, MANUTENCAO)  
**para que** o sistema reflita a situação real do equipamento e bloqueie operações indevidas.

| Campo | Detalhe |
|-------|---------|
| Prioridade | Alta |
| Status | Entregue |
| Rastreabilidade | `MaquinaServiceTest`, `MaquinaControllerTest` |

### Critérios de aceitação

**Cenário 1 — Alteração bem-sucedida**
- **Given** máquina pertence à empresa autenticada
- **When** gestor altera o status para MANUTENCAO
- **Then** sistema salva o novo status e retorna 200

**Cenário 2 — Máquina de outra empresa**
- **Given** ID informado pertence a máquina de outra empresa
- **When** gestor tenta alterar o status
- **Then** sistema retorna 404

**Cenário 3 — Status nulo**
- **Given** status não informado no request
- **When** gestor tenta fazer a alteração
- **Then** sistema retorna 400

---

## US-05 — Cadastro de produto

**Como** gestor logado no sistema,  
**eu quero** cadastrar os produtos fabricados pela minha empresa  
**para que** eu possa associá-los às ordens de produção.

| Campo | Detalhe |
|-------|---------|
| Prioridade | Alta |
| Status | Entregue |
| Rastreabilidade | `ProdutoServiceTest`, `ProdutoControllerTest`, `ProdutoRepositoryTest` |

### Critérios de aceitação

**Cenário 1 — Cadastro bem-sucedido**
- **Given** gestor autenticado com dados válidos de produto
- **When** envia o formulário
- **Then** produto criado vinculado à empresa, retorna 201

**Cenário 2 — Nome ausente**
- **Given** campo nome vazio
- **When** gestor tenta cadastrar
- **Then** sistema retorna 400

**Cenário 3 — Listagem isolada por empresa**
- **Given** existem produtos de outras empresas no banco
- **When** gestor lista os produtos
- **Then** sistema retorna apenas produtos da empresa autenticada

---

## US-06 — Criação e ciclo de vida da ordem de produção

**Como** gestor logado no sistema,  
**eu quero** criar ordens de produção vinculando produto e máquina e controlar seu ciclo de vida (PENDENTE → EM_PRODUCAO → FINALIZADA)  
**para que** eu acompanhe em tempo real o andamento da produção.

| Campo | Detalhe |
|-------|---------|
| Prioridade | Alta |
| Status | Entregue |
| Rastreabilidade | `OrdemDeProducaoServiceTest`, `OrdemDeProducaoControllerTest`, `OrdemDeProducaoRepositoryTest` |

### Critérios de aceitação

**Cenário 1 — Criação bem-sucedida**
- **Given** produto e máquina ATIVA pertencem à empresa autenticada
- **When** gestor cria ordem com produto, máquina e quantidade
- **Then** ordem criada com status PENDENTE, retorna 201

**Cenário 2 — Máquina inativa**
- **Given** máquina selecionada está INATIVA
- **When** gestor tenta criar a ordem
- **Then** sistema retorna 400

**Cenário 3 — Iniciar ordem**
- **Given** ordem está PENDENTE e máquina está ATIVA
- **When** gestor inicia a ordem
- **Then** status muda para EM_PRODUCAO e dataInicio é registrada

**Cenário 4 — Finalizar ordem**
- **Given** ordem está EM_PRODUCAO
- **When** gestor finaliza a ordem
- **Then** status muda para FINALIZADA e dataFim é registrada

**Cenário 5 — Transição inválida**
- **Given** ordem está PENDENTE
- **When** gestor tenta finalizá-la diretamente
- **Then** sistema retorna 400

---

## US-07 — Registro de falha em máquina

**Como** gestor logado no sistema,  
**eu quero** registrar falhas ocorridas em máquinas informando tipo e descrição  
**para que** o sistema coloque a máquina automaticamente em manutenção e mantenha um histórico de ocorrências.

| Campo | Detalhe |
|-------|---------|
| Prioridade | Alta |
| Status | Entregue |
| Rastreabilidade | `FalhaMaquinaServiceTest`, `FalhaMaquinaControllerTest`, `FalhaMaquinaRepositoryTest` |

### Critérios de aceitação

**Cenário 1 — Registro bem-sucedido**
- **Given** máquina pertence à empresa autenticada
- **When** gestor registra falha com tipo e descrição
- **Then** falha criada, máquina muda para MANUTENCAO automaticamente, retorna 201

**Cenário 2 — Máquina de outra empresa**
- **Given** ID informado não pertence à empresa do gestor
- **When** tenta registrar a falha
- **Then** sistema retorna 404

**Cenário 3 — Descrição ausente**
- **Given** campo descrição vazio
- **When** gestor tenta registrar
- **Then** sistema retorna 400

---

## US-08 — Resolução de falha e retorno da máquina

**Como** gestor logado no sistema,  
**eu quero** registrar a resolução de uma falha informando o que foi feito  
**para que** a máquina volte automaticamente para ATIVA quando não houver mais falhas abertas.

| Campo | Detalhe |
|-------|---------|
| Prioridade | Alta |
| Status | Entregue |
| Rastreabilidade | `FalhaMaquinaServiceTest`, `FalhaMaquinaControllerTest` |

### Critérios de aceitação

**Cenário 1 — Resolução com única falha aberta**
- **Given** apenas uma falha aberta na máquina
- **When** gestor resolve informando a descrição da solução
- **Then** falha recebe dataResolucao, máquina volta para ATIVA, retorna 200

**Cenário 2 — Resolução com outra falha ainda aberta**
- **Given** duas falhas abertas na mesma máquina
- **When** gestor resolve apenas uma
- **Then** falha é resolvida mas máquina permanece em MANUTENCAO

**Cenário 3 — Falha já resolvida**
- **Given** falha já possui dataResolucao
- **When** gestor tenta resolver novamente
- **Then** sistema retorna 400

---

## Resumo de rastreabilidade

| ID | Funcionalidade | Testes relacionados |
|----|---------------|---------------------|
| US-01 | Cadastro de empresa | `CompanyServiceTest`, `CompanyControllerTest` |
| US-02 | Login | `CompanyServiceTest`, `CompanyControllerTest` |
| US-03 | Cadastro de máquina | `MaquinaServiceTest`, `MaquinaControllerTest`, `MaquinaRepositoryTest` |
| US-04 | Status da máquina | `MaquinaServiceTest`, `MaquinaControllerTest` |
| US-05 | Cadastro de produto | `ProdutoServiceTest`, `ProdutoControllerTest`, `ProdutoRepositoryTest` |
| US-06 | Ordem de produção | `OrdemDeProducaoServiceTest`, `OrdemDeProducaoControllerTest`, `OrdemDeProducaoRepositoryTest` |
| US-07 | Registro de falha | `FalhaMaquinaServiceTest`, `FalhaMaquinaControllerTest`, `FalhaMaquinaRepositoryTest` |
| US-08 | Resolução de falha | `FalhaMaquinaServiceTest`, `FalhaMaquinaControllerTest` |