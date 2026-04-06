# Projeto PFC Central do Corte
Alunos:
Bruna de Medeiros Santos | RGM: 11222101313
Guilherme de Oliveira Matos | RGM: 11222101717

## Sobre o Projeto

O **PFC Central do Corte** é um sistema web desenvolvido como Projeto de Finalização de Curso, com foco na criação de uma plataforma para gerenciamento de estabelecimentos de estética.
A aplicação permite que usuários encontrem **barbearias e salões de beleza com base na localização**, visualizem serviços disponíveis e realizem agendamentos online.
Além disso, oferece ferramentas para que os estabelecimentos possam gerenciar seus serviços, clientes e horários de forma eficiente.

---
## Funcionalidades

- Cadastro e autenticação
- Busca por barbearias/salões
- Visualização de serviços
- Agendamento de horários
- Cadastro de estabelecimento
- Gerenciamento de serviços
- Controle de agenda
- Gestão de clientes

---

## Tecnologias Utilizadas

### Back-end
- Java
- Spring Boot
- Spring Security
- JWT (JSON Web Token)

### Banco de Dados
- PostgreSQL

### Ferramentas
- Maven
- Git & GitHub
- Postman

---

## Arquitetura

O projeto segue o padrão de arquitetura em camadas:

src/
├── controller
├── service
├── repository
├── domain
└── infra (security, config)

---

## Segurança

- Autenticação via JWT
- Autorização baseada em roles
- Rotas protegidas com Spring Security
