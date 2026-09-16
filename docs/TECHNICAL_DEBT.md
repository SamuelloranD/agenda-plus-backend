# Dívida técnica — resolvida

## `AvailabilityIntegrationTest` usa data absoluta

**Status:** resolvida em 16/09/2026 no commit da correção do teste.

- **Local:** `src/test/java/com/agendaplus/AvailabilityIntegrationTest.java:29`
- **Sintoma:** `mvn -Dtest=AvailabilityIntegrationTest test` falha no cadastro do agendamento, esperando HTTP `201` e recebendo `400`.
- **Causa:** o teste usa `LocalDate.of(2026, 9, 14)`. Essa data já ficou no passado em relação à execução atual, e o domínio rejeita agendamentos passados.
- **Evidência:** a falha ocorre tanto na `master` quanto na branch de segurança, portanto não foi introduzida pela alteração da `SecurityConfiguration`.
- **Correção aplicada:** a data agora é derivada do relógio da execução usando a próxima segunda-feira futura, e todos os timestamps/assertions usam essa variável.
- **Prioridade:** corrigir assim que possível, antes de depender da suíte como gate de CI. É uma alteração pequena e exclusivamente de teste; não deve ser empurrada para uma etapa de polimento visual.
