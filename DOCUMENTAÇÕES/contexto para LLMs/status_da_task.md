# Status da Task: MyStreaming

## O que foi feito (Done) ✅
- **Estrutura Base:** Projeto Maven iniciado com Spring Boot (`MyStreamingApplication`).
- **Modelagem de Dados:** Entidades definidas para `User`, `Video`, `EncodingJob`, `Favorite`, etc.
- **Camada de Persistência:** Repositórios JPA configurados e migrations Flyway inicializadas.
- **Segurança JWT:** Implementação completa com filtro, utilitário e serviço de detalhes do usuário.
- **Serviços Core:** Lógica para streaming (`StreamingService`), encoding (`EncodingService`) e manipulação de arquivos.
- **API REST:** Controllers para autenticação, biblioteca, vídeos e status de jobs.

## O que falta fazer (To Do) ⏳
- [ ] **Testes de Integração:** Expandir a cobertura além dos testes unitários de repositório.
- [ ] **Refinamento de DTOs:** Padronizar as respostas da API para esconder campos sensíveis e simplificar o consumo externo.
- [ ] **Tratamento de Erros Global:** Centralizar exceções em um `GlobalExceptionHandler` via `@ControllerAdvice`.
- [ ] **Documentação Swagger/OpenAPI:** Expor os endpoints automaticamente para frontends ou clientes externos.

---
*Documentação atualizada em 6/23/2026.*