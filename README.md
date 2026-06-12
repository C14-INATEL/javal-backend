# JAVAL — Sistema de Gestão de Produção Industrial

API REST para gestão de produção industrial multiempresa: cada empresa cadastra suas máquinas, produtos, ordens de produção e registra as falhas de suas máquinas, acompanhando tudo por um dashboard de métricas. O acesso é isolado por empresa e protegido por autenticação JWT — cada empresa só enxerga e manipula os próprios dados.

Projeto desenvolvido na disciplina **C14 - Engenharia de Software** do INATEL.

---

## Tecnologias

- **Java 21**
- **Spring Boot 3.2.5** (Web, Data JPA, Validation, Security)
- **PostgreSQL** (banco de produção/desenvolvimento)
- **H2** (banco em memória, usado nos testes)
- **JWT** (jjwt 0.11.5) para autenticação stateless
- **Lombok** para redução de boilerplate
- **Maven** como gerenciador de dependências e build
- **JUnit 5 + Mockito** para testes automatizados
- **Docker / Docker Compose** para orquestração
- **Jenkins** para o pipeline de CI/CD

---

## Pré-requisitos

- **JDK 21** instalado (o projeto não compila em versões anteriores)
- **Docker** e **Docker Compose** (para subir o banco e/ou a aplicação)
- O wrapper do Maven (`mvnw`) já vem incluído — não é necessário instalar o Maven globalmente

---

## Instalação

Clone o repositório e entre na pasta:

```bash
git clone https://github.com/C14-INATEL/javal-backend.git
cd javal-backend
```

A aplicação Spring Boot fica na subpasta `production-system/`. Toda a configuração de banco e segredos é lida de variáveis de ambiente, com valores padrão para desenvolvimento local.

### Configuração local (application.properties)

Por conter o segredo do JWT, o arquivo `application.properties` **não é versionado**. Crie o seu a partir do template incluído:

```bash
cd production-system
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Depois, abra o arquivo e defina um `jwt.secret` próprio (mínimo de 256 bits). Os demais valores já apontam para o banco do Docker Compose.

---

## Execução

Há dois caminhos: subir tudo via Docker (mais simples) ou rodar a aplicação localmente conectando ao banco em container.

### Opção A — Tudo via Docker Compose

Na raiz do projeto, o Compose sobe o **PostgreSQL** e a **aplicação** já configurados e conectados:

```bash
docker-compose up -d
```

A API ficará disponível em `http://localhost:8080`. O banco roda na porta `5433` do host.

### Opção B — Banco no Docker, aplicação local

Útil durante o desenvolvimento, com hot-reload via devtools. Primeiro suba apenas o banco:

```bash
docker-compose up -d db
```

Depois, dentro de `production-system/`, rode a aplicação:

```bash
./mvnw spring-boot:run
```

> No Windows, use `mvnw spring-boot:run`.

As tabelas são criadas automaticamente pelo Hibernate na primeira execução (`ddl-auto=update`), a partir das entidades.

### Verificando o banco

```bash
docker exec -it javal-postgres psql -U javal_user -d javal_db -c "\dt"
```

Devem aparecer as tabelas `companies`, `maquinas`, `produtos`, `ordens_producao` e `falhas_maquina`.

---

## Testes

Os testes usam um banco H2 em memória (perfil `test`) e não dependem do Docker. Dentro de `production-system/`:

```bash
./mvnw test
```

A suíte cobre as camadas de entity, repository, service, controller e security de todos os domínios.

---

## Autenticação

A API é **stateless** e protegida por JWT. Apenas o cadastro e o login são públicos; todas as outras rotas exigem um token válido.

O fluxo é: registrar uma empresa, fazer login para receber o token, e enviar esse token no cabeçalho `Authorization` das demais requisições.

**1. Registrar empresa** (`POST /api/companies/register`)

```json
{
  "name": "Fábrica Teste",
  "cnpj": "12345678000199",
  "email": "contato@fabrica.com",
  "phone": "35999999999",
  "responsibleName": "Maria Silva",
  "password": "senha123"
}
```

**2. Login** (`POST /api/companies/login`) — retorna o `token`:

```json
{
  "email": "contato@fabrica.com",
  "password": "senha123"
}
```

**3. Usar o token** nas demais chamadas:

```
Authorization: Bearer <token>
```

---

## Uso da API

Todos os endpoints abaixo (exceto registro e login) exigem o cabeçalho `Authorization: Bearer <token>`. A empresa dona dos dados é identificada pelo token, então não é necessário informá-la nos corpos das requisições.

### Empresas — `/api/companies`

| Método | Rota | Descrição | Acesso |
|--------|------|-----------|--------|
| POST | `/register` | Cadastra uma nova empresa | Público |
| POST | `/login` | Autentica e retorna o token JWT | Público |

### Produtos — `/api/produtos`

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/` | Cadastra um produto |
| GET | `/` | Lista os produtos da empresa |
| DELETE | `/{id}` | Remove um produto (se não houver ordens vinculadas) |

Corpo de criação:

```json
{ "nome": "Parafuso M8", "tempoProducaoUnitario": 5 }
```

### Máquinas — `/api/maquinas`

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/` | Cadastra uma máquina |
| GET | `/` | Lista as máquinas da empresa |
| PATCH | `/{id}/status` | Altera o status da máquina |
| DELETE | `/{id}` | Remove uma máquina (se não houver falhas ou ordens vinculadas) |

