# Projeto PFC Central do Corte
Alunos:
Bruna de Medeiros Santos | RGM: 11222101313
Guilherme de Oliveira Matos | RGM: 11222101717

## Sobre o Projeto

O **PFC Central do Corte** é um sistema web desenvolvido como Projeto de Finalização de Curso, com foco na criação de uma plataforma para gerenciamento de estabelecimentos de estética.
A aplicação permite que usuários encontrem **barbearias e salões de beleza com base na localização**, visualizem serviços disponíveis e realizem agendamentos online.
Além disso, oferece ferramentas para que os estabelecimentos possam gerenciar seus serviços, clientes e horários de forma eficiente.

---
## Funcionalidades Previstas

- Cadastro e autenticação
- Busca por barbearias/salões
- Visualização de serviços
- Agendamento de horários
- Cadastro de estabelecimento
- Gerenciamento de serviços
- Controle de agenda
- Gestão de clientes

---

## Funcionalidades Implementadas

### Autenticação
- Cadastro de usuários como **Cliente** (`/auth/register`)
- Cadastro de usuários como **Proprietário de Barbearia** (`/auth/register/barbearia`)
- Login com retorno de token JWT (expiração de 2 horas)
- Renovação de token (refresh token)
- Senhas criptografadas com BCrypt

### Usuários
- Listagem de usuários ativos (ADMIN e BARBEARIA_ADM)
- Busca por ID, Role e Nome
- Atualização de perfil (próprio usuário ou admin)
- Alteração de senha com confirmação da senha atual (próprio usuário)
- Soft Delete (desativação lógica)
- Ativação/Desativação por Administradores

### Barbearias
- Cadastro de barbearia vinculado ao usuário autenticado (apenas Barbearia_ADM)
- Listagem pública de barbearias ativas
- Busca por ID, Nome e Localização (Cidade/UF)
- Listagem das barbearias do próprio usuário (`/my-barbearias`)
- Atualização e desativação com validação de propriedade
- Campos de auditoria (criado_em, atualizado_em)

### Administração
- Criação automática do primeiro administrador (CommandLineRunner)
- Endpoints protegidos para criação de usuários com qualquer role

## Tecnologias Utilizadas

### Back-end
- Java 24
- Spring Boot 3.1.0
- Spring Security (RBAC)
- JWT (JSON Web Token)
- Bean Validation
- Lombok

### Banco de Dados
- PostgreSQL
- Flyway (versionamento)

### Documentação
- OpenAPI 3 / Swagger

### Ferramentas
- Maven
- Git & GitHub
- Postman

---

## Arquitetura

O projeto segue o padrão de arquitetura em camadas:

src/
├── config (configurações)
├── controllers
├── services
├── domains (entidades e repositories)
├── dtos
├── exceptions (tratamento de exceções)
└── infra (filtros de segurança)

---

## Segurança

- Autenticação via JWT com expiração de 2 horas
- Autorização baseada em roles (RBAC) com 4 perfis:
    - `ROLE_ADMIN` - Acesso total
    - `ROLE_BARBEARIA_ADM` - Gerencia sua(s) barbearia(s)
    - `ROLE_FUNCIONARIO` - Acesso à barbearia vinculada
    - `ROLE_CLIENTE` - Acesso ao próprio perfil
- Rotas protegidas com Spring Security + `@PreAuthorize`
- Criptografia via BCrypt

---

## Como Executar

### Execução Local

#### Pré-requisitos
- Java 24+
- Maven
- PostgreSQL

#### Passos

# 1. Configurar banco de dados em application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha

# 2. Executar
mvn spring-boot:run

### Execução com Docker

#### Pré-requisitos
- Docker
- Docker Compose

# 2. Subir os contêineres
docker compose up

# Acessar Documentação
http://localhost:8080/swagger-ui/index.html

# Credenciais Padrão
Email: admin@sistema.com

Senha: admin123