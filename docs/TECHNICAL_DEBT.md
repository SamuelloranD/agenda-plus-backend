# Dívida técnica

## `AvailabilityIntegrationTest` usa data absoluta

- **Local:** `src/test/java/com/agendaplus/AvailabilityIntegrationTest.java:29`
- **Sintoma:** `mvn -Dtest=AvailabilityIntegrationTest test` falha no cadastro do agendamento, esperando HTTP `201` e recebendo `400`.
- **Causa:** o teste usa `LocalDate.of(2026, 9, 14)`. Essa data já ficou no passado em relação à execução atual, e o domínio rejeita agendamentos passados.
- **Evidência:** a falha ocorre tanto na `master` quanto na branch de segurança, portanto não foi introduzida pela alteração da `SecurityConfiguration`.
- **Correção recomendada:** derivar a data a partir do relógio da execução, garantindo uma data futura compatível com a agenda (por exemplo, uma segunda-feira futura), e reutilizar essa variável ao montar todos os timestamps/assertions do teste.
- **Prioridade:** corrigir assim que possível, antes de depender da suíte como gate de CI. É uma alteração pequena e exclusivamente de teste; não deve ser empurrada para uma etapa de polimento visual.