Corpo de criação (o campo `status` é opcional, padrão `ATIVA`):

```json
{ "nome": "Torno CNC 01", "tipo": "Usinagem", "capacidadePorHora": 120, "status": "ATIVA" }
```

Status possíveis: `ATIVA`, `INATIVA`, `MANUTENCAO`.

### Ordens de Produção — `/api/ordens`

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/` | Cria uma ordem de produção |
| GET | `/` | Lista as ordens da empresa |
| POST | `/{id}/iniciar` | Inicia a produção de uma ordem |
| POST | `/{id}/finalizar` | Finaliza uma ordem |
| POST | `/{id}/cancelar` | Cancela uma ordem (somente se estiver PENDENTE) |

Corpo de criação:

```json
{ "produtoId": 1, "maquinaId": 1, "quantidade": 500 }
```

Status possíveis: `PENDENTE`, `EM_PRODUCAO`, `FINALIZADA`, `CANCELADA`.

### Falhas de Máquina — `/api/falhas`

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/` | Registra uma falha (coloca a máquina em MANUTENCAO) |
| GET | `/` | Lista todas as falhas da empresa |
| GET | `/maquina/{maquinaId}` | Histórico de falhas de uma máquina |
| PATCH | `/{id}/resolver` | Resolve a falha; se não houver mais falhas abertas, a máquina volta a ATIVA |

Corpo de registro:

```json
{ "maquinaId": 1, "descricao": "Motor superaquecendo", "severidade": "ALTA" }
```

Severidades: `BAIXA`, `MEDIA`, `ALTA`, `CRITICA`. Status da falha: `ABERTA`, `RESOLVIDA`.

### Dashboard — `/api/dashboard`

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/` | Métricas consolidadas da empresa |

Retorna contadores de máquinas (por status), produtos, ordens (por status), total de unidades produzidas e em aberto, e o ranking das máquinas mais produtivas.

---

## Funcionalidades

- **Autenticação e multiempresa** — cadastro e login de empresas com JWT; cada empresa acessa apenas os próprios dados.
- **Gestão de produtos** — cadastro, listagem e remoção (com validação de vínculos a ordens de produção), com o tempo de produção unitário.
- **Gestão de máquinas** — cadastro, listagem, alteração de status e remoção (com validação de vínculos a falhas ou ordens), com capacidade produtiva por hora.
- **Ordens de produção** — criação vinculando produto e máquina, com ciclo de vida (PENDENTE → EM_PRODUCAO → FINALIZADA) e cancelamento permitido apenas no estado PENDENTE.
- **Registro e rastreamento de falhas** — abertura de falhas por máquina com severidade e datas; o registro coloca a máquina em manutenção e a resolução da última falha aberta a reativa, mantendo o histórico completo de cada máquina.
- **Dashboard de métricas** — visão consolidada da operação da empresa, incluindo ranking de máquinas.

---

## Estrutura do projeto

```
javal-backend/
├── docker-compose.yml          # Orquestra PostgreSQL + aplicação
├── Jenkinsfile                 # Pipeline de CI/CD
└── production-system/          # Aplicação Spring Boot
    ├── pom.xml
    ├── mvnw / mvnw.cmd
    └── src/
        ├── main/java/com/industrial/productionsystem/
        │   ├── config/         # Configuração de segurança
        │   ├── controller/     # Endpoints REST
        │   ├── dto/            # Objetos de request/response
        │   ├── entity/         # Entidades JPA (+ enums)
        │   ├── exception/      # Tratamento global de erros
        │   ├── repository/     # Repositórios Spring Data
        │   ├── security/       # Filtro e utilitários JWT
        │   └── service/        # Regras de negócio
        └── test/               # Testes (entity, repository, service, controller, security)
```

A aplicação segue uma arquitetura em camadas: o `controller` recebe a requisição e identifica a empresa pelo token, o `service` concentra as regras de negócio, e o `repository` faz o acesso a dados sempre filtrando pela empresa.

---

## CI/CD

O pipeline de CI/CD do projeto é executado em um servidor **Jenkins** próprio, hospedado em container Docker. GitHub Actions não é utilizado, conforme exigência da disciplina.

### Servidor Jenkins

O Jenkins é orquestrado via `docker-compose` e fica acessível em `http://localhost:8090` (porta interna do container `8080` mapeada para `8090` no host, evitando conflito com o backend que ocupa a porta `8080`). A interface exibe o histórico de builds executados ao longo do projeto.

### Pipeline (`Jenkinsfile`)

O `Jenkinsfile` na raiz do repositório define o pipeline com cinco stages, distribuídos entre os integrantes — atendendo ao requisito de ao menos um job por integrante, comitado pelo próprio:

| Stage | Responsável | Função |
|-------|-------------|--------|
| Build | Ana | Compila o projeto via `./mvnw clean compile` |
| Testes Controller e Repository | Pettrius | Executa `./mvnw test -Dtest="*ControllerTest,*RepositoryTest"` |
| Testes Service e Entity | Vinícius | Executa `./mvnw test -Dtest="*ServiceTest,*EntityTest"` |
| Package | João | Empacota o JAR via `./mvnw package -DskipTests` |
| Docker Build | Ana | Constrói a imagem Docker da aplicação |

