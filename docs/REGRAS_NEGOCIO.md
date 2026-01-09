# Regras de Negócio (Codechella)

## Ingressos
- **Listar todos (GET /ingressos):** retorna todos os ingressos como `IngressoDto`.
- **Obter por ID (GET /ingressos/{id}):** retorna ingresso; 404 se não existir.
- **Disponível por evento (SSE) (GET /ingressos/{id}/disponivel):**
  - Emite via SSE a lista de lotes do evento do ingresso.
  - Atualiza em tempo real quando há compra/criação de lote (`ingressoSink`).
- **Criar (POST /ingressos):** cria ingresso com `eventoId`, `tipo`, `valor`, `total`; retorna `IngressoDto`.
- **Comprar (POST /ingressos/compra):**
  - Requer autenticação JWT; extrai `userId` do token.
  - Busca ingresso por `ingressoId`; 404 se não existir.
  - Identifica lote ativo: primeiro com `total > 0` ordenado por `lote` (ASC).
  - Regra: só permite comprar se o `ingressoId` for o do lote ativo; caso contrário 400 "Lote não disponível. Compre o lote atual primeiro.".
  - Registra `Venda` com `userId` e decrementa `total` do ingresso pelo `dto.total`.
  - 400 se "Nenhum lote disponível para venda".
- **Excluir (DELETE /ingressos/{id}):** deleta ingresso se existir; não retorna corpo.
- **Alterar (PUT /ingressos/{id}):**
  - 404 se `id` não existir.
  - Atualiza `eventoId`, `tipo`, `valor`, `total` e persiste.
- **Criar Lote (POST /ingressos/lotes):**
  - Calcula próximo `lote`: `ultimoLote + 1`; se não houver, inicia em 1.
  - Cria novo ingresso (lote) com `eventoId`, `tipo`, `valor`, `total`, `lote`.
- **Listar Lotes por Evento (GET /ingressos/eventos/{eventoId}/lotes):**
  - Retorna ingressos do evento ordenados por `lote` (ASC).

## Eventos
- **Listar todos (GET /eventos):** retorna todos os eventos como `EventoDto`.
- **Obter por ID (GET /eventos/{id}):** retorna evento; 404 se não existir.
- **Obter por tipo (SSE) (GET /eventos/categoria/{tipo}):**
  - Converte `tipo` para `TipoEvento` (uppercase); retorna eventos do tipo.
  - Emite via SSE mesclando novos eventos do `eventoSink`; atraso de emissão por item: ~4s.
- **Traduzir descrição (GET /eventos/{id}/traduzir/{idioma}):**
  - Busca evento por `id` e chama `TraducaoDeTextos.obterTraducao(descricao, idioma)`.
- **Criar (POST /eventos):** cria evento; em caso de sucesso emite no `eventoSink`.
- **Excluir (DELETE /eventos/{id}):** deleta evento se existir; não retorna corpo.
- **Alterar (PUT /eventos/{id}):**
  - 404 se `id` não existir.
  - Atualiza `tipo`, `nome`, `data`, `descricao` e persiste.

## Regras Transversais
- **Erros padronizados:** 404 quando recurso não encontrado; 400 para regras violadas (e.g., compra em lote não ativo, lote indisponível).
- **Observabilidade:** métodos são observados com `@Observed`; spans aparecem no Zipkin aninhados ao HTTP.
- **SSE:** endpoints com stream usam `MediaType.TEXT_EVENT_STREAM_VALUE` e podem emitir atualizações periódicas/ao vivo.

## Usuários
- **Registrar (POST /auth/register):** cria usuário com `email` único e `senha` armazenada como hash BCrypt; role padrão `USER`; 400 se email já cadastrado.
- **Login (POST /auth/login):** valida credenciais e retorna `token` JWT; 401 se inválidas.
- **Token JWT:** assinado com HMAC usando secret configurado; subject = email; claim `role`; expiração configurável (padrão 1h).

## Segurança nos Endpoints
- **Públicos:** GET /eventos/**, GET /ingressos/** (exceto compra), POST /auth/**.
- **Protegidos:** POST /ingressos/compra requer Bearer token JWT válido no header Authorization.
