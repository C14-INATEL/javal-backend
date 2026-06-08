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

A suíte cobre as camadas de entity, repository, service e controller de todos os domínios.

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
| DELETE | `/{id}` | Remove um produto |

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
| DELETE | `/{id}` | Remove uma máquina |

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
- **Gestão de produtos** — cadastro, listagem e remoção, com o tempo de produção unitário.
- **Gestão de máquinas** — cadastro, listagem, alteração de status e remoção, com capacidade produtiva por hora.
- **Ordens de produção** — criação vinculando produto e máquina, com ciclo de vida (pendente -> em produção -> finalizada).
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
        └── test/               # Testes (entity, repository, service, controller)
```

A aplicação segue uma arquitetura em camadas: o `controller` recebe a requisição e identifica a empresa pelo token, o `service` concentra as regras de negócio, e o `repository` faz o acesso a dados sempre filtrando pela empresa.

---

## CI/CD

O `Jenkinsfile` na raiz define o pipeline, com estágios de build e execução de testes. Os relatórios de teste são publicados a cada execução. (GitHub Actions não é utilizado neste projeto.)

---

## Uso de IA

> Seção a ser preenchida pela equipe antes da entrega final, conforme exigido pela disciplina: modelos utilizados, finalidades (geração de código, testes, documentação, debugging), exemplos reais de prompts, dinâmica de uso e quais partes foram feitas sem auxílio de IA.