Os relatórios de teste são publicados ao final dos stages de teste via JUnit Publisher do Jenkins, exibindo número de testes, falhas e tempo de execução diretamente na interface.

### Disparo da pipeline

Atualmente, a execução do pipeline é **disparada manualmente** pela interface do Jenkins. A configuração de webhook do GitHub para disparo automático a cada push está prevista mas ainda não foi implementada — para habilitá-la, seria necessário expor o Jenkins em um endereço público (ou via túnel, como ngrok) e cadastrar o webhook nas configurações do repositório no GitHub.

---

## Histórias de Usuário

As funcionalidades acima foram especificadas previamente em forma de histórias de usuário, com critérios de aceitação no formato Given/When/Then e rastreabilidade direta para os testes automatizados que as exercitam.

### US-01 — Cadastro de empresa

**Como** gestor de uma indústria, **eu quero** cadastrar minha empresa no sistema **para que** eu possa acessar e gerenciar os recursos de produção da minha unidade.

| Campo | Detalhe |
|-------|---------|
| Prioridade | Alta |
| Status | Entregue |
| Rastreabilidade | `CompanyServiceTest`, `CompanyControllerTest` |

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

### US-02 — Login da empresa

**Como** gestor de uma empresa cadastrada, **eu quero** fazer login com e-mail e senha **para que** eu acesse o painel de gestão com os dados exclusivos da minha empresa.

| Campo | Detalhe |
|-------|---------|
| Prioridade | Alta |
| Status | Entregue |
| Rastreabilidade | `CompanyServiceTest`, `CompanyControllerTest`, `CompanyServiceLoginTest`, `JwtAuthFilterTest`, `JwtUtilTest` |

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

### US-03 — Cadastro de máquina de produção

**Como** gestor logado no sistema, **eu quero** cadastrar máquinas de produção vinculadas à minha empresa **para que** eu possa gerenciar o parque de equipamentos da minha unidade sem misturar com dados de outras empresas.

| Campo | Detalhe |
|-------|---------|
| Prioridade | Alta |
| Status | Entregue |
| Rastreabilidade | `MaquinaServiceTest`, `MaquinaControllerTest`, `MaquinaRepositoryTest`, `MaquinaEntityTest` |

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

### US-04 — Gerenciamento de status da máquina

**Como** gestor logado no sistema, **eu quero** alterar o status de uma máquina (ATIVA, INATIVA, MANUTENCAO) **para que** o sistema reflita a situação real do equipamento e bloqueie operações indevidas.

| Campo | Detalhe |
|-------|---------|
| Prioridade | Alta |
| Status | Entregue |
| Rastreabilidade | `MaquinaServiceTest`, `MaquinaControllerTest` |

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

### US-05 — Cadastro de produto

**Como** gestor logado no sistema, **eu quero** cadastrar os produtos fabricados pela minha empresa **para que** eu possa associá-los às ordens de produção.

| Campo | Detalhe |
|-------|---------|
| Prioridade | Alta |
| Status | Entregue |
| Rastreabilidade | `ProdutoServiceTest`, `ProdutoControllerTest`, `ProdutoRepositoryTest`, `ProdutoEntityTest` |

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

### US-06 — Criação e ciclo de vida da ordem de produção

**Como** gestor logado no sistema, **eu quero** criar ordens de produção vinculando produto e máquina e controlar seu ciclo de vida (PENDENTE → EM_PRODUCAO → FINALIZADA) **para que** eu acompanhe em tempo real o andamento da produção.

| Campo | Detalhe |
|-------|---------|
| Prioridade | Alta |
| Status | Entregue |
| Rastreabilidade | `OrdemDeProducaoServiceTest`, `OrdemDeProducaoControllerTest`, `OrdemDeProducaoRepositoryTest`, `OrdemDeProducaoEntityTest` |

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

**Cenário 5 — Cancelar ordem pendente**
- **Given** ordem está PENDENTE
- **When** gestor cancela a ordem
- **Then** status muda para CANCELADA

**Cenário 6 — Transição inválida**
- **Given** ordem está EM_PRODUCAO
- **When** gestor tenta cancelar a ordem
- **Then** sistema retorna 400

---

### US-07 — Registro de falha em máquina

**Como** gestor logado no sistema, **eu quero** registrar falhas ocorridas em máquinas informando severidade e descrição **para que** o sistema coloque a máquina automaticamente em manutenção e mantenha um histórico de ocorrências.

| Campo | Detalhe |
|-------|---------|
| Prioridade | Alta |
| Status | Entregue |
| Rastreabilidade | `FalhaMaquinaServiceTest`, `FalhaMaquinaControllerTest` |

**Cenário 1 — Registro bem-sucedido**
- **Given** máquina pertence à empresa autenticada
- **When** gestor registra falha com severidade e descrição
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

### US-08 — Resolução de falha e retorno da máquina

**Como** gestor logado no sistema, **eu quero** registrar a resolução de uma falha **para que** a máquina volte automaticamente para ATIVA quando não houver mais falhas abertas.

