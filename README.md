# Agenda+ Backend

API REST de uma plataforma de gestão de estúdios e casas de ofício. O Agenda+ foi desenvolvido para transformar a rotina de reservas em um fluxo organizado: o negócio configura seus serviços e profissionais, o cliente encontra um horário disponível e o administrador acompanha o ciclo do atendimento.

Este repositório representa a parte do projeto em que concentrei as regras de negócio, modelagem de domínio, segurança, persistência e qualidade da aplicação.

## Demonstração online

- API: <https://agenda-plus-api-2nm8.onrender.com>
- Swagger UI: <https://agenda-plus-api-2nm8.onrender.com/swagger-ui/index.html>
- Especificação OpenAPI: <https://agenda-plus-api-2nm8.onrender.com/v3/api-docs>
- Interface web: <https://agenda-plus-frontend-pearl.vercel.app>

Como entrega adicional, o backend foi empacotado com Docker e publicado em uma infraestrutura cloud integrada a um PostgreSQL gerenciado. A publicação utiliza variáveis de ambiente para manter credenciais, segredo JWT e origens CORS fora do código-fonte.

## O problema que o projeto resolve

Pequenos estúdios frequentemente controlam reservas, disponibilidade e clientes por mensagens, planilhas ou anotações desconectadas. Isso gera conflitos de horário, pouca visibilidade sobre a agenda e dificuldade para o cliente acompanhar seus compromissos.

O Agenda+ organiza esse fluxo em uma API com responsabilidades bem definidas:

- o negócio mantém o catálogo de serviços;
- profissionais possuem horários de trabalho e disponibilidade calculada;
- clientes podem reservar horários livres;
- administradores confirmam ou cancelam atendimentos;
- agendamentos futuros ficam separados dos atendimentos do dia para facilitar a operação.

## Funcionalidades

- Cadastro de contas de negócio e de clientes.
- Login stateless com token JWT.
- Consulta do usuário autenticado.
- Cadastro, alteração, consulta e remoção de serviços.
- Cadastro, alteração, consulta e remoção de profissionais.
- Definição de especialidade e horários de trabalho.
- Cálculo de horários disponíveis por profissional, serviço e data.
- Criação de agendamentos com validação de período e disponibilidade.
- Consulta de agendamentos do cliente autenticado.
- Consulta administrativa por período, profissional, status e paginação.
- Confirmação e cancelamento de agendamentos.
- Migrações versionadas e reprodutíveis com Flyway.
- Documentação da API com Swagger/OpenAPI.

## Stack

| Área | Tecnologias |
| --- | --- |
| Linguagem | Java 21 |
| Framework | Spring Boot 3.5 |
| API | Spring Web, Bean Validation e Problem Details |
| Persistência | Spring Data JPA e PostgreSQL |
| Evolução do banco | Flyway |
| Segurança | Spring Security e JWT com JJWT |
| Mapeamento | MapStruct |
| Documentação | Springdoc OpenAPI |
| Testes | JUnit, Spring Boot Test e Testcontainers |
| Build e execução | Maven e Docker |

## Arquitetura: DDD aplicado a um projeto prático

O backend utiliza uma organização orientada ao domínio. Em vez de agrupar todos os controllers, serviços e entidades em pastas técnicas globais, o código é dividido por contextos de negócio. Essa escolha deixa explícito onde cada regra pertence e reduz o acoplamento entre partes diferentes do sistema.

### Bounded contexts

```text
identity        Usuários, clientes, autenticação, autorização e JWT
professionals   Profissionais, especialidades e horários de trabalho
services        Catálogo de serviços, duração e preço
scheduling      Disponibilidade, reservas e ciclo de vida do atendimento
shared          Primitivas compartilhadas sem concentrar regras de negócio dos contextos
```

Cada contexto possui seu próprio modelo, casos de uso, DTOs, contratos e adaptadores. Isso cria limites claros para evolução e facilita a leitura do sistema por quem entra no projeto.

### Camadas por contexto

```text
domain/
  Entidades, objetos de valor, enums, exceções e contratos de repositório

application/
  Casos de uso, serviços de aplicação, comandos, consultas e DTOs

infrastructure/
  Controllers HTTP, persistência JPA, segurança, configurações e integrações
```

As regras centrais ficam no domínio e nos casos de uso, enquanto HTTP e banco são detalhes de infraestrutura. O resultado é uma separação que permite testar decisões de negócio sem depender diretamente do controller ou do banco.

### Decisões de modelagem

- O período do agendamento é representado por início e fim, permitindo validar duração e sobreposição.
- O serviço define a duração utilizada no cálculo dos horários disponíveis.
- O status do agendamento representa seu ciclo de vida e evita decisões espalhadas por strings.
- Regras como horário futuro, período válido, disponibilidade e antecedência para cancelamento ficam próximas do caso de uso correspondente.
- Repositórios são definidos por contratos de domínio e implementados na infraestrutura.
- Portas de aplicação e eventos de domínio ajudam a manter integrações desacopladas.
- DTOs protegem o modelo interno e tornam explícito o contrato HTTP.

## Segurança

A API adota autenticação stateless:

