# Agenda+ Backend

API REST do Agenda+, uma plataforma de gestão de estúdios e casas de ofício. O backend centraliza autenticação, catálogo de serviços e profissionais, disponibilidade de horários e agendamentos.

## Produção

- API: <https://agenda-plus-api-2nm8.onrender.com>
- Swagger UI: <https://agenda-plus-api-2nm8.onrender.com/swagger-ui/index.html>
- OpenAPI: <https://agenda-plus-api-2nm8.onrender.com/v3/api-docs>
- Frontend: <https://agenda-plus-frontend-pearl.vercel.app>

As URLs de produção não incluem credenciais. Use variáveis de ambiente no Render para os segredos do banco e do JWT.

## Funcionalidades

- Cadastro e login de administradores e clientes.
- Autenticação stateless com JWT.
- Cadastro, edição, consulta e remoção de serviços.
- Cadastro, edição, consulta e remoção de profissionais.
- Configuração de horários de trabalho e consulta de horários disponíveis.
- Criação de agendamentos com validação de disponibilidade.
- Consulta de agendamentos por período, profissional, status e paginação.
- Confirmação e cancelamento de agendamentos por administradores.
- Migrações de banco controladas pelo Flyway.
- Documentação interativa com OpenAPI/Swagger.

## Stack

- Java 21
- Spring Boot 3.5
- Spring Web, Spring Data JPA e Spring Security
- PostgreSQL
- Flyway
- JWT com JJWT
- MapStruct
- Maven
- Testes com JUnit, Spring Boot Test e Testcontainers
- Docker

## Pré-requisitos

- JDK 21 ou superior
- Maven 3.9 ou superior, ou o Maven Wrapper incluído no repositório
- Docker Desktop, necessário para o PostgreSQL local e para a suíte de integração com Testcontainers
- Git

## Executando localmente

### 1. Baixe o projeto

```bash
git clone https://github.com/SamuelIoranD/agenda-plus-backend.git
cd agenda-plus-backend
```

### 2. Suba o PostgreSQL

O `docker-compose.yml` inicia somente o banco por padrão e publica a porta `5433` no computador host:

```bash
docker compose up -d postgres
```

Valores padrão do banco local:

| Variável | Valor padrão |
| --- | --- |
| Banco | `agenda_plus` |
| Usuário | `agenda_plus` |
| Senha | `agenda_plus` |
| Host | `localhost` |
| Porta no host | `5433` |

O arquivo `.env.example` serve como referência para as variáveis. Ele não deve conter credenciais reais e não é carregado automaticamente pelo Maven. Ao executar a aplicação fora do Docker, configure as variáveis no ambiente do terminal.

No PowerShell:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5433/agenda_plus"
$env:DB_USERNAME = "agenda_plus"
$env:DB_PASSWORD = "agenda_plus"
$env:JWT_SECRET = "dev-secret-local-com-at-least-32-characters"
$env:JWT_EXPIRATION = "PT1H"
```

No Bash:

```bash
export DB_URL="jdbc:postgresql://localhost:5433/agenda_plus"
export DB_USERNAME="agenda_plus"
export DB_PASSWORD="agenda_plus"
export JWT_SECRET="dev-secret-local-com-at-least-32-characters"
export JWT_EXPIRATION="PT1H"
```

### 3. Inicie a API

Com Maven instalado:

```bash
mvn spring-boot:run
```

Ou usando o wrapper do projeto:

```bash
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

A API ficará disponível em `http://localhost:8080`. Na primeira inicialização, o Flyway aplica automaticamente as migrações em `src/main/resources/db/migration`.

### 4. Verifique a aplicação

Abra <http://localhost:8080/swagger-ui/index.html> ou consulte a documentação JSON em <http://localhost:8080/v3/api-docs>.

Endpoints protegidos retornam `401 Unauthorized` quando não recebem um token JWT. Isso é esperado ao acessar a API sem autenticação.

## Variáveis de ambiente

| Variável | Obrigatória em produção | Descrição |
| --- | --- | --- |
| `DB_URL` | Sim | URL JDBC do PostgreSQL. No Render, use a Internal Database URL com o prefixo `jdbc:postgresql://`. |
| `DB_USERNAME` | Sim | Usuário do PostgreSQL. |
| `DB_PASSWORD` | Sim | Senha do PostgreSQL, nunca a senha da conta do Render. |
| `JWT_SECRET` | Sim | Segredo com pelo menos 32 caracteres, exclusivo da implantação. |
| `JWT_EXPIRATION` | Não | Duração do token no formato ISO-8601; padrão `PT1H`. |
| `CORS_ALLOWED_ORIGINS` | Recomendada | Origens separadas por vírgula. Exemplo: `https://agenda-plus-frontend-pearl.vercel.app`. |
| `PORT` | Fornecida pela plataforma | Porta HTTP usada pelo Render. Localmente o padrão é `8080`. |

Nunca versione senhas reais, tokens, URLs com credenciais ou arquivos `.env`. O `.env.example` deve conter apenas valores fictícios.

## API principal

Todas as datas usam ISO-8601. Endpoints protegidos usam o cabeçalho:

```http
Authorization: Bearer <token>
```

### Autenticação

| Método | Endpoint | Acesso | Uso |
| --- | --- | --- | --- |
| `POST` | `/auth/cadastro` | Público | Cria uma conta de cliente. |
| `POST` | `/auth/cadastro-negocio` | Público | Cria uma conta de administrador/negócio. |
| `POST` | `/auth/login` | Público | Retorna o token JWT. |
| `GET` | `/identity/me` | Autenticado | Retorna o usuário da sessão atual. |

