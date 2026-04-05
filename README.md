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

### 🌐 Passo 4: Conexão Remota (AWS / Apresentação)

Se precisar conectar em um banco aws:
1. Abra o arquivo `src/main/resources/application-aws.properties`.
2. Troque o IP, usuário e senha pelos dados que o professor passar.
3. No terminal, rode o comando:
   ```bash
   mvn spring-boot:run -Dspring.profiles.active=aws
   ```

---

## Relatórios, Consultas e Funcionalidades

Foram implementados endpoints completos (REST) baseados na construção limpa de `DTOs` e métodos customizados com `JPQL` no Spring Data.  Alguns destaques:

*   **Filtros Dinâmicos**: A `MissaoRepository` combina status, nível de perigo e, quando informados os dois limites, um **intervalo de datas** por sobreposição com `dataInicio` / `dataTermino` (ou `createdAt` quando ausentes).
*   **Rankeamento MVP**: A `ParticipacaoRepository` agrupa e conta todas as participações de aventureiros num recorte de tempo, gerando um ranking consolidado com a soma das *recompensas de ouro*.
*   **Perfil Completo**: É injetado num DTO único todos os dados do Aventureiro, mais dados da tabela de Participações (como *"Total de Histórico"* e o *"Título da Última Missão"*).
*   **Busca Parcial**: Endpoint de busca paginável buscando aventureiros ignorando case sensitivity (`LIKE %nome%`).
*   **Missões (REST)**: `GET /missoes` (filtros, intervalo de datas por sobreposição com `dataInicio`/`dataTermino` ou `createdAt`), `GET /missoes/{id}` (detalhe + participantes), `POST /missoes/{id}/participacoes` (regras de negócio no serviço).
*   **Regras Parte 2**: `ParticipacaoService` valida mesma organização, aventureiro ativo, status da missão (`PLANEJADA` ou `EM_ANDAMENTO`) e unicidade do par; cadastro de aventureiro exige usuário responsável da mesma organização (`AventureiroController` + `RegraNegocioException` → HTTP 400).

---

## Requisitos do enunciado × onde foi atendido

Referência rápida para o PDF e para a banca. Itens só de entrega (PDF público, capa, prints de SQL) continuam por sua conta.

### Parte 1 — Legado `audit`

| Requisito | Como / onde | Por quê? |
|-----------|-------------|----------|
| Entidades JPA do schema `audit` | `audit.domain` — `Organizacao`, `Usuario`, `Role`, `Permission`, `ApiKey`, `AuditEntry` | O enunciado exige mapear o legado sem alterar o DDL; o código precisa refletir tabelas reais. |
| `@Table(schema = "audit")` e relacionamentos N:N | `Usuario`, `Role` — `@JoinTable` (nomes conforme o DDL da imagem Docker) | O banco usa schema `audit` e tabelas de junção; o JPA precisa apontar para os nomes corretos. |
| Repositórios | `audit.repository` — `OrganizacaoRepository`, `UsuarioRepository`, `RoleRepository` (`findAllWithPermissions`) | Persistência e consultas ficam isoladas e testáveis; `JOIN FETCH` evita N+1 nas permissions. |
| Datasource configurável | `application.properties` — `DB_URL`, `DB_USER`, `DB_PASSWORD` | Permite Docker local, AWS na banca e CI sem mudar código. |
| `ddl-auto` adequado à entrega | `validate` em `application.properties` (testes usam `update` via `@TestPropertySource`) | Entrega final não pode usar `create`; em teste, `update` ajuda a evoluir o schema `aventura` sem conflitar com o legado. |
| Testes: usuários + roles, roles + permissions, persistir usuário | `AuditJpaTest` (`@SpringBootTest`, equivalente ao pedido) | Prova que o mapeamento bate com o PostgreSQL real, como pede a Parte 1. |

### Parte 2 — Domínio `aventura`

