# Projeto PFC Central do Corte - Backend

**Alunos:**
- Bruna de Medeiros Santos | RGM: 11222101313
- Guilherme de Oliveira Matos | RGM: 11222101717

---

## Sobre o Projeto

O **PFC Central do Corte** é um sistema web desenvolvido como Projeto de Finalização de Curso, com foco na criação de uma plataforma para gerenciamento de estabelecimentos de estética.

A aplicação permite que usuários encontrem **barbearias e salões de beleza com base na localização**, visualizem serviços disponíveis e realizem agendamentos online. Além disso, oferece ferramentas para que os estabelecimentos possam gerenciar seus serviços, clientes, funcionários e horários de forma eficiente.

---

## Funcionalidades Implementadas

### Autenticação e Segurança
- Cadastro de usuários como **Cliente** (`/auth/register`)
- Cadastro de usuários como **Proprietário de Barbearia** (`/auth/register/barbearia`)
- Login com retorno de token JWT (access + refresh token)
- Renovação de token (refresh token)
- Logout com registro de auditoria
- Senhas criptografadas com BCrypt
- RBAC com 4 perfis de acesso:
  - `ROLE_ADMIN` - Acesso total ao sistema
  - `ROLE_BARBEARIA_ADM` - Gerencia suas barbearias
  - `ROLE_FUNCIONARIO` - Visualiza e conclui agendamentos
  - `ROLE_CLIENTE` - Agenda serviços e gerencia perfil

### Usuários
- CRUD completo de usuários (ADMIN)
- Busca por ID, Role, Nome e Status
- Paginação e filtros
- Atualização de perfil (próprio usuário ou admin)
- Alteração de senha com validação da senha atual
- Ativação/Desativação de usuários
- Soft Delete com anonimização de dados

### Barbearias
- Cadastro de barbearia vinculado ao proprietário
- Listagem pública com paginação
- Busca por ID, Nome, Cidade/UF e CEP
- Busca avançada por CEP com fallback para cidade
- Listagem das barbearias do usuário logado
- Atualização e desativação com validação de propriedade
- Upload de imagem da barbearia (URL)

### Serviços
- CRUD completo de serviços por barbearia
- Ativação/Desativação de serviços
- Busca por barbearias com o serviços
- Campos: nome, descrição, preço, duração

###  Funcionários
- Criação de funcionários vinculados à barbearia
- Vinculação de funcionários existentes
- Desvinculação com transferência automática de agendamentos
- Controle de disponibilidade (férias/folgas)
- Listagem de funcionários por barbearia
- Funcionários podem visualizar e concluir seus agendamentos

### Agendamentos
- Criação de agendamentos por clientes
- Atribuição automática de funcionário (se não especificado)
- Validação de horário de funcionamento
- Validação de disponibilidade de funcionário
- Status: `PENDENTE`, `CONFIRMADO`, `CONCLUIDO`, `CANCELADO_PELO_CLIENTE`, `CANCELADO_PELA_BARBEARIA`
- Cancelamento com motivo (cliente e barbearia)
- Confirmação (proprietário)
- Conclusão (proprietário e funcionário)
- Listagens por cliente, barbearia e funcionário
- Agendamentos do dia

### Horários de Funcionamento
- Configuração de horários por dia da semana
- Possibilidade de marcar dia como fechado
- Validação de horário disponível para agendamento
- Horários padrão (09:00 às 18:00 de segunda a sábado)

### Catálogo de Produtos
- CRUD completo de produtos por barbearia
- Ativação/Desativação de produtos
- Campos: nome, descrição, preço, categoria, marca, imagem

### Dashboard e Métricas
- Cards com métricas principais
  - Total de Agendamentos
  - Faturamento do Mês
  - Faturamento do Ano
  - Clientes Atendidos
  - Taxa de Conclusão
  - Cancelamentos
- Gráfico de agendamentos concluídos por período (semana/mês/ano)
- Tabela de resumo de faturamento mensal (atualizada automaticamente via triggers)

### Logs do Sistema (Auditoria)
- Registro de todas as ações importantes do sistema
- Tipos de log: AGENDAMENTO, USUARIO, LOGIN, LOGOUT, FUNCIONARIO
- Captura de IP, User-Agent e detalhes da ação
- Filtros por tipo, ação, usuário e período
- Paginação
- Acesso exclusivo para ADMIN

### Integrações
- ViaCEP (busca automática de endereço por CEP)
- Envio de emails para recuperação de senha

---

## Tecnologias Utilizadas

### Back-end
| Tecnologia | Versão | Finalidade |
|------------|--------|------------|
| Java | 24 | Linguagem principal |
| Spring Boot | 3.1.0 | Framework principal |
| Spring Security | - | Autenticação e autorização |
| Spring Data JPA | - | Persistência de dados |
| Spring Mail | - | Envio de emails |
| JWT (JJWT) | - | Tokens de autenticação |
| Hibernate Validator | - | Validação de dados |
| PostgreSQL | - | Banco de dados |
| Flyway | - | Versionamento do banco |
| Lombok | - | Redução de boilerplate |
| OpenAPI 3 / Swagger | - | Documentação da API |

### DevOps & Ferramentas
- Maven
- Git & GitHub
- Docker / Docker Compose
- Postman (testes)

---

## Arquitetura

O projeto segue o padrão de arquitetura em camadas:

src/

├── config/ # Configurações (Security, CORS, Swagger)

├── controllers/ # Endpoints REST

├── services/ # Regras de negócio

├── domain/

│ ├── models/ # Entidades JPA

│ ├── repositories/# Interfaces de acesso a dados

│ └── enums/ # Enumeradores

├── dto/ # Data Transfer Objects

├── exception/ # Tratamento de exceções

├── infra/ # Infraestrutura (filtros JWT, security)

└── resources/

└── db/migration/# Scripts Flyway


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

# Acessar Documentação
http://localhost:8080/swagger-ui/index.html

# Credenciais Padrão
Email: admin@sistema.com

Senha: admin123