| Campo | Detalhe |
|-------|---------|
| Prioridade | Alta |
| Status | Entregue |
| Rastreabilidade | `FalhaMaquinaServiceTest`, `FalhaMaquinaControllerTest` |

**Cenário 1 — Resolução com única falha aberta**
- **Given** apenas uma falha aberta na máquina
- **When** gestor resolve a falha
- **Then** falha recebe dataResolucao, máquina volta para ATIVA, retorna 200

**Cenário 2 — Resolução com outra falha ainda aberta**
- **Given** duas falhas abertas na mesma máquina
- **When** gestor resolve apenas uma
- **Then** falha é resolvida mas máquina permanece em MANUTENCAO

---

### Resumo de rastreabilidade

| ID | Funcionalidade | Testes relacionados |
|----|----------------|---------------------|
| US-01 | Cadastro de empresa | `CompanyServiceTest`, `CompanyControllerTest` |
| US-02 | Login | `CompanyServiceTest`, `CompanyControllerTest`, `CompanyServiceLoginTest`, `JwtAuthFilterTest`, `JwtUtilTest` |
| US-03 | Cadastro de máquina | `MaquinaServiceTest`, `MaquinaControllerTest`, `MaquinaRepositoryTest`, `MaquinaEntityTest` |
| US-04 | Status da máquina | `MaquinaServiceTest`, `MaquinaControllerTest` |
| US-05 | Cadastro de produto | `ProdutoServiceTest`, `ProdutoControllerTest`, `ProdutoRepositoryTest`, `ProdutoEntityTest` |
| US-06 | Ordem de produção | `OrdemDeProducaoServiceTest`, `OrdemDeProducaoControllerTest`, `OrdemDeProducaoRepositoryTest`, `OrdemDeProducaoEntityTest` |
| US-07 | Registro de falha | `FalhaMaquinaServiceTest`, `FalhaMaquinaControllerTest` |
| US-08 | Resolução de falha | `FalhaMaquinaServiceTest`, `FalhaMaquinaControllerTest` |

---

## Metodologia de Desenvolvimento

O grupo adotou uma abordagem **Kanban informal**, sem sprints fixos ou cerimônias de Scrum. A escolha foi motivada pela natureza assíncrona do trabalho acadêmico: as tarefas eram disponibilizadas gradualmente pelo monitor da disciplina via Microsoft Teams, sem datas fixas de iteração. Cada integrante contribuía conforme sua disponibilidade, e o progresso era contínuo em vez de dividido em sprints fechados.

### Kanban na prática

O **GitHub** funcionou como nosso quadro Kanban. Cada Pull Request representava uma tarefa passando pelos estados:

```
Em desenvolvimento (branch pessoal)  -->  Em revisão (PR aberto)  -->  Concluído (mergeado no main)
```

A cada nova atividade publicada pelo monitor no Teams, o grupo discutia a divisão no WhatsApp, criava branches pessoais e abria PRs à medida que cada parte ficava pronta. A visualização do progresso vinha diretamente da aba "Pull Requests" do GitHub — quem estava aberto, quem estava em revisão, quem já tinha mergeado.

### Papéis e Responsabilidades

O grupo **não adotou papéis formais** (PO, Scrum Master, QA dedicado) — todos os integrantes contribuíram em todas as frentes do projeto, com a distribuição surgindo organicamente conforme as habilidades e disponibilidades de cada um.

| Integrante | Atuação |
|------------|---------|
| Ana Júlia | Desenvolvedora backend e líder do grupo |
| João Vítor | Desenvolvedor backend |
| Letícia | Desenvolvedora frontend |
| Pettrius | Desenvolvedor backend e responsável pela documentação |
| Vinícius | Desenvolvedor backend |

### Tomada de Decisão

As decisões técnicas foram tomadas majoritariamente no **grupo do WhatsApp**, com discussões rápidas e consenso entre os integrantes. Reuniões formais foram raras e usadas apenas para alinhamento de prioridades quando uma nova atividade era publicada pelo monitor.

### Ferramentas e Cadência

O desenvolvimento ocorreu em **ciclos definidos pelas datas de publicação** das atividades disponibilizadas pelo monitor no Microsoft Teams. A cada nova atividade publicada, o grupo discutia a divisão de tarefas e cada integrante criava branches próprias para suas contribuições.

**Ferramentas utilizadas:**

- **GitHub** — versionamento de código, Pull Requests e quadro Kanban informal
- **Microsoft Teams** — publicação de atividades pelo monitor
- **WhatsApp** — comunicação rápida e tomada de decisões do grupo

### Definição de Pronto (Definition of Done)

Uma contribuição era considerada pronta quando:

1. O código estava commitado na branch do integrante com mensagem descritiva.
2. Um Pull Request foi aberto para a branch `main`.
3. Os testes passavam localmente (quando aplicável ao tipo de contribuição).
4. Ao menos um outro membro revisou o PR antes do merge.

Na prática, o critério de testes foi aplicado rigorosamente para commits de código. Commits de documentação e CI/CD foram revisados semanticamente, sem exigir execução de testes.

### Métricas do Projeto

