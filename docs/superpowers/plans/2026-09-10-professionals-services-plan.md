# Professionals, Services, Clients and Availability Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the backend CRUDs for professionals, services and authenticated clients, plus service-duration-based availability slots.

**Architecture:** Professionals and services use the project's simplified context style with direct JPA entities and focused services/controllers. Availability is orchestrated by `scheduling/application/usecase/ListarHorariosDisponiveisUseCase`, which reads the three contexts and reuses `VerificadorDeDisponibilidade`. `Dinheiro` is a single shared Value Object in `shared/domain/model`.

**Tech Stack:** Java 21, Spring Boot 3.5, Spring Data JPA, Flyway, Bean Validation, PostgreSQL, JUnit 5, Mockito, MockMvc and Testcontainers.

**Spec:** `docs/superpowers/specs/2026-09-10-professionals-services-design.md`

## Global Constraints

- Use UUID identifiers in Java, PostgreSQL and JSON.
- Do not edit existing Flyway migrations; add an incremental migration.
- Do not implement frontend, events, notifications, auditing or other future-scope work.
- `Dinheiro` lives only at `shared/domain/model/Dinheiro.java` and is reused by `services`.
- Client CRUD manages only `Usuario` records with `Role.CLIENTE`; walk-in clients are out of scope.
- Availability requires `data` and `servicoId`; slots advance by the service duration from each work-range start.
- New behavior must be covered by tests written before production code.

---

### Task 1: Shared money value object and database migration

**Files:**
- Create: `src/main/java/com/agendaplus/shared/domain/model/Dinheiro.java`
- Create: `src/main/resources/db/migration/V4__create_profissionais_servicos.sql`
- Test: `src/test/java/com/agendaplus/shared/domain/model/DinheiroTest.java`

**Interfaces:**
- Produces `Dinheiro(BigDecimal valor, Currency moeda)`, accessors, equality and a JSON-safe decimal representation for service DTOs.
- Migration creates professional work ranges and service tables with UUID primary keys and PostgreSQL-compatible numeric price storage.

- [ ] Write failing tests for rejecting null/negative values, normalizing scale, equality, and preserving the configured currency.
- [ ] Run `mvn -q -Dtest=DinheiroTest test`; expect failure because `Dinheiro` does not exist.
- [ ] Implement the minimal immutable `Dinheiro` value object with `BigDecimal` and `Currency`.
- [ ] Add the migration for `profissionais`, `horarios_trabalho` and `servicos`, including constraints for valid times, positive duration and non-negative price.
- [ ] Run `mvn -q -Dtest=DinheiroTest test`; expect passing tests.
- [ ] Commit with `feat: adiciona valor monetario compartilhado e tabelas de catalogo`.

### Task 2: Professional model and CRUD

**Files:**
- Create or modify under `src/main/java/com/agendaplus/professionals/`: entity, repository, service, DTOs and controller.
- Create: `src/test/java/com/agendaplus/ProfessionalsIntegrationTest.java`

**Interfaces:**
- `Profissional` exposes UUID `id`, `nome`, `especialidade` and ordered work ranges by `DayOfWeek`.
- CRUD endpoints use `/profissionais`: `POST`, `GET`, `GET /{id}`, `PUT /{id}` and `DELETE /{id}`.
- Request ranges contain day of week, start and end local times; response never exposes persistence-only fields.

- [ ] Add integration tests for creating, listing, retrieving, updating and deleting a professional, including persisted work ranges.
- [ ] Run the focused integration test with Docker and verify it fails because the endpoints/entities do not exist.
- [ ] Implement direct JPA entities and Spring Data repository in the simplified `professionals` context; include the required top-of-class comment explaining the simplified DDD choice.
- [ ] Implement the service/controller and Bean Validation for nonblank names, valid specialties and `end > start` work ranges.
- [ ] Run the focused integration test and verify all professional CRUD assertions pass.
- [ ] Commit with `feat: implementa CRUD de profissionais e horarios de trabalho`.

### Task 3: Service model and CRUD

**Files:**
- Create or modify under `src/main/java/com/agendaplus/services/`: entity, repository, service, DTOs and controller.
- Create: `src/test/java/com/agendaplus/ServicesIntegrationTest.java`

**Interfaces:**
- `Servico` exposes UUID `id`, `nome`, duration in minutes and `Dinheiro preco`.
- CRUD endpoints use `/servicos`: `POST`, `GET`, `GET /{id}`, `PUT /{id}` and `DELETE /{id}`.
- Price JSON uses a decimal amount; persistence stores the amount and currency required by `Dinheiro`.

- [ ] Add integration tests for service CRUD and invalid duration/price requests.
- [ ] Run the focused integration test and verify failure before implementation.
- [ ] Implement the simplified direct JPA service model while keeping `Dinheiro` in `shared/domain/model`.
- [ ] Implement repository, service and controller with constructor injection and validation.
- [ ] Run the focused integration test and verify all service CRUD assertions pass.
- [ ] Commit with `feat: implementa CRUD de servicos com preco monetario`.

### Task 4: Client CRUD over identity users

**Files:**
- Modify: `src/main/java/com/agendaplus/identity/domain/repository/UsuarioRepository.java`
- Modify: `src/main/java/com/agendaplus/identity/infrastructure/persistence/SpringDataUsuarioRepository.java`
- Modify: `src/main/java/com/agendaplus/identity/infrastructure/persistence/UsuarioRepositoryImpl.java`
- Create or modify under `src/main/java/com/agendaplus/identity/application/cliente/` and `identity/infrastructure/web/`.
- Create: `src/test/java/com/agendaplus/ClientIntegrationTest.java`

**Interfaces:**
- `/clientes` lists and retrieves only `Role.CLIENTE` users and never returns password/hash.
- Update changes the client's name and email through the existing identity persistence path.
- Delete removes the client record; walk-in creation is not added.

- [ ] Add integration tests proving role filtering, no password/hash leakage, update, delete and not-found behavior.
- [ ] Run the focused integration test and verify failure before implementation.
- [ ] Extend the identity repository with role-filtered listing and deletion/update operations needed by the CRUD.
- [ ] Implement the client service/controller using `UsuarioResponse`-compatible safe output.
- [ ] Run the focused integration test and verify it passes.
- [ ] Commit with `feat: adiciona CRUD de clientes autenticados`.

### Task 5: Availability use case and endpoint

**Files:**
- Create: `src/main/java/com/agendaplus/scheduling/application/usecase/ListarHorariosDisponiveisUseCase.java`
- Create: availability request/response DTOs under `scheduling/application/dto/`.
- Modify: `src/main/java/com/agendaplus/scheduling/domain/repository/AgendamentoRepository.java` and implementation as needed for day-range queries.
- Modify: professional/service repository interfaces only where needed to expose read operations.
- Modify or create the Controller responsible for `/profissionais/{id}/horarios-disponiveis`.
- Create: `src/test/java/com/agendaplus/scheduling/application/usecase/ListarHorariosDisponiveisUseCaseTest.java`
- Extend: integration coverage for availability.

**Interfaces:**
- `ListarHorariosDisponiveisUseCase.executar(UUID profissionalId, LocalDate data, UUID servicoId)` returns ordered slot responses with `inicio` and `fim`.
- It loads the professional work ranges for `data.getDayOfWeek()`, service duration and existing appointments for `[data 00:00, next day 00:00)`.
- Each candidate starts at the range start and advances by the service duration; `VerificadorDeDisponibilidade` decides conflict status.

- [ ] Add unit tests for exact-fit ranges, leftover time too short for a slot, conflicts, cancelled appointments and multiple work ranges.
- [ ] Run the unit test and verify failure before implementation.
- [ ] Implement the use case with constructor-injected read dependencies and no HTTP or JPA imports in the scheduling domain service.
- [ ] Add the endpoint with required ISO date and UUID parameters, returning 400 for missing/invalid parameters and 404 for missing professional/service.
- [ ] Add integration coverage proving a booked slot is omitted while a cancelled appointment does not block availability.
- [ ] Run unit and focused integration tests.
- [ ] Commit with `feat: adiciona consulta de horarios por servico`.

### Task 6: Cross-context verification and cleanup

**Files:**
- Modify only files required by failing tests or compiler diagnostics from Tasks 1–5.
- Test: full backend test suite.

- [ ] Run `mvn -q test` with Docker Desktop available.
- [ ] Run `git diff --check` and inspect the final diff for scope leaks.
- [ ] Verify no changed path, branch reference or new commit message contains the cronogram term prohibited by the project owner.
- [ ] Verify no frontend, future-scope event or notification files changed.
- [ ] Commit any strictly necessary test/build fix with a content-based message.
