# Recriar a demonstração no Render Free

O PostgreSQL Free do Render expira após 30 dias. Quando isso acontecer, crie um
novo PostgreSQL, atualize a Internal Database URL nas variáveis do Web Service
e faça um novo deploy ou restart. O Spring Boot executa as migrations Flyway
automaticamente durante o startup.

Depois que o serviço estiver disponível, execute:

```powershell
.\scripts\recriar-demo-render.ps1 `
  -ApiBaseUrl "https://agenda-plus-api.onrender.com" `
  -AdminEmail "seu-email-de-admin@example.com" `
  -AdminPassword "sua-senha-forte"
```

O script aguarda o cold start, cria ou reutiliza uma conta ADMIN e uma conta
CLIENTE, cadastra um profissional com horário de trabalho, cadastra um serviço
e cria três agendamentos futuros de demonstração.

Não coloque senhas ou URLs com credenciais neste arquivo. A URL do banco deve
ser convertida no painel do Render para o formato JDBC usado pela aplicação:

```text
postgres://usuario:senha@host/banco
jdbc:postgresql://host/banco
```

Use `DB_URL` com a URL JDBC, `DB_USERNAME` com o usuário separado e
`DB_PASSWORD` com a senha separada.
