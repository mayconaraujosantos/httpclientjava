# HTTP Client Java 17 - Amil API Example

Este projeto demonstra como usar o **HTTP Client nativo do Java 17** para consumir a API da Amil, seguindo princípios de **Clean Architecture** e **SOLID**.

## 🏗️ Arquitetura

O projeto está organizado seguindo Clean Architecture:

```
src/main/java/com/amil/rede/credenciadasimples/
├── domain/
│   ├── model/
│   │   └── RedeCredenciada.java          # Entidade de domínio
│   └── repository/
│       └── RedeCredenciadaRepository.java # Interface do repositório
├── application/
│   └── usecase/
│       └── FindRedeCredenciadaByCpfUseCase.java # Caso de uso
├── infra/
│   ├── config/
│   │   └── DependencyConfig.java         # Configuração de dependências
│   ├── http/
│   │   └── HttpClientConfig.java         # Configuração do HTTP Client
│   ├── mapper/
│   │   └── RedeCredenciadaJsonMapper.java # Mapeamento JSON (sem dependências externas)
│   └── repository/
│       └── HttpRedeCredenciadaRepository.java # Implementação HTTP
├── presentation/
│   └── controller/
│       └── RedeCredenciadaController.java # Controlador
├── HttpClientExample.java                # Exemplo com Clean Architecture
└── SimpleHttpClientExample.java          # Exemplo simples e direto
```

## 🚀 Como usar

### Exemplo Simples (Direto)

```java
// Execute a classe SimpleHttpClientExample
// Demonstra o uso básico do HTTP Client nativo
```

### Exemplo com Clean Architecture

```java
// Execute a classe HttpClientExample
// Demonstra a arquitetura completa
```

## 🔧 Características do HTTP Client Java 17

### 1. **Configuração do Cliente**

```java
HttpClient client = HttpClient.newBuilder()
    .version(HttpClient.Version.HTTP_2)        // HTTP/2 por padrão
    .connectTimeout(Duration.ofSeconds(30))    // Timeout de conexão
    .followRedirects(HttpClient.Redirect.NORMAL) // Segue redirects
    .build();
```

### 2. **Criação de Requisições**

```java
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create(url))
    .timeout(Duration.ofSeconds(30))
    .header("Accept", "application/json")
    .header("User-Agent", "Mozilla/5.0...")
    .GET()  // ou .POST(), .PUT(), .DELETE()
    .build();
```

### 3. **Execução Assíncrona**

```java
CompletableFuture<HttpResponse<String>> future =
    client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

future
    .thenApply(HttpResponse::body)
    .thenApply(this::parseJson)
    .thenAccept(this::processResult)
    .exceptionally(this::handleError);
```

## 🏛️ Princípios SOLID Aplicados

### Single Responsibility Principle (SRP)

- `HttpClientConfig`: Apenas configuração HTTP
- `RedeCredenciadaJsonMapper`: Apenas mapeamento JSON
- `FindRedeCredenciadaByCpfUseCase`: Apenas lógica de busca por CPF

### Open/Closed Principle (OCP)

- Interface `RedeCredenciadaRepository` permite diferentes implementações
- Pode ser estendido sem modificar código existente

### Liskov Substitution Principle (LSP)

- `HttpRedeCredenciadaRepository` pode substituir qualquer implementação de `RedeCredenciadaRepository`

### Interface Segregation Principle (ISP)

- Interfaces focadas e específicas para cada responsabilidade

### Dependency Inversion Principle (DIP)

- Use cases dependem de abstrações (interfaces), não de implementações
- Configuração de dependências centralizada

## 📦 Vantagens do HTTP Client Java 17

### ✅ **Nativo**

- Sem dependências externas
- Parte da biblioteca padrão do Java

### ✅ **Moderno**

- Suporte HTTP/2 nativo
- API fluente e intuitiva
- Programação assíncrona com CompletableFuture

### ✅ **Performático**

- Connection pooling automático
- Reutilização de conexões
- Compressão automática

### ✅ **Flexível**

- Configuração detalhada
- Interceptors customizáveis
- Diferentes body handlers

## 🔄 Comparação com outras bibliotecas

| Característica      | HTTP Client Java 17 | OkHttp   | Apache HttpClient |
| ------------------- | ------------------- | -------- | ----------------- |
| Dependência Externa | ❌                  | ✅       | ✅                |
| HTTP/2              | ✅                  | ✅       | ✅                |
| Async               | ✅                  | ✅       | ✅                |
| Configuração        | Simples             | Complexa | Complexa          |
| Tamanho             | 0 MB                | ~2 MB    | ~5 MB             |

## 🧪 Executando os Exemplos

### Compilar o projeto:

```bash
./gradlew build
```

### Executar exemplo simples:

```bash
./gradlew run -PmainClass=com.amil.rede.credenciadasimples.SimpleHttpClientExample
```

### Executar exemplo com Clean Architecture:

```bash
./gradlew run -PmainClass=com.amil.rede.credenciadasimples.HttpClientExample
```

## 📝 Headers da API Amil

O exemplo inclui todos os headers necessários extraídos do comando curl:

- `Accept`: application/json, text/plain, _/_
- `Authorization`: Bearer undefined
- `CorrelationId`: UUID único
- `User-Agent`: Browser simulation
- `Referer`: https://www.amil.com.br/institucional/
- E outros headers de segurança

## 🛡️ Considerações de Segurança

- Headers de Cookie foram omitidos por segurança
- Use autenticação adequada em produção
- Implemente retry logic e circuit breakers
- Configure timeouts apropriados

## 📚 Recursos Adicionais

- [Documentação oficial HTTP Client](https://docs.oracle.com/en/java/javase/17/docs/api/java.net.http/java/net/http/HttpClient.html)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Princípios SOLID](https://en.wikipedia.org/wiki/SOLID)
