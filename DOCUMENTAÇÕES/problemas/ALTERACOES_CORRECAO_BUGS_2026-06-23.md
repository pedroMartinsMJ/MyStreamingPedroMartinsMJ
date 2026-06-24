# Alterações — Correção de Bugs Críticos (2026-06-23)

## Resumo

Correção de 7 problemas identificados na análise do projeto MyStreaming. Os bugs variam de críticos (OOM em streaming, @EnableAsync ausente) a moderados (JWT secret fraco, upload size excessivo).

---

## Problemas Corrigidos

### 1. 🔴 CRÍTICO — `@EnableAsync` ausente
**Arquivo:** `config/SecurityConfig.java` → **novo arquivo** `config/AsyncConfig.java`

**Problema:** O `EncodingService.processJobAsync()` usa `@Async`, mas sem `@EnableAsync` no contexto, os jobs rodavam synchronously no thread do caller. Encoding de vídeos longos bloquearia a API inteira.

**Solução:** Criado `AsyncConfig.java` com:
- `@EnableAsync` habilitando processamento assíncrono
- Thread pool dedicado (`encodingExecutor`) com 2 core threads, 4 max threads, fila de 10 jobs
- Rejeição explícita quando o pool está saturado (em vez de travar silenciosamente)

---

### 2. 🔴 CRÍTICO — OOM em `streamVideoDirectly()` (cast `(int)` + `Files.readAllBytes()`)
**Arquivo:** `service/StreamingService.java`, `controller/StreamingController.java`

**Problema A (Linha 146):** `byte[] data = new byte[(int) length;` — cast para `int` em tamanho de chunk. Para Range Requests >2GB, o cast trunca e causa dados corrompidos ou OOM.

**Problema B (Linha 132):** `Files.readAllBytes(file.toPath())` — sem Range Header, carrega o arquivo **inteiro na memória**. Vídeo de 50GB = OOM garantido.

**Solução:**
- Substituído `ResponseEntity<byte[]>` por `ResponseEntity<InputStreamResource>` em ambos os caminhos (com e sem Range)
- Sem Range: usa `FileInputStream` direto (streaming sem buffering na memória)
- Com Range: usa `RandomAccessFile` com seek, lendo em chunks de 64KB via InputStream customizado
- O Spring streama o InputStreamResource para a resposta HTTP automaticamente

---

### 3. 🟡 ALTO — `@Transactional` + `@Async` no mesmo método (Spring AOP proxy issue)
**Arquivo:** `service/EncodingService.java`

**Problema:** Quando `@Async` e `@Transactional` estão no mesmo método, o comportamento depende da ordem dos advices do Spring AOP. O transactional pode criar um proxy que bypassa o async, ou vice-versa.

**Solução:** Separado em dois métodos:
- `processJobAsync()` — apenas `@Async("encodingExecutor")`, delega para o método transacional
- `executeJobInTransaction()` — apenas `@Transactional`, executa a lógica real do job

---

### 4. 🟡 ALTO — FFmpeg path hardcoded como "ffmpeg"
**Arquivos:** `service/EncodingService.java`, `application.yaml`

**Problema:** `"ffmpeg"` hardcodado na linha 123 assume que o binário está no PATH do sistema. No Windows, FFmpeg não está no PATH por padrão — encoding nunca funcionaria.

**Solução:**
- Adicionado `@Value("${ffmpeg.path}")` injetando o path via configuração
- Path configurado no `application.yaml`:
  ```yaml
  ffmpeg:
    path: C:\Users\pedro\Desktop\projetos\MyStreaming\ffmpeg\ffmpeg-2026-06-15-git-44d082edc8-full_build\bin\ffmpeg.exe
  ```
- Validação antes de executar: se o arquivo não existe, lança `EncodingException` com mensagem clara

---

### 5. 🟢 MÉDIO — max-file-size: 100GB (risco de DoS)
**Arquivo:** `application.yaml`

**Problema:** Uploads de até 100GB permitidos. Um único upload malicioso pode esgotar disco/memória do servidor.

**Solução:** Reduzido para 5GB:
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5GB
      max-request-size: 5GB
```

---

### 6. 🟢 MÉDIO — Exception handlers globais ausentes
**Arquivo:** **novo arquivo** `exception/GlobalExceptionHandler.java`

**Problema:** Sem `@ControllerAdvice`, exceções como `VideoNotFoundException` retornavam HTTP 500 com stack trace completo — informação sensível exposta ao cliente.

**Solução:** Criado handler global cobrindo:
- **Domínio:** VideoNotFoundException (404), UserNotFoundException (404), EncodingException (500), StorageException (500)
- **Comuns:** IllegalArgumentException (400), IllegalStateException (400), MaxUploadSizeExceededException (413)
- **Fallback:** Exception genérica (500) — mensagem genérica sem detalhes internos

Cada handler retorna `ErrorResponseDTO` estruturado com: status, error code, message, path e timestamp.

---

### 7. 🟡 ALTO — JWT secret com default fraco
**Arquivo:** `security/JwtUtil.java`, `application.yaml`

**Problema:** Default `"mystreaming_dev_secret_key_change_in_production_please"` é previsível. Se alguém intercepta um token, pode gerar tokens válidos.

**Solução:**
- Adicionado suporte a variável de ambiente: `${JWT_SECRET:mystreaming_dev_secret_key_change_in_production_please}`
- Em produção, definir `JWT_SECRET` como variável de ambiente com valor forte e aleatório
- Warning no log se o default for detectado (ajuda em deploys acidentais)

---

## Arquivos Criados

| Arquivo | Descrição |
|---------|-----------|
| `config/AsyncConfig.java` | Configuração do thread pool async com `@EnableAsync` |
| `exception/GlobalExceptionHandler.java` | Handler global de exceções com respostas estruturadas |

## Arquivos Modificados

| Arquivo | Alterações |
|---------|------------|
| `service/EncodingService.java` | Separado @Async/@Transactional, FFmpeg path configurável, validação de existência |
| `service/StreamingService.java` | Streaming com InputStreamResource (sem OOM), chunks de 64KB |
| `controller/StreamingController.java` | Retorno alterado para `InputStreamResource` no endpoint `/direct` |
| `application.yaml` | FFmpeg path, JWT config, max-file-size reduzido para 5GB |
| `security/JwtUtil.java` | Validação de secret fraco com warning no log |

---

## Problemas NÃO Corrigidos (pendentes)

### Progress tracking durante encoding
O FFmpeg output é lido mas não parseado. Para implementar progress real, seria necessário:
1. Parsear `time=XX:XX:XX` do stderr do FFmpeg em tempo real
2. Calcular duration total com ffprobe antes de começar
3. Atualizar `progressPercentage` no EncodingJob periodicamente
4. Expor via WebSocket ou polling endpoint

### Ownership check no LibraryController
O endpoint `/api/library/user/{userId}` aceita qualquer userId autenticado. Para corrigir:
1. Injetar `Authentication` no controller
2. Extrair o userId do JWT token
3. Validar que o userId do path == userId do token

### WebSocket dependency bloat
`spring-boot-starter-websocket` está no pom.xml mas sem uso. Remover se não for usar futuramente (ex: notificações de encoding progress).PC 