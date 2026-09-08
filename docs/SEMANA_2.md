# Semana 2 — Identity: execução e contrato HTTP

## Escopo e decisões

- `POST /auth/cadastro` sempre cria CLIENTE.
- `POST /auth/cadastro-negocio` sempre cria ADMIN. Nesta semana cria somente a conta do dono; não há entidade de negócio adicional.
- Ambos recebem somente `nome`, `email` e `senha`. Enviar `role` ou qualquer campo desconhecido retorna 400.
- PROFISSIONAL está previsto no enum, sem cadastro público. Sua criação por ADMIN pertence à Semana 5.
- Domínio puro, persistência JPA separada e mapeamento MapStruct. UUID é o padrão para todas as entidades, inclusive Agendamento e referências futuras: UUID no Java/PostgreSQL e string no JSON/TypeScript.
- Pacotes: domain/model, domain/repository, domain/exception; application/usecase, application/dto, application/port e application/exception. Controllers permanecem em infrastructure/web.
- Email é normalizado com remoção de espaços externos e letras minúsculas; é único entre todos os perfis.
- Nome obrigatório, até 150 caracteres; email em formato ASCII usual, até 254 caracteres e parte local até 64.
- Senha obrigatória, preservada sem trim, limitada a 72 bytes UTF-8 pelo BCrypt. Não foi adicionada política de complexidade não definida no plano.
- JWT assinado, com emissor `agenda-plus`, subject igual ao ID do usuário e expiração padrão de 1 hora. A chave vem de `JWT_SECRET` e deve ter pelo menos 32 bytes; não há chave padrão em produção.
- A autenticação consulta o usuário persistido e usa sua role atual. Sessões, Basic e formulário padrão estão desativados; a API recebe token somente no header Bearer.
- `GET /identity/me` é a rota protegida usada para validar a autenticação.
- DTOs de entrada e token ocultam credenciais no `toString()` usado em logs DEBUG.

## Rodar testes

Com Java 21, Maven e Docker Desktop em execução, no diretório do backend:

```powershell
mvn test
```

Os testes de integração usam um PostgreSQL 16 Alpine descartável via Testcontainers, aplicam Flyway e validam o esquema com Hibernate. O banco de desenvolvimento não é usado. Os testes Spring ativam explicitamente o perfil `test`. `src/test/resources/application-test.yml` sobrescreve a configuração JWT principal com `${TEST_JWT_SECRET:chave-exclusiva-para-testes-sem-uso-em-producao-123456}`.

O passo Maven de `.github/workflows/ci.yml` define `TEST_JWT_SECRET` com uma chave pública exclusiva para testes. Assim, o CI não depende de `.env`, de `JWT_SECRET` nem de cadastrar um GitHub Secret. Localmente, sem `TEST_JWT_SECRET`, os testes usam o fallback acima. Essa chave não protege dados reais; é usada apenas com o banco descartável. A configuração está em recursos de teste e não integra o JAR de produção. Produção continua exigindo `JWT_SECRET` próprio, injetado pelo ambiente de deploy.

O workflow existente executa `./mvnw -B test` no GitHub Actions. A passagem local não comprova a execução remota: após revisar e publicar as alterações, conferir o job no GitHub.

## Rodar manualmente

No diretório do backend, com Docker disponível:

```powershell
docker compose up -d postgres
$env:JWT_SECRET = [Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(48))
mvn spring-boot:run
```

Se usar credenciais próprias no Compose, definir `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` correspondentes no terminal do backend. O Spring não carrega `.env` automaticamente. `.env.example` é apenas referência, e a chave de exemplo deve ser substituída. Alterar a chave JWT invalida tokens anteriores.

Em outro terminal PowerShell:

```powershell
$cadastro = @{ nome = 'Ana Silva'; email = 'ana@exemplo.com'; senha = 'senha-de-teste' } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/auth/cadastro' -ContentType 'application/json' -Body $cadastro

$negocio = @{ nome = 'Dona do Estudio'; email = 'dona@exemplo.com'; senha = 'senha-de-teste' } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/auth/cadastro-negocio' -ContentType 'application/json' -Body $negocio

$credenciais = @{ email = 'ana@exemplo.com'; senha = 'senha-de-teste' } | ConvertTo-Json
$login = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/auth/login' -ContentType 'application/json' -Body $credenciais
Invoke-RestMethod -Uri 'http://localhost:8080/identity/me' -Headers @{ Authorization = "Bearer $($login.token)" }
```

O cadastro retorna 201 e `{id,nome,email,role}`; o login retorna 200 e `{token,tokenType,expiresIn}` (`expiresIn` em segundos). Nenhuma resposta contém senha ou hash.

No Postman/Insomnia, repetir `GET /identity/me` sem Authorization: esperado 401. Com `Authorization: Bearer <token>`: esperado 200. Token inválido, expirado ou de usuário inexistente: 401.

Outras validações: repetir email retorna 409; senha de login incorreta retorna 401; JSON inválido, dados inválidos ou campo `role` retornam 400. Erros usam `application/problem+json`.

## Referências técnicas

- [JJWT 0.12.6](https://github.com/jwtk/jjwt/tree/0.12.6): assinatura e validação de JWT.
- [Arquitetura do Spring Security](https://docs.spring.io/spring-security/reference/servlet/architecture.html): filtro na cadeia de segurança.

## Limites desta entrega

Não inclui telas, CRUD de profissionais ou recursos de outras semanas. Sem refresh token, recuperação de senha ou verificação de email, que não constam na Semana 2. A semana permanece aguardando revisão e confirmação do usuário; nenhum commit é feito automaticamente.
