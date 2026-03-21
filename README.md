# 🏭 Sistema de Gestão de Produção Industrial — Backend

API REST para gerenciamento e monitoramento de linhas de produção industrial, desenvolvida com Java + Spring Boot.

> 💡 Este repositório contém apenas o **backend** do projeto. O frontend desenvolvido em React.js está disponível em [C14-INATEL/javal-frontend](https://github.com/C14-INATEL/javal-frontend).

---

## 📋 Sobre o Projeto

O sistema permite o controle completo de uma linha de produção industrial, oferecendo:

- **Cadastro e gerenciamento de máquinas**
- **Controle de ordens de produção**
- **Monitoramento de status em tempo real**
- **Detecção de gargalos na produção**
- **Cálculo de eficiência operacional**
- **Registro e rastreamento de falhas**

---

## 🛠️ Stack Tecnológica

| Camada | Tecnologia |
|---|---|
| Backend | Java 17 + Spring Boot 3 |
| Banco de Dados | PostgreSQL |
| Testes | JUnit 5 + Mockito |
| Build | Maven |
| CI/CD | GitLab CI |
| Infraestrutura | Docker + Docker Compose |
| Frontend | React.js — [repositório separado](https://github.com/C14-INATEL/javal-frontend) |

---

## 📁 Estrutura do Projeto

```
Ainda a ser decidido pela equipe.
```

---

## ✅ Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- [Java 17+](https://adoptium.net/)
- [Maven 3.8+](https://maven.apache.org/)
- [Docker](https://www.docker.com/) e [Docker Compose](https://docs.docker.com/compose/)
- [Git](https://git-scm.com/)

---

## 🚀 Como Executar

### 1. Clone o repositório

```bash
git clone https://github.com/C14-INATEL/javal-backend.git
cd javal-backend
```

### 2. Suba o banco de dados com Docker

```bash
docker-compose up -d
```

> Isso iniciará uma instância do PostgreSQL na porta `5433`.

### 3. Configure o banco no Spring Boot

Edite o arquivo:
```bash
src/main/resources/application.properties
```
Adicione:
```bash
spring.datasource.url=jdbc:postgresql://localhost:5433/javal_db
spring.datasource.username=javal_user
spring.datasource.password=javal_pass
```

### 4. Compile e execute a aplicação

```bash
mvn clean install
mvn spring-boot:run
```

A API estará disponível em: `http://localhost:8080`

---

## 🧪 Executando os Testes

```bash
mvn test
```

Para gerar o relatório de cobertura:

```bash
mvn verify
```

---

## 🔁 CI/CD

O projeto utiliza **GitLab CI** para automação de build e testes. O pipeline é disparado automaticamente a cada push e executa:

1. Compilação do projeto
2. Execução dos testes
3. Build da imagem Docker (em merges para `main`)

---

## 🤝 Contribuindo

1. Crie uma branch a partir da `main`:
   ```bash
   git checkout -b sua-branch
   ```
2. Faça suas alterações e commite:
   ```bash
   git commit -m "descrição clara da alteração"
   ```
3. Envie para o repositório remoto:
   ```bash
   git push -u origin sua-branch
   ```
4. Abra um **Pull Request** para a branch `main`

---

## 👥 Equipe

Projeto desenvolvido pela turma **C14 - INATEL - Javal**.

---

## 📄 Licença

Este projeto está sob uso acadêmico — INATEL © 2025.