1. O usuário se cadastra ou realiza login.
2. A API retorna um JWT assinado.
3. O cliente envia `Authorization: Bearer <token>` nas chamadas protegidas.
4. Um filtro do Spring Security valida o token e associa o usuário à requisição.
5. As regras de autorização distinguem endpoints públicos, usuários autenticados e administradores.

O segredo JWT, a conexão com o banco e as origens permitidas pelo CORS são configuráveis por ambiente. Credenciais não fazem parte do código-fonte nem da documentação.

## Regras de negócio representadas

O projeto não trata agendamento como apenas um CRUD. Antes de criar uma reserva, o backend valida, entre outros pontos:

- início e fim no futuro;
- período consistente;
- serviço, profissional e cliente existentes;
- compatibilidade com os horários de trabalho;
- ausência de conflito com outro agendamento;
- duração do serviço;
- transição válida de status;
- regras de cancelamento.

Na consulta administrativa, os limites de data podem ser usados separadamente. Isso permite consultar tanto os atendimentos do dia quanto uma faixa aberta de atendimentos futuros.

## API principal

### Autenticação e identidade

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `POST` | `/auth/cadastro` | Cadastra cliente. |
| `POST` | `/auth/cadastro-negocio` | Cadastra administrador/negócio. |
| `POST` | `/auth/login` | Autentica e retorna JWT. |
| `GET` | `/identity/me` | Consulta o usuário da sessão. |

### Catálogo

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `GET` | `/servicos` | Lista serviços. |
| `POST` / `PUT` / `DELETE` | `/servicos` e `/servicos/{id}` | Gerencia serviços. |
| `GET` | `/profissionais` | Lista profissionais. |
| `POST` / `PUT` / `DELETE` | `/profissionais` e `/profissionais/{id}` | Gerencia profissionais. |
| `GET` | `/profissionais/{id}/horarios-disponiveis` | Calcula horários livres. |
| `GET` | `/clientes` | Lista clientes para a operação administrativa. |

### Agendamentos

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `POST` | `/agendamentos` | Cria uma reserva pendente. |
| `GET` | `/agendamentos` | Consulta administrativa paginada. |
| `GET` | `/agendamentos/meus` | Consulta reservas do cliente autenticado. |
| `PATCH` | `/agendamentos/{id}/confirmar` | Confirma uma reserva. |
| `PATCH` | `/agendamentos/{id}/cancelar` | Cancela uma reserva conforme as regras do domínio. |

A documentação completa dos schemas e respostas está disponível no Swagger UI.

## Persistência e evolução do banco

O PostgreSQL é acessado por JPA, mas a criação e a evolução do schema são controladas pelo Flyway. Cada alteração estrutural é registrada em uma migração versionada em `src/main/resources/db/migration`.

Esse fluxo evita depender de alterações manuais no banco e torna o ambiente local, os testes de integração e a publicação reproduzíveis. O Hibernate trabalha com `ddl-auto=validate`, reforçando que o schema deve estar alinhado às migrações.

## Testes e qualidade

A suíte cobre regras de domínio, casos de uso, web, segurança e persistência. Os testes de integração utilizam PostgreSQL em containers para validar o comportamento em um banco próximo do ambiente real.

Comandos principais:

```bash
mvn -B test
mvn -B verify
```

O ciclo `verify` também executa as verificações de cobertura configuradas pelo JaCoCo para as classes de domínio.

## Execução local

Pré-requisitos: JDK 21, Maven e Docker Desktop.

```bash
git clone https://github.com/SamuelIoranD/agenda-plus-backend.git
cd agenda-plus-backend
docker compose up -d postgres
mvn spring-boot:run
```

O banco local é publicado na porta `5433` do host e a API inicia em `http://localhost:8080`. Para a execução local, as variáveis `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` e `JWT_EXPIRATION` podem ser configuradas no ambiente do terminal. O arquivo `.env.example` documenta os nomes esperados sem conter segredos reais.

Para gerar a imagem:

```bash
docker build -t agenda-plus-backend .
```

## Estrutura do projeto

```text
src/main/java/com/agendaplus/
├── identity/
│   ├── domain/
│   ├── application/
│   └── infrastructure/
├── professionals/
│   ├── domain/
│   ├── application/
│   └── infrastructure/
├── services/
│   ├── domain/
│   ├── application/
│   └── infrastructure/
├── scheduling/
    ├── domain/
    ├── application/
    └── infrastructure/
└── shared/                         # componentes realmente transversais

src/main/resources/db/migration/  # histórico do schema
docs/                              # documentação complementar
scripts/                           # automações auxiliares
```

## O que este projeto demonstra

- Capacidade de transformar um problema operacional em casos de uso claros.
- Aplicação prática de DDD sem criar abstrações desconectadas do produto.
- Separação entre domínio, aplicação e infraestrutura.
- Modelagem de regras de disponibilidade e ciclo de vida.
- Segurança baseada em JWT e autorização por papel.
- Persistência versionada e integração com PostgreSQL.
- Testes automatizados próximos de um ambiente real.
- Empacotamento e publicação de uma API consumida por um frontend real.
