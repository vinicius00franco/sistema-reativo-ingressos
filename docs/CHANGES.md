# Mudanças no Projeto Codechella

## Resumo das Alterações

Este documento registra as principais mudanças implementadas no projeto, incluindo correções de bugs, novas funcionalidades e atualizações de regras de negócio.

## Novas Funcionalidades

- **Observabilidade Completa**: Implementado sistema de tracing e logging detalhado para todas as rotas HTTP
  - Adicionadas dependências do Spring Boot Actuator, Micrometer Tracing e Zipkin
  - Criado filtro de observabilidade para capturar requests, responses, headers e bodies
  - Configurado logging estruturado com traceId e spanId
  - Adicionado suporte a métricas Prometheus e tracing distribuído

<!-- Removido: OpenTelemetry Java Agent (incompatível com versão atual do Java) -->
<!-- Todas as referências ao agente foram retiradas do projeto. Mantemos Micrometer Tracing + Zipkin. -->

- **Spans Detalhados de Banco de Dados**: Implementação de observabilidade granular
  - Adicionados @Observed em todos os métodos do IngressoService
  - Adicionados @Observed em todos os métodos do EventoService
  - Configuração do ObservedAspect para instrumentação automática
  - Habilitada observabilidade R2DBC para queries SQL
  - Spans específicos para operações: obterTodos, obterPorId, comprar, cadastrar, etc.
  - Visualização detalhada no Zipkin: tempo HTTP + tempo de cada método + tempo de queries SQL
  - Habilitada propagação automática de contexto do Reactor (Micrometer context-propagation)
  - Adicionado `ReactorContextConfig` para `Hooks.enableAutomaticContextPropagation()`

- **Documentação de Regras de Negócio**: Criado arquivo `docs/REGRAS_NEGOCIO.md` com regras de negócio das rotas de Eventos e Ingressos. Atualizar este arquivo a cada alteração de regras.

## Correções de Bugs

- **Erro 500 no endpoint /eventos**: Corrigido incompatibilidade de versão do H2 com Flyway. Downgrade do H2 para 2.1.214 e mudança para armazenamento baseado em arquivo.
- **Lotes não identificados no banco**: Criada migração V007 para atualizar valores corretos de lote no evento 11 (ids 12, 24, 46 para lotes 1, 2, 3).
- **Stream SSE fechando imediatamente**: Alterado `concatWith` para `mergeWith` no endpoint `/ingressos/{id}/disponivel` para manter o stream aberto para updates em tempo real.

## Novas Funcionalidades

- **Sistema de Lotes (Batches)**: Implementado para ingressos, permitindo compras sequenciais por lote.
  - Campo `lote` adicionado à entidade `Ingresso`.
  - Validação em `IngressoService.comprar()` para garantir compras apenas no lote ativo.
  - Método `criarLote()` para criar novos lotes automaticamente incrementados.
  - Método `obterLotesPorEvento()` para listar todos os lotes de um evento.

- **Atualizações em Tempo Real via SSE**: Modificado endpoint `/ingressos/{id}/disponivel` para exibir todos os lotes de um evento em vez de um ingresso único.

- **Novo Endpoint**: POST `/ingressos/lotes` para criação de lotes via `NovoLoteDto`.

- **Sistema de Autenticação JWT**: Implementado para proteger endpoints de compra.
  - Adicionados endpoints de autenticação: POST /auth/register e POST /auth/login.
  - Implementado JWT com segredo HMAC e expiração configurável.
  - Endpoint POST /ingressos/compra protegido com autenticação Bearer token; vendas agora associadas ao usuário autenticado.
  - Demais endpoints (GET para ver eventos e ingressos disponíveis) permanecem públicos.

## Arquivos Modificados

- **pom.xml**: Downgrade da versão do H2 e adição de dependências Spring Security e JJWT.
- **application.properties**: Configuração para H2 baseado em arquivo, logging e JWT (secret e expiration).
- **application.properties**: Configuração para H2 baseado em arquivo e logging.
- **Ingresso.java**: Adicionado campo `lote` com getters/setters.
- **IngressoDto.java**: Atualizado para incluir `lote` e mapeamentos.
- **IngressoRepository.java**: Adicionadas queries para ordenação por lote e busca do lote ativo.
- **IngressoService.java**: Modificado `comprar()` para extrair `userId` do contexto de segurança e associar à venda.
- **IngressoController.java**: Modificado `totalDisponivel()` para SSE de todos os lotes do evento.
- **Venda.java**: Adicionado campo `userId` para associar vendas a usuários.
 - **ObservabilityFilter.java**: Comentário atualizado para refletir Micrometer/Brave em vez de OpenTelemetry.
 - **run-with-otel.sh**: Script removido/descontinuado devido à incompatibilidade do OpenTelemetry Agent.
 - **http/ingressos.http**: Adicionados endpoints de criação de lote (`POST /ingressos/lotes`) e listagem de lotes por evento (`GET /ingressos/eventos/{eventoId}/lotes`).
 - **http/eventos.http**: Revisado; cobre todas as rotas (`GET`, `POST`, `PUT`, `DELETE`, tradução e categoria SSE).

- **Novos Arquivos para Autenticação**:
  - `User.java`: Entidade para usuários.
  - `UserRepository.java`: Repositório reativo para usuários.
  - `RegistroUsuarioDto.java`, `LoginRequest.java`, `TokenResponse.java`: DTOs para auth.
  - `JwtUtil.java`: Utilitário para geração e validação de JWT.
  - `JwtAuthenticationManager.java`: Gerenciador de autenticação reativo.
  - `JwtSecurityContextRepository.java`: Repositório de contexto de segurança.
  - `SecurityConfig.java`: Configuração do Spring Security para WebFlux.
  - `AuthService.java`: Serviço de autenticação.
  - `AuthController.java`: Controller para registro e login.
  - `V008__create_table_users.sql`: Migração para tabela users.
  - `V009__add_user_id_to_vendas.sql`: Migração para adicionar user_id às vendas.

## Migrações de Banco

- **V006__add_lote_column.sql**: Adicionada coluna `lote` à tabela `ingressos`.
- **V008__create_table_users.sql**: Criada tabela `users` com campos id, email, password_hash, role.
- **V009__add_user_id_to_vendas.sql**: Adicionada coluna `user_id` à tabela `vendas` para associar vendas a usuários.

## Regras de Negócio

- **Compras Sequenciais**: Ingressos só podem ser comprados no lote ativo (mais recente) de um evento.
- **Criação Automática de Lotes**: Novos lotes são criados com numeração incremental.
- **SSE para Eventos**: Streams em tempo real agora mostram disponibilidade de todos os tipos de ingresso de um evento.

## Testes Realizados

- Endpoint /eventos funcionando sem erros 500.
- SSE emitindo atualizações em tempo real para múltiplos lotes.
- Validação de compras em lotes não ativos retornando erro 400.
- Criação de lotes via POST funcionando com auto-incremento.