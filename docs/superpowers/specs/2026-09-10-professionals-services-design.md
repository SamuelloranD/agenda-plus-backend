# Profissionais, Serviços, Clientes e Disponibilidade

## Objetivo

Completar os fluxos de backend previstos para o MVP: CRUD de profissionais, serviços e clientes, além de consulta de horários disponíveis por profissional e serviço.

## Escopo

- `Profissional` pertence ao contexto `professionals` e terá nome, especialidade e faixas de trabalho por dia da semana.
- `Servico` pertence ao contexto `services` e terá nome, duração em minutos e preço representado pelo Value Object compartilhado `Dinheiro`.
- `Cliente` será o `Usuario` existente com `role = CLIENTE`. O CRUD `/clientes` atenderá somente usuários cadastrados via `/auth/cadastro`; clientes walk-in, sem login, não fazem parte desta entrega.
- O endpoint de disponibilidade será `GET /profissionais/{id}/horarios-disponiveis?data=...&servicoId=...`, com ambos os parâmetros obrigatórios.

## Orquestração entre contextos

`ListarHorariosDisponiveisUseCase`, em `scheduling/application/usecase/`, orquestrará a operação. Ele consultará os repositórios de profissional, serviço e agendamento, gerará os candidatos e reutilizará `VerificadorDeDisponibilidade` para rejeitar conflitos. O Controller apenas converterá os parâmetros HTTP e formatará a resposta.

## Regra de geração de slots

Para cada faixa de trabalho do dia, o primeiro slot começa no início da faixa. Cada candidato seguinte avança exatamente a duração do serviço. Um candidato só será retornado se seu fim não ultrapassar o fim da faixa e se não conflitar com agendamentos existentes. Horários intermediários que não se alinham à grade iniciada no expediente não serão oferecidos nesta versão; essa é uma simplificação intencional do MVP.

## Persistência e API

- Profissionais e serviços usarão entidades JPA diretas, conforme o rigor DDD simplificado definido no README; seus CRUDs ficarão organizados nos próprios contextos `professionals/` e `services/`, sem Mapper separado nem implementação de Repository totalmente desacoplada.
- Cada CRUD terá endpoints REST protegidos e respostas JSON com IDs UUID.
- Será criada migration incremental para as novas tabelas; migrations existentes não serão alteradas.
- O CRUD de clientes filtrará por `role = CLIENTE` e não exporá senha ou hash.

## Testes

- Testes unitários cobrirão a geração de slots e o Value Object `Dinheiro`, quando houver regras novas.
- Testes de integração cobrirão operações principais dos CRUDs e disponibilidade sem conflito/com conflito.
- A verificação completa depende de Docker disponível para Testcontainers; essa limitação será reportada se permanecer.

## Fora de escopo

- Cadastro de clientes walk-in sem autenticação.
- Alterações no frontend.
- Eventos, notificações, auditoria e demais tarefas futuras.