| Métrica | Valor |
|---------|-------|
| Pull Requests mergeados na main | 33 |
| Commits na main | 87+ |
| Classes de teste | 27 |
| Métodos de teste (`@Test`) | 99 |
| Stages do pipeline Jenkins | 5 |
| Histórias de usuário | 8 (com Given/When/Then e rastreabilidade) |
| Branches por integrante | 1 branch pessoal + branches de feature/fix |

---

## Dinâmica de Desenvolvimento

### Principal desafio

O maior bloqueio foi sincronizar o trabalho de cinco pessoas com disponibilidades diferentes, sem sprints com datas fixas. Algumas entregas ficaram aguardando enquanto outras estavam prontas há dias, e em alguns momentos houve trabalho duplicado por falta de coordenação fina. Um exemplo concreto: o `DashboardResponse` foi criado de forma quase simultânea em duas branches diferentes — quando o segundo PR foi rebaseado, o arquivo já existia na `main`, gerando campos duplicados que quebraram o Lombok do projeto inteiro e fizeram a build falhar com 100 erros aparentes (na verdade derivados de um único arquivo com sintaxe quebrada).

### Conflitos e resolução

**Conflito 1 — `DashboardResponse` duplicado:** durante o desenvolvimento da feature de falhas de máquina, o arquivo `DashboardResponse.java` foi reconstruído numa branch antes que o desenvolvedor percebesse que o arquivo oficial já havia sido mergeado na `main` por outro membro. Ao tentar rodar o build, os campos duplicados impediam o Lombok de processar as anotações e o erro cascateava em ~97 falsos positivos do tipo `cannot find symbol getId()` em classes não relacionadas. Resolvido substituindo o arquivo pela versão oficial da `main`.

**Conflito 2 — Padrão de teste inconsistente:** o `CompanyControllerTest` foi escrito sem excluir o `JwtAuthFilter` no `@WebMvcTest`, enquanto outros controllers (Máquina, Produto) faziam essa exclusão. Como o filtro depende do `JwtUtil`, que não está disponível no contexto reduzido do `@WebMvcTest`, a inicialização do Spring quebrava com `No qualifying bean of type 'JwtUtil'`. Resolvido replicando o padrão dos controllers que funcionavam.

**Conflito 3 — Lazy loading no front:** após a entrega da feature de falhas, a desenvolvedora frontend reportou `LazyInitializationException` ao consumir o endpoint `GET /api/falhas`. As relações `@ManyToOne` com `Maquina` e `Company` estavam configuradas como `FetchType.LAZY`, e o `FalhaMaquinaResponse.from()` acessava essas relações fora de uma sessão Hibernate ativa. Resolvido anotando o `FalhaMaquinaService` com `@Transactional`, garantindo que a sessão permanecesse aberta durante a montagem do DTO.

**Conflito 4 — CORS bloqueando PATCH:** requisições PATCH do frontend foram bloqueadas pela política de CORS do backend, impedindo operações de atualização. Resolvido na branch `fix/cors-patch-method` com ajuste na configuração do Spring Security.

### Lições aprendidas

- **Sempre atualizar a branch local com a `main` antes de criar branches de feature.** O conflito do `DashboardResponse` teria sido evitado com um `git pull origin main` antes do início do trabalho.
- **Padronizar o estilo dos testes desde o primeiro arquivo.** O `JwtAuthFilter` precisava ser excluído em todos os `@WebMvcTest`, mas isso só virou regra depois que três testes falharam por motivos relacionados.
- **Anotar serviços com `@Transactional` quando montam DTOs a partir de entidades com relações lazy.** Testes com Mockito não pegam esse tipo de bug — ele só aparece em runtime real, com banco e sessão Hibernate.
- **Nunca versionar `application.properties` com segredos.** O grupo manteve o arquivo no `.gitignore` desde o início e adicionou um `application.properties.example` como template, evitando expor o `jwt.secret` no repositório público.
- **Mensagens de commit descritivas economizam tempo na hora de defender o trabalho.** Commits no padrão `feat:`, `fix:`, `chore:`, `ci:` facilitam a leitura do histórico e a rastreabilidade entre PRs e features.

---

## Refactoring

Ao longo do projeto, vários refactorings foram aplicados com evidência direta no histórico de commits e Pull Requests.

### 1. Centralização do tratamento global de erros (PR #36)

Após análise da arquitetura, foi identificada a ausência de um tratamento global de exceções. Foi criada uma classe `GlobalExceptionHandler` com `@ControllerAdvice`, centralizando o mapeamento de exceções de domínio (como `NotFoundException`) para respostas HTTP apropriadas.

**Motivação:** evitar `try/catch` repetidos em cada controller e padronizar o formato de erro retornado pela API.

### 2. Reconstrução do `DashboardResponse` ausente (PR #38)

O PR do dashboard foi mergeado sem o DTO `DashboardResponse`, quebrando a compilação da `main`. O arquivo foi reconstruído a partir do uso no `DashboardService`, identificando todos os campos necessários e a classe aninhada `MaquinaRankingItem`.

**Motivação:** restaurar a compilação do projeto e garantir que o pipeline conseguisse passar.

### 3. Padronização de `@WebMvcTest` excluindo `JwtAuthFilter` (PR #37)

Vários testes de controller estavam falhando porque tentavam carregar o `JwtAuthFilter` no contexto reduzido do `@WebMvcTest`. A correção foi aplicada padronizando todos os testes com `excludeFilters = @ComponentScan.Filter(type = ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)`.

**Motivação:** isolar o teste da camada de segurança, que tem suas próprias classes de teste dedicadas (`JwtAuthFilterTest`, `JwtUtilTest`).

### 4. Adição de `@Transactional` em serviços com relações lazy (PR #45)

O `FalhaMaquinaService` retornava DTOs montados a partir de entidades com `@ManyToOne FetchType.LAZY`, causando `LazyInitializationException` em runtime. Foi anotado com `@Transactional`, mantendo a sessão Hibernate aberta durante a montagem do response.

**Motivação:** corrigir bug reportado pela desenvolvedora frontend e garantir que o padrão fosse seguido em futuros services com relações lazy.

### 5. Validação de integridade na exclusão de máquinas e produtos (PRs #56 e #57)

As exclusões simples de máquinas e produtos foram refatoradas para validar vínculos antes de deletar: máquina não pode ser excluída se tem falhas ou ordens vinculadas; produto não pode ser excluído se tem ordens vinculadas.

**Motivação:** evitar erros de integridade referencial no banco e preservar o histórico operacional do sistema.

### 6. Regra de cancelamento de ordens de produção (PR #55)

Foi adicionada uma transição de estado nova (`CANCELADA`) com validação rigorosa: apenas ordens em `PENDENTE` podem ser canceladas. Ordens em `EM_PRODUCAO`, `FINALIZADA` ou já `CANCELADA` retornam erro.

**Motivação:** dar ao gestor a possibilidade de descartar ordens criadas por engano sem comprometer o histórico das ordens efetivamente executadas.

### 7. Configuração de `application.properties.example` (PR #38)

O `application.properties` original era versionado contendo o `jwt.secret` exposto. Foi movido para o `.gitignore` e substituído por um template `application.properties.example` com placeholder no lugar do segredo.

**Motivação:** remover segredo do repositório público e estabelecer um padrão claro de configuração local para novos contribuidores.

### 8. Remoção de duplicações e anotações Lombok redundantes (PR #45)

Após análise de qualidade do código (apoiada por IA), foram identificadas duplicações em construtores, anotações Lombok redundantes (como `@Getter`/`@Setter` em classes já anotadas com `@Data`) e métodos não utilizados. Os ajustes foram aplicados gradualmente, com validação manual para evitar alterações em componentes já estabilizados.

**Motivação:** melhorar legibilidade e manutenção do código, sem introduzir mudanças desnecessárias em código que já estava funcionando.

---

## Uso de IA

O uso de IA pelo grupo foi documentado de forma transparente abaixo, contendo para cada integrante: modelos utilizados, finalidades, exemplos reais de prompts com indicação do que foi aceito/ajustado/descartado, dinâmica de uso e o que não foi feito por IA.

### Modelos utilizados pelo grupo

- **Claude (Anthropic)** — usado por Ana Júlia e Pettrius
- **ChatGPT Plus (OpenAI)** — usado por Vinícius
- **GitHub Copilot** — usado por Vinícius

### Dinâmica geral de uso

A IA foi utilizada **individualmente** por cada integrante, sem pair programming ou geração de código do zero. O foco foi acelerar tarefas pontuais (geração de boilerplate, refatoração, identificação de bugs) e dar apoio técnico quando algum integrante encontrava um obstáculo. Toda saída da IA foi revisada, adaptada ao padrão do projeto e testada antes de ser mergeada.

---

### Ana Júlia

**Modelo utilizado:** Claude (Anthropic).

**Finalidades:**

- Refatoração de código existente (extração de métodos, anotações `@Transactional`)
- Correção e geração de testes (JUnit 5 + Mockito, camadas service/controller/repository)
- Debugging de erros específicos (`LazyInitializationException`, configuração de `@WebMvcTest`, H2 em testes)
- Configuração de CI/CD (Jenkinsfile, Dockerfile, docker-compose)
- Geração de documentação (user stories com critérios Given/When/Then)

**Exemplos reais de prompts:**

**Prompt 1 — Refatoração de serviço**

> "Refatore o FalhaMaquinaService extraindo métodos privados e adicionando @Transactional por método, com readOnly = true nos métodos de leitura. Mantenha o comportamento atual."

→ Resposta aceita, aplicada diretamente após revisão.

**Prompt 2 — Correção de teste de controller**

> "Meu @WebMvcTest está falhando porque o CompanyPrincipal não é reconhecido no contexto de segurança. Aqui está minha configuração de auth [colei o código]. Como corrigir sem quebrar os outros testes?"

→ Resposta ajustada — a solução precisou ser adaptada para o modelo de autenticação já implementado no projeto.

**Prompt 3 — Geração de user stories**

> "Com base nessas classes de teste [colei os nomes], gere user stories no formato Given/When/Then para as funcionalidades de falha de máquina, ordens de produção e dashboard. Critérios curtos e diretos, sem texto de preenchimento."

→ Resposta ajustada — primeira versão ficou verbosa; foi solicitada reescrita com critérios mais concisos.

**Prompt 4 — Debug de pipeline**

> "Os stages de teste no Jenkins estão tentando conectar ao PostgreSQL em vez de usar H2. Aqui está meu Jenkinsfile [colei]. O que está faltando?"