Exemplo de login:

```json
{
  "email": "cliente@example.com",
  "senha": "uma-senha-segura"
}
```

### Catálogo

| Método | Endpoint | Acesso | Uso |
| --- | --- | --- | --- |
| `GET` | `/servicos` | Público | Lista serviços. |
| `POST` | `/servicos` | Autenticado | Cria serviço. |
| `GET` | `/servicos/{id}` | Público | Consulta serviço. |
| `PUT` | `/servicos/{id}` | Autenticado | Atualiza serviço. |
| `DELETE` | `/servicos/{id}` | Autenticado | Remove serviço. |
| `GET` | `/profissionais` | Público | Lista profissionais. |
| `POST` | `/profissionais` | Autenticado | Cria profissional. |
| `GET` | `/profissionais/{id}` | Público | Consulta profissional. |
| `PUT` | `/profissionais/{id}` | Autenticado | Atualiza profissional. |
| `DELETE` | `/profissionais/{id}` | Autenticado | Remove profissional. |
| `GET` | `/profissionais/{id}/horarios-disponiveis` | Público | Consulta horários livres por data e serviço. |
| `GET` | `/clientes` | Autenticado | Lista clientes. |
| `GET` | `/clientes/{id}` | Autenticado | Consulta cliente. |
| `PUT` | `/clientes/{id}` | Autenticado | Atualiza cliente. |
| `DELETE` | `/clientes/{id}` | Autenticado | Remove cliente. |

### Agendamentos

| Método | Endpoint | Acesso | Uso |
| --- | --- | --- | --- |
| `POST` | `/agendamentos` | Autenticado | Cria agendamento pendente. |
| `GET` | `/agendamentos` | Administrador | Lista por período, profissional e status. |
| `GET` | `/agendamentos/meus` | Cliente | Lista os agendamentos do cliente autenticado. |
| `PATCH` | `/agendamentos/{id}/confirmar` | Autenticado | Confirma agendamento. |
| `PATCH` | `/agendamentos/{id}/cancelar` | Autenticado | Cancela agendamento conforme as regras de negócio. |

Na listagem administrativa, `dataInicio` e `dataFim` podem ser usados separadamente para consultar, por exemplo, somente os atendimentos futuros. É necessário informar ao menos um limite de data. `pagina` começa em `0` e `tamanho` tem padrão `20`.

Exemplo:

```text
GET /agendamentos?dataInicio=2026-10-01&pagina=0&tamanho=20
```

## Testes e qualidade

Executa a suíte completa, incluindo verificações de cobertura:

```bash
mvn -B verify
```

Executa somente os testes:

```bash
mvn -B test
```

Gera o artefato sem executar testes:

```bash
mvn -B -DskipTests package
```

O `verify` utiliza Testcontainers em testes de integração; mantenha o Docker em execução. A cobertura do domínio é verificada pelo JaCoCo durante o ciclo de build.

## Docker

Para construir a imagem:

```bash
docker build -t agenda-plus-backend .
```

Para executar a aplicação apontando para um PostgreSQL acessível:

```powershell
docker run --rm -p 8080:8080 `
  -e DB_URL="jdbc:postgresql://host.docker.internal:5433/agenda_plus" `
  -e DB_USERNAME="agenda_plus" `
  -e DB_PASSWORD="agenda_plus" `
  -e JWT_SECRET="dev-secret-local-com-at-least-32-characters" `
  agenda-plus-backend
```

No Bash, substitua a crase final de cada linha por `\`.

O serviço de backend no `docker-compose.yml` está comentado para manter o fluxo local simples. Ele pode ser habilitado quando for necessário subir a aplicação e o banco na mesma rede Docker; nesse cenário, a URL do banco deve usar o hostname `postgres` e a porta interna `5432`.

## Deploy no Render

O serviço de produção usa o Dockerfile da raiz e a branch `master`.

1. Crie um PostgreSQL no Render.
2. Crie um Web Service conectado ao repositório `agenda-plus-backend`.
3. Selecione runtime `Docker`, região compatível com o banco e a branch `master`.
4. Adicione as variáveis `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION` e `CORS_ALLOWED_ORIGINS`.
5. Em `DB_URL`, use a Internal Database URL do banco do Render convertida para JDBC: `jdbc:postgresql://host:5432/database`.
6. Use a senha exibida na seção de credenciais do banco para `DB_PASSWORD`; ela não é a senha da conta do Render.
7. Faça o deploy e confirme o Swagger e o login pela URL pública.

O Render fornece `PORT` automaticamente. A aplicação lê essa variável e usa `8080` como fallback local.

## Estrutura

```text
src/main/java/com/agendaplus/
├── identity/       # autenticação, usuários, clientes e segurança JWT
├── professionals/  # profissionais e horários de trabalho
├── services/       # catálogo de serviços
└── scheduling/     # disponibilidade e agendamentos

src/main/resources/
└── db/migration/   # migrações versionadas do Flyway

scripts/            # scripts auxiliares de demonstração e deploy
docs/               # documentação complementar
```

O projeto separa domínio, casos de uso, DTOs e infraestrutura web/persistência por contexto de negócio.

## Desenvolvimento

- Prefira alterações pequenas e focadas por contexto.
- Não coloque credenciais em código ou logs.
- Atualize as migrações do Flyway quando alterar o modelo persistido.
- Adicione ou ajuste testes junto com cada mudança de regra de negócio.
- Antes de enviar alterações, execute `mvn -B verify`.