| Requisito | Como / onde | Por quê? |
|-----------|-------------|----------|
| Entidades no schema `aventura` | `aventura.domain` — `Aventureiro`, `Companheiro`, `Missao`, `Participacao` + `ParticipacaoId` | Separa o domínio novo do legado `audit`, como o enunciado define. |
| 1:1 aventureiro ↔ companheiro, cascade/orphanRemoval | `Aventureiro.companheiro`, `Companheiro.aventureiro` | Regra: companheiro não existe sozinho e some com o aventureiro. |
| Unicidade (missão, aventureiro) | Chave composta `ParticipacaoId`; `ParticipacaoService` impede duplicata (`existsById`) | Garante o par único no banco e mensagem clara antes de violar PK. |
| Aventureiro inativo não entra em missão nova | `ParticipacaoService.registrar` | Regra explícita do enunciado; validação na camada de serviço, não só no banco. |
| Missão em estado compatível para novos participantes | `ParticipacaoService` — só `PLANEJADA` e `EM_ANDAMENTO` | Evita incluir gente em missão concluída ou cancelada. |
| Sem cruzamento entre organizações na participação | `ParticipacaoService` — compara `organizacao` de missão e aventureiro | Restrição organizacional obrigatória no texto do trabalho. |
| Usuário responsável na mesma organização do cadastro | `AventureiroController.registrar` | Evita cadastrar aventureiro “na guilda A” com usuário que pertence só à guilda B. |

### Parte 3 — Consultas, API e relatórios

| Requisito | Como / onde | Por quê? |
|-----------|-------------|----------|
| Listagem de aventureiros: filtros, ordenação, paginação | `GET /aventureiros` — `AventureiroRepository.findByFiltros`; `sortBy`/`sortDir` (`nome` ou `nivel`) | Atende listagem operacional com volume grande (filtros + página + ordem segura). |
| Busca por nome parcial, paginável, ordenável | `GET /aventureiros/busca` — `sortBy`/`sortDir` (`nome` ou `nivel`) | O enunciado pede correspondência parcial e ordenação; não só filtro fixo por nome. |
| Perfil completo do aventureiro | `GET /aventureiros/{id}` — companheiro, `countByAventureiro_Id`, última missão (`findUltimaMissao` com `JOIN FETCH`) | Uma tela “ficha completa” sem várias idas ao banco desnecessárias. |
| Listagem de missões: filtros + intervalo de datas | `GET /missoes` — `findByFiltrosSemPeriodo` / `findByFiltrosComPeriodo` | Duas queries evitam `param IS NULL` no PostgreSQL sem tipo; com as duas datas, aplica sobreposição de janela. |
| Detalhe da missão + participantes | `GET /missoes/{id}` — `ParticipacaoRepository.findByMissaoId` | Consulta operacional pedida: missão com papéis, recompensas e MVP na lista. |
| Incluir participante na missão (com regras) | `POST /missoes/{id}/participacoes` — `ParticipacaoService` + `ParticipacaoRequest` | Centraliza regras da Parte 2 no serviço e expõe uso real via API. |
| Ranking por período | `ParticipacaoRepository.findRankingParticipacao` — teste em `AventuraIntegracaoTest` | Relatório gerencial com agregação e filtro temporal. |
| Relatório de missões com métricas | `ParticipacaoRepository.findRelatorioMissoes` — teste em `AventuraIntegracaoTest` | Totais por missão conforme critério de aceite da Parte 3. |
| Teste por busca / fluxo principal | `AventuraIntegracaoTest` (intervalo de datas, ordenação na busca, rejeições de negócio) | Critério de aceite: uma verificação por tipo de consulta/regra relevante. |

### Entrega (fora do repositório)

| Requisito | Observação | Por quê? |
|-----------|------------|----------|
| Repositório público + README | Este arquivo; garantir remoto público antes da entrega | Critério explícito da banca: sem repo público não há correção. |
| PDF `nome_sobrenome_DR1_TP2.PDF` | Estrutura e prints de SQL com `logging.level.org.hibernate.SQL=DEBUG` (já em `application.properties`) | O enunciado exige PDF na ordem definida e evidência das queries do ORM. |