→ Resposta aceita — identificou a ausência de `-Dspring.profiles.active=test` nos comandos Maven.

---

### João Vítor

**Modelo utilizado:** Claude (Anthropic)

**Finalidades:**

- Análise da arquitetura do projeto
- Priorização de issues do backlog
- Refatoração e identificação de débitos técnicos

**Exemplos reais de prompts:**

**Prompt 1 — Análise geral da arquitetura**

> "https://github.com/C14-INATEL/javal-backend.git quero que analise esse código e me diga quais melhorias podem ser feitas"

→ Resposta aceita com ajustes — a IA identificou corretamente a arquitetura em camadas (Controller → Service → Repository → Entity), destacando o uso adequado de Spring Boot, JPA, DTOs e BCrypt para criptografia de senhas. Também apontou oportunidades de melhoria relacionadas a tratamento de exceções, autenticação, uso de DTOs em todos os módulos e padronização da API. As observações foram revisadas e priorizadas pela equipe antes de serem consideradas para implementação.

**Prompt 2 — Priorização das issues do projeto**

> "Com base na lista de issues do repositório, qual delas deveria ser implementada primeiro?"

→ Resposta aceita — a IA sugeriu iniciar pelo tratamento global de erros utilizando `@ControllerAdvice`, seguido pela configuração do ambiente e implementação de autenticação. Também forneceu orientação de como deveria ser implementado.

**Prompt 3 — Análise de qualidade e refatoração do código**

> "Analise este repositório e identifique o que podem ser refatorado para melhorar manutenção, legibilidade e aderência às boas práticas."

→ Resposta aceita parcialmente — a IA identificou duplicações de código, uso redundante de anotações Lombok, métodos não utilizados e oportunidades de centralização de regras compartilhadas. Algumas sugestões foram consideradas relevantes, enquanto outras exigiram validação manual para evitar alterações desnecessárias em componentes já estabilizados.

---

### Letícia

