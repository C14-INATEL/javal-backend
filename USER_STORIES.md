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
- **Given** os dados da empresa (nome, CNPJ, e-mail, telefone, responsável, senha) são válidos e o e-mail e CNPJ ainda não estão cadastrados
- **When** o gestor envia o formulário de cadastro
- **Then** a empresa é criada e o sistema retorna HTTP 201 com os dados cadastrados

**Cenário 2 — E-mail já cadastrado**
- **Given** o e-mail informado já existe no banco
- **When** o gestor tenta cadastrar
- **Then** o sistema retorna HTTP 400 com mensagem de erro informando o conflito

**Cenário 3 — CNPJ já cadastrado**
- **Given** o CNPJ informado já existe no banco
- **When** o gestor tenta cadastrar
- **Then** o sistema retorna HTTP 400 com mensagem de erro informando o conflito

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
- **Given** a empresa está cadastrada e as credenciais estão corretas
- **When** o gestor envia e-mail e senha
- **Then** o sistema retorna HTTP 200 com token JWT e o companyId da empresa

**Cenário 2 — Credenciais inválidas**
- **Given** a senha informada está incorreta
- **When** o gestor tenta fazer login
- **Then** o sistema retorna HTTP 401

**Cenário 3 — Acesso a rota protegida sem token**
- **Given** o gestor não está autenticado
- **When** tenta acessar qualquer endpoint protegido
- **Then** o sistema retorna HTTP 403

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
- **Given** o gestor está autenticado e informa nome, tipo e capacidade por hora
- **When** envia o formulário de cadastro
- **Then** a máquina é criada com status ATIVA e vinculada à empresa do gestor, retornando HTTP 201

**Cenário 2 — Dados obrigatórios ausentes**
- **Given** o campo nome está vazio
- **When** o gestor tenta cadastrar
- **Then** o sistema retorna HTTP 400 sem criar a máquina

**Cenário 3 — Isolamento por empresa**
- **Given** existem máquinas de outras empresas no banco
- **When** o gestor lista suas máquinas
- **Then** o sistema retorna apenas as máquinas da empresa autenticada

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
- **Given** a máquina pertence à empresa do gestor autenticado
- **When** o gestor altera o status para MANUTENCAO
- **Then** o sistema salva o novo status e retorna HTTP 200

**Cenário 2 — Máquina de outra empresa**
- **Given** o ID informado pertence a uma máquina de outra empresa
- **When** o gestor tenta alterar o status
- **Then** o sistema retorna HTTP 404

**Cenário 3 — Status nulo**
- **Given** o gestor não informa o status
- **When** tenta fazer a alteração
- **Then** o sistema retorna HTTP 400

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
- **Given** o gestor informa nome e tempo de produção unitário válidos
- **When** envia o formulário
- **Then** o produto é criado vinculado à empresa e o sistema retorna HTTP 201

**Cenário 2 — Nome ausente**
- **Given** o campo nome está vazio
- **When** o gestor tenta cadastrar
- **Then** o sistema retorna HTTP 400

**Cenário 3 — Listagem isolada por empresa**
- **Given** existem produtos de outras empresas no banco
- **When** o gestor lista os produtos
- **Then** o sistema retorna apenas os produtos da empresa autenticada

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
- **Given** produto e máquina pertencem à empresa e a máquina está ATIVA
- **When** o gestor cria a ordem informando produto, máquina e quantidade
- **Then** a ordem é criada com status PENDENTE e retorna HTTP 201

**Cenário 2 — Máquina inativa bloqueada**
- **Given** a máquina está INATIVA
- **When** o gestor tenta criar a ordem
- **Then** o sistema retorna HTTP 400 informando que a máquina não pode receber ordens

**Cenário 3 — Iniciar ordem**
- **Given** a ordem está PENDENTE e a máquina está ATIVA
- **When** o gestor inicia a ordem
- **Then** o status muda para EM_PRODUCAO e a dataInicio é registrada

**Cenário 4 — Finalizar ordem**
- **Given** a ordem está EM_PRODUCAO
- **When** o gestor finaliza a ordem
- **Then** o status muda para FINALIZADA e a dataFim é registrada

**Cenário 5 — Bloqueio de transição inválida**
- **Given** a ordem está PENDENTE
- **When** o gestor tenta finalizá-la diretamente
- **Then** o sistema retorna HTTP 400

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
- **Given** a máquina pertence à empresa e está operacional
- **When** o gestor registra uma falha informando tipo e descrição
- **Then** a falha é criada, a máquina muda para MANUTENCAO automaticamente e retorna HTTP 201

**Cenário 2 — Máquina de outra empresa**
- **Given** o ID informado não pertence à empresa do gestor
- **When** tenta registrar a falha
- **Then** o sistema retorna HTTP 404

**Cenário 3 — Descrição ausente**
- **Given** o campo descrição está vazio
- **When** o gestor tenta registrar
- **Then** o sistema retorna HTTP 400

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

**Cenário 1 — Resolução quando é a única falha aberta**
- **Given** existe apenas uma falha aberta na máquina
- **When** o gestor resolve a falha informando a descrição da solução
- **Then** a falha recebe dataResolucao, a máquina volta para ATIVA e retorna HTTP 200

**Cenário 2 — Resolução com outra falha ainda aberta**
- **Given** existem duas falhas abertas na mesma máquina
- **When** o gestor resolve apenas uma
- **Then** a falha é resolvida mas a máquina permanece em MANUTENCAO

**Cenário 3 — Falha já resolvida**
- **Given** a falha já possui dataResolucao
- **When** o gestor tenta resolver novamente
- **Then** o sistema retorna HTTP 400

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