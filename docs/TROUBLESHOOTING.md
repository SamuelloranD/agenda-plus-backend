# Troubleshooting

## Testcontainers não encontra o Docker Desktop no Windows

Em versões recentes do Docker Desktop no Windows, o Testcontainers pode não detectar automaticamente o endpoint do Docker usado pelo Docker Desktop.

Se `mvn test` falhar com uma mensagem como `Could not find a valid Docker environment`, configure `DOCKER_HOST` como uma variável de ambiente do usuário, apontando para o named pipe do engine Linux:

```powershell
[Environment]::SetEnvironmentVariable(
  "DOCKER_HOST",
  "npipe:////./pipe/dockerDesktopLinuxEngine",
  "User"
)
```

Depois, feche e reabra o terminal ou a IDE e execute novamente:

```powershell
mvn test
```

Essa configuração pertence à máquina local e não deve ser adicionada ao código, ao `pom.xml`, ao `.gitignore` ou commitada no repositório. O valor pode variar conforme a instalação, a versão e o contexto ativo do Docker Desktop.