> Prompts disponíveis no repositório do Frontend (https://github.com/C14-INATEL/javal-frontend).

---

### Pettrius

**Modelo utilizado:** Claude (Anthropic)

**Finalidades:**

- Documentação do projeto (README, mensagens de PR, comunicação com o grupo)
- Geração de testes unitários para a feature de registro de falhas
- Geração de código para a feature de registro e rastreamento de falhas de máquina

**Exemplos reais de prompts:**

**Prompt 1 — Documentação (README do repositório)**

> "Leia agora a branch principal (main) do repositório. Primeiro de tudo, crie um readme.md para esse repositório completo. O README.md deve conter instalação, execução, uso e funcionalidades."

→ Resposta aceita com ajustes — o Claude leu a `main` real do repositório (não inventou conteúdo), extraiu os endpoints reais de cada controller, os campos dos DTOs de request e a configuração do `docker-compose.yml`. Gerou um README com seções de tecnologias, pré-requisitos, instalação, execução em dois modos (Docker completo e banco no Docker + app local), testes, autenticação JWT, uso da API (tabelas de endpoints por domínio com exemplos JSON), funcionalidades e estrutura do projeto.

**Prompt 2 — Geração de testes unitários**

> "Isso que eu quero perguntar, não é necessário gerar os testes? Gere tudo que for necessário para funcionar perfeitamente."

→ Resposta aceita — após gerar a feature de registro e rastreamento de falhas (ver Prompt 3), o Claude foi solicitado a complementar com testes unitários. Gerou dois arquivos: `FalhaMaquinaServiceTest.java` (7 testes com Mockito cobrindo as regras de negócio — registrar coloca máquina em MANUTENCAO, resolver última falha volta pra ATIVA, resolver com outra aberta mantém em MANUTENCAO, casos de erro com `NotFoundException`) e `FalhaMaquinaControllerTest.java` (6 testes com `@WebMvcTest` cobrindo os endpoints REST e validações). Os testes seguiram o estilo dos testes existentes no projeto e já incluíram a exclusão correta do `JwtAuthFilter` no `@WebMvcTest`.

**Prompt 3 — Geração de código (feature)**

> "Preciso fazer o registro e rastreamento de falhas de uma máquina. E estou em dúvida como fazer isso. Por isso gostaria que você me respondesse tendo a leitura do projeto em todas as branchs."

→ Resposta aceita com ajustes — o Claude leu todas as branches do repositório antes de gerar qualquer código, confirmando que a feature ainda não havia sido implementada e identificando os padrões do projeto (autenticação via `CompanyPrincipal`, repositórios com `findByIdAndCompanyId` para isolamento por empresa, DTOs request/response, exceções via `NotFoundException`). Antes de gerar código, o Claude perguntou duas decisões de negócio (se registrar falha deveria mudar o status da máquina automaticamente e qual nível de detalhe a falha precisaria guardar). Após as respostas, gerou 8 arquivos seguindo os padrões do projeto: entidade `FalhaMaquina` com relação `@ManyToOne` para `Maquina` e `Company`, enums `SeveridadeFalha` e `StatusFalha`, repository, DTOs, service com a regra de negócio do ciclo (registrar → MANUTENCAO; resolver última falha → ATIVA) e controller.

---

### Vinícius

**Modelos utilizados:**

- GitHub Copilot (análise extensiva do workspace)
- ChatGPT Plus (OpenAI) — debugging, regras de negócio e integração

**Finalidades:**

- Análise completa do projeto para onboarding e levantamento de suíte de testes (Copilot)
- Debugging de erros HTTP, CORS e autenticação JWT (ChatGPT)
- Implementação de regras de negócio (cancelamento de ordens, validação de exclusão de máquinas e produtos)
- Criação de testes unitários e validação de integração frontend + backend

**Exemplos reais de prompts:**

**Prompt 1 — Análise completa do projeto (Copilot)**

> "Quero que você analise TODO o projeto aberto no workspace e atue como um desenvolvedor sênior responsável por fazer o onboarding de um novo integrante da equipe. Sua tarefa é criar uma documentação completa explicando o contexto geral do sistema. Analise todos os pacotes, classes, configurações, entidades, serviços, controladores, repositórios, banco de dados, integrações e arquivos relevantes."

O prompt completo solicitava análise em 17 tópicos detalhados (objetivo do sistema, arquitetura, estrutura dos pacotes, modelo de domínio, fluxo de negócio, fluxo de requisição, endpoints, segurança, banco de dados, tecnologias, regras de negócio, integrações, funcionalidades recentes, classes mais importantes, ordem de estudo recomendada, pontos críticos e diagramas), com a exigência explícita de basear toda a análise nos arquivos reais do projeto, sem suposições.

→ O relatório gerado serviu como referência interna para que o integrante compreendesse o sistema como um todo, especialmente em áreas que ele não havia codificado diretamente.

**Prompt 2 — Levantamento completo da suíte de testes (Copilot)**

> "Analise TODO o workspace do projeto e faça um levantamento completo da suíte de testes existente. Quero que você examine todos os arquivos de teste, incluindo testes unitários, testes de integração, testes de segurança, testes de controller, service, repository e quaisquer outros tipos presentes no projeto."

O prompt completo solicitava 11 tópicos: visão geral da estratégia, inventário completo, cobertura funcional, cobertura por camada, testes de autenticação/autorização, testes das regras de negócio, casos de erro, lacunas da suíte, qualidade dos testes, resumo executivo e recomendações dos 10 testes mais importantes a adicionar.

→ Identificou lacunas na cobertura que serviram de base para a priorização de novos testes pela equipe.

**Prompt 3 — Diagnóstico de erros HTTP (ChatGPT)**

> "Por que estou recebendo HTTP 403 ao acessar determinado endpoint?"

→ Identificou problemas relacionados à autenticação, autorização, CORS e configuração do Spring Security.

**Prompt 4 — Correção de CORS (ChatGPT)**

> "Como corrigir requisições PATCH bloqueadas por CORS no frontend?"

→ Implementação da correção que originou a branch `fix/cors-patch-method`.

**Prompt 5 — Cancelamento de ordens de produção (ChatGPT)**

> "Como implementar uma regra de negócio para cancelar ordens de produção apenas quando estiverem pendentes?"

→ Regra implementada: PENDENTE pode cancelar; EM_PRODUCAO, FINALIZADA e CANCELADA não podem.

**Prompt 6 — Validação de exclusão de máquinas (ChatGPT)**

> "Como impedir a exclusão de máquinas que possuem falhas ou ordens de produção vinculadas?"

→ Regra implementada: máquina sem vínculos pode excluir; máquina com falhas ou ordens vinculadas não pode.

**Prompt 7 — Validação de exclusão de produtos (ChatGPT)**

> "Como impedir a exclusão de produtos que já possuem ordens de produção vinculadas?"

→ Regra implementada: produto sem ordens pode excluir; produto com ordens vinculadas não pode.

**Prompt 8 — Criação de testes unitários (ChatGPT)**

> "Como criar testes unitários utilizando JUnit e Mockito para validar as novas regras de negócio?"

→ Testes para cancelamento de ordens, exclusão de máquinas e exclusão de produtos. Todos executados com sucesso.

**Prompt 9 — Validação completa de integração (ChatGPT)**

> "Como executar um teste completo de integração entre frontend, backend e banco de dados?"

→ Fluxos testados: cadastro de empresa, login, cadastro de máquinas, cadastro de produtos, criação de ordens, registro e resolução de falhas, cancelamento de ordens, exclusão de máquinas e produtos, dashboard. Todos validados com sucesso.

---

### O que não foi feito por IA

Os itens a seguir representam decisões e trabalho realizados integralmente pelo grupo, sem auxílio de ferramentas de IA:

- Definição do tema e escopo do projeto
- Escolha das tecnologias utilizadas (Java, Spring Boot, PostgreSQL, JWT, Jenkins)
- Definição da metodologia de desenvolvimento adotada pelo grupo
- Definição da estrutura de pastas e arquitetura em camadas
- Decisão de quais testes eram relevantes ao domínio do projeto
- Modelagem do domínio e relacionamentos JPA entre as entidades
- Configuração inicial do Spring Security e estrutura de autenticação JWT
- Configuração da infraestrutura do servidor Jenkins
- Discussões e decisões do grupo via WhatsApp e reuniões informais
- Revisões de Pull Request e aprovações de merge
- Execução manual de todos os comandos de Git, Maven, Docker e testes