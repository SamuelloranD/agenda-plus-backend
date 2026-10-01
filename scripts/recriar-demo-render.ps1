param(
    [Parameter(Mandatory = $true)]
    [string] $ApiBaseUrl,

    [Parameter(Mandatory = $true)]
    [string] $AdminEmail,

    [Parameter(Mandatory = $true)]
    [string] $AdminPassword,

    [string] $ClientEmail = "cliente.demo@agenda.plus",
    [string] $ClientPassword = "AgendaPlus-demo-2026!"
)

$ErrorActionPreference = "Stop"
$ApiBaseUrl = $ApiBaseUrl.TrimEnd('/')

function Invoke-JsonApi {
    param(
        [ValidateSet("Get", "Post")]
        [string] $Method,
        [string] $Path,
        [object] $Body,
        [string] $Token
    )

    $headers = @{}
    if ($Token) { $headers.Authorization = "Bearer $Token" }
    $json = if ($null -ne $Body) { $Body | ConvertTo-Json -Depth 8 } else { $null }

    $params = @{
        Method = $Method
        Uri = "$ApiBaseUrl$Path"
        Headers = $headers
        ContentType = "application/json"
    }
    if ($null -ne $json) { $params.Body = $json }
    try {
        Invoke-RestMethod @params
    } catch {
        $response = $_.Exception.Response
        $status = if ($response) { [int]$response.StatusCode } else { "desconhecido" }
        $body = if ($response) {
            $reader = New-Object System.IO.StreamReader($response.GetResponseStream())
            try { $reader.ReadToEnd() } finally { $reader.Dispose() }
        } else {
            $_.Exception.Message
        }
        throw "API $Method $Path retornou HTTP $status. Resposta: $body"
    }
}

function Wait-ForApi {
    Write-Host "Aguardando o backend acordar..."
    for ($attempt = 1; $attempt -le 12; $attempt++) {
        try {
            Invoke-JsonApi -Method Get -Path "/servicos" | Out-Null
            Write-Host "Backend disponível."
            return
        } catch {
            if ($attempt -eq 12) { throw "O backend não respondeu após 2 minutos." }
            Start-Sleep -Seconds 10
        }
    }
}

function Ensure-Account {
    param([string] $Path, [string] $Name, [string] $Email, [string] $Password)
    try {
        $account = Invoke-JsonApi -Method Post -Path $Path -Body @{
            nome = $Name
            email = $Email
            senha = $Password
        }
        Write-Host "Conta criada: $Email"
        return $account
    } catch {
        $status = if ($_.Exception.Response) {
            $_.Exception.Response.StatusCode.value__
        } elseif ($_.Exception.Message -match "HTTP 409") {
            409
        } else {
            $null
        }
        if ($status -ne 409) { throw }
        Write-Host "Conta já existente: $Email"
    }
}

function Login {
    param([string] $Email, [string] $Password)
    (Invoke-JsonApi -Method Post -Path "/auth/login" -Body @{
        email = $Email
        senha = $Password
    }).token
}

Wait-ForApi

# O restart/deploy do Web Service executa o Flyway automaticamente no startup.
Ensure-Account -Path "/auth/cadastro-negocio" -Name "Demonstração Agenda+" -Email $AdminEmail -Password $AdminPassword
Ensure-Account -Path "/auth/cadastro" -Name "Cliente Demonstração" -Email $ClientEmail -Password $ClientPassword

$adminToken = Login -Email $AdminEmail -Password $AdminPassword
$clientAccount = @(Invoke-JsonApi -Method Get -Path "/clientes" -Token $adminToken |
    Where-Object { $_.email -eq $ClientEmail } |
    Select-Object -First 1)
if ($clientAccount.Count -eq 0) {
    throw "Nao foi possivel localizar o cliente $ClientEmail."
}
$clientAccount = $clientAccount[0]

$professional = @(Invoke-JsonApi -Method Get -Path "/profissionais" |
    Select-Object -First 1)
if ($professional.Count -eq 0) {
    $professional = Invoke-JsonApi -Method Post -Path "/profissionais" -Token $adminToken -Body @{
        nome = "Marina Costa"
        especialidade = "Cortes e acabamento"
        horariosTrabalho = @(
            @{ diaSemana = "MONDAY"; inicio = "08:00"; fim = "18:00" },
            @{ diaSemana = "TUESDAY"; inicio = "08:00"; fim = "18:00" },
            @{ diaSemana = "WEDNESDAY"; inicio = "08:00"; fim = "18:00" },
            @{ diaSemana = "THURSDAY"; inicio = "08:00"; fim = "18:00" },
            @{ diaSemana = "FRIDAY"; inicio = "08:00"; fim = "18:00" }
        )
    }
} else {
    $professional = $professional[0]
    Write-Host "Profissional existente reutilizado."
}

$existingServices = @(Invoke-JsonApi -Method Get -Path "/servicos" | Select-Object -First 1)
if ($existingServices.Count -gt 0) {
    $service = $existingServices[0]
    Write-Host "Servico existente reutilizado."
} else {
    $service = Invoke-JsonApi -Method Post -Path "/servicos" -Token $adminToken -Body @{
    nome = "Corte clássico"
    duracaoMinutos = 45
    preco = @{ valor = 65.00; moeda = "BRL" }
    }
}

$nextMonday = [DateTime]::Today.AddDays((8 - [int][DateTime]::Today.DayOfWeek) % 7)
if ($nextMonday -le [DateTime]::Today) { $nextMonday = $nextMonday.AddDays(7) }
$slots = @(
    $nextMonday.AddHours(10),
    $nextMonday.AddDays(1).AddHours(11),
    $nextMonday.AddDays(2).AddHours(14)
)

foreach ($start in $slots) {
    $finish = $start.AddMinutes(45)
    Invoke-JsonApi -Method Post -Path "/agendamentos" -Token $adminToken -Body @{
        inicio = $start.ToString("yyyy-MM-ddTHH:mm:ss")
        fim = $finish.ToString("yyyy-MM-ddTHH:mm:ss")
        profissionalId = $professional.id
        clienteId = $clientAccount.id
        servicoId = $service.id
    } | Out-Null
}

Write-Host "Demonstração recriada com sucesso."
Write-Host "Profissional: $($professional.id)"
Write-Host "Serviço: $($service.id)"
Write-Host "Cliente: $($clientAccount.id)"
