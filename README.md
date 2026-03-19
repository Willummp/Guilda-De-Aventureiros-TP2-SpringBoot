# Guilda de Aventureiros - TP2

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.3-brightgreen)
![Java](https://img.shields.io/badge/Java-17-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED)

Este projeto é a segunda parte (TP2) da disciplina de **Desenvolvimento de Aplicações Corporativas e Escaláveis**.  
O objetivo central foi migrar a aplicação de gerenciamento da guilda, antes armazenada apenas em memória, para uma estrutura robusta conectada a um banco de dados relacional (PostgreSQL) focado em **duas grandes frentes**: o mapeamento de um sistema de auditoria legado e a criação do novo domínio para missões e aventureiros.

## Arquitetura de Dados (Schemas)

A aplicação agora persiste seus dados em dois schemas independentes dentro do PostgreSQL:

### 1. `audit` (Sistema Legado e Intocável)
O banco de dados fornecido pelo professor no Docker já continha um schema de auditoria focado em controle de acesso (https://hub.docker.com/r/leogloriainfnet/postgres-tp2-spring):
- **Tabelas mapeadas**: `organizacoes`, `usuarios`, `roles`, `permissions`, `api_keys` e `audit_entries`.
- **Regra de Negócio**: Nenhuma alteração estrutural pôde ser feita neste schema (`ddl-auto=validate`).
- **Descoberta via SQL**: Identificamos a estrutura do banco através de consultas ao `information_schema.columns`, o que nos permitiu mapear corretamente nomes em `snake_case` para `camelCase` no Java.
- **Implementação Avançada**: 
    - **JSONB**: Mapeamento de colunas de auditoria (`diff`, `metadata`) usando `@JdbcTypeCode(SqlTypes.JSON)`.
    - **INET**: Suporte nativo ao tipo de endereço IP do PostgreSQL.
    - **N:N**: Relacionamentos bidirecionais `@ManyToMany` (ex: `user_roles`) sem criar entidades extras para as tabelas de junção, utilizando apenas a anotação `@JoinTable(schema = "audit", ...)`.

### 2. `aventura` (Novo Domínio da Guilda)
Este schema acomodou o desenvolvimento das novas lógicas de negócio:
- **`Aventureiro` e `Companheiro`**: Refatoradas para o banco de dados como entidades independentes, unidas por um relacionamento estrito de `1:1` (`@OneToOne` com deleção em cascata).
- **`Missao` e `Participacao`**: Uma missão pode ter vários aventureiros, e um aventureiro pode fazer várias missões. Modelamos isso no JPA criando uma **entidade associativa `Participacao`**, sustentada por uma chave primária composta (`@EmbeddedId`).
- **Enums**: Utilizados para padronizar Status das Missões, Nível de Perigo e o Papel do participante na missão (Líder, Suporte, etc).

---

## Como Executar o Projeto Localmente

Para que os testes e a API funcionem adequadamente, é estritamente necessário ter o banco de dados em execução. Toda a configuração da aplicação espera encontrar o PostgreSQL rodando na porta `5432`.

### Passo 1: Iniciar o Banco de Dados via Docker

Você usará a imagem fornecida pelo professor para garantir que o ambiente tenha as tabelas legacy configuradas:

```bash
docker run --name postgres-tp2 -p 5432:5432 -e POSTGRES_PASSWORD=postgres -d leogloriainfnet/postgres-tp2-spring:1.0
```
*(Se você já rodou o comando acima antes e o contêiner apenas está desligado, use: `docker start postgres-tp2`)*

### Passo 2: Rodar a API Spring Boot

No diretório raiz do projeto (`/GuildaAventureiros`), com o Maven instalado:

```bash
mvn spring-boot:run
```
O Tomcat inicializará a API em `http://localhost:8080`.

### Passo 3: Executar a Suite de Testes (Integração)

O projeto conta com testes end-to-end (`@SpringBootTest`) que verificam a consistência de cada query e mapeamento de banco elaborados.

```bash
mvn test
```
*(O relatório gerado pelo Maven do Surefire indicará `BUILD SUCCESS`).*

---

## Relatórios, Consultas e Funcionalidades

Foram implementados endpoints completos (REST) baseados na construção limpa de `DTOs` e métodos customizados com `JPQL` no Spring Data.  Alguns destaques:

*   **Filtros Dinâmicos**: A `MissaoRepository` encontra missões usando filtros combinados (Data de Conclusão, Status, etc). Graças à função `COALESCE`, anulamos erros nativos de inferência do PostgreSQL para campos não enviados.
*   **Rankeamento MVP**: A `ParticipacaoRepository` agrupa e conta todas as participações de aventureiros num recorte de tempo, gerando um ranking consolidado com a soma das *recompensas de ouro*.
*   **Perfil Completo**: É injetado num DTO único todos os dados do Aventureiro, mais dados da tabela de Participações (como *"Total de Histórico"* e o *"Título da Última Missão"*).
*   **Busca Parcial**: Endpoint de busca paginável buscando aventureiros ignorando case sensitivity (`LIKE %nome%`).

