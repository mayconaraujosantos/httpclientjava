# 🚀 Rede Credenciada API - Spring Boot + HTTP Client Java 17

Esta API REST demonstra a integração do **HTTP Client nativo do Java 17** com **Spring Boot**, seguindo princípios de **Clean Architecture** e **SOLID**.

## 📋 Resumo da Implementação

### 🏗️ **Arquitetura da API**

```
📁 Spring Boot Application
├── 🎯 Presentation Layer
│   ├── RedeCredenciadaRestController.java - REST endpoints
│   ├── ApiExceptionHandler.java - Global exception handling
│   └── DTOs/
│       ├── RedeCredenciadaDto.java - Response object
│       └── ApiResponse.java - Standardized response wrapper
├── 🔧 Application Layer
│   └── FindRedeCredenciadaByCpfUseCase.java - Business logic
├── 🏛️ Domain Layer
│   ├── RedeCredenciada.java - Domain entity
│   └── RedeCredenciadaRepository.java - Repository interface
└── 🔌 Infrastructure Layer
    ├── HttpClientConfig.java - HTTP Client configuration
    ├── RedeCredenciadaJsonMapper.java - JSON parsing
    ├── HttpRedeCredenciadaRepository.java - HTTP implementation
    └── SpringConfig.java - Dependency injection
```

## 🌐 **Endpoints Disponíveis**

### 1. **Buscar Rede Credenciada por CPF**

```http
GET /api/v1/rede-credenciada/cpf/{cpf}
```

**Exemplo:**

```bash
curl http://localhost:8080/api/v1/rede-credenciada/cpf/01596670207
```

**Resposta:**

```json
{
  "success": true,
  "message": "Encontrados 8 registros para CPF 01596670207",
  "data": [
    {
      "codigo_rede": 884,
      "situacao": "ATIVO",
      "nome_plano_cartao": "AMIL S380 COPART",
      "nome_do_plano": "AMIL S380 QP NAC R COPART S/OBST PJ",
      "registro_ans": "483785191",
      "classificacao": "2-Coletivo Empresarial",
      "beneficiario_status": "ATIVO",
      "contexto": "AMIL",
      "operadora": "AMIL_PLANOS",
      "modalidade": "SAUDE",
      "numero_carterinha": "094236258",
      "unidade": "São Paulo"
    }
  ],
  "timestamp": "2025-07-07T23:32:20.112654",
  "path": "uri=/api/v1/rede-credenciada/cpf/01596670207"
}
```

### 2. **Health Check**

```http
GET /api/v1/rede-credenciada/health
```

**Exemplo:**

```bash
curl http://localhost:8080/api/v1/rede-credenciada/health
```

**Resposta:**

```json
{
  "success": true,
  "message": "RedeCredenciada API está funcionando",
  "data": "HTTP Client Java 17 + Spring Boot",
  "timestamp": "2025-07-07T23:31:51.499535"
}
```

### 3. **Informações da API**

```http
GET /api/v1/rede-credenciada/info
```

**Exemplo:**

```bash
curl http://localhost:8080/api/v1/rede-credenciada/info
```

## ⚡ **Características Técnicas**

### 🔥 **HTTP Client Java 17 Features**

- ✅ **Nativo**: Zero dependências externas
- ✅ **HTTP/2**: Suporte nativo ao protocolo mais recente
- ✅ **Assíncrono**: CompletableFuture para operações não-bloqueantes
- ✅ **Connection Pooling**: Reutilização automática de conexões
- ✅ **Compression**: Suporte automático à compressão

### 🏛️ **Clean Architecture**

- ✅ **Separation of Concerns**: Cada camada tem responsabilidade específica
- ✅ **Dependency Inversion**: Dependências apontam para abstrações
- ✅ **Testability**: Fácil criação de testes unitários
- ✅ **Maintainability**: Código organizado e de fácil manutenção

### 🎯 **Spring Boot Integration**

- ✅ **Dependency Injection**: Injeção automática de dependências
- ✅ **Exception Handling**: Tratamento global de exceções
- ✅ **JSON Serialization**: Conversão automática para JSON
- ✅ **CORS Support**: Suporte a requisições cross-origin
- ✅ **Async Support**: Endpoints assíncronos com CompletableFuture

## 🚀 **Como Executar**

### 1. **Compilar e Executar**

```bash
# Compilar o projeto
./gradlew build

# Executar a aplicação
./gradlew bootRun
```

### 2. **Testar os Endpoints**

```bash
# Health check
curl http://localhost:8080/api/v1/rede-credenciada/health

# Informações da API
curl http://localhost:8080/api/v1/rede-credenciada/info

# Buscar por CPF
curl http://localhost:8080/api/v1/rede-credenciada/cpf/01596670207
```

## 🛡️ **Tratamento de Erros**

### **CPF Inválido**

```bash
curl http://localhost:8080/api/v1/rede-credenciada/cpf/123
```

```json
{
  "success": false,
  "message": "CPF must have 11 digits",
  "timestamp": "2025-07-07T23:32:46.383672",
  "path": "uri=/api/v1/rede-credenciada/cpf/123"
}
```

### **Erro de API Externa**

```json
{
  "success": false,
  "message": "Erro ao buscar dados da rede credenciada: Failed to fetch RedeCredenciada data for CPF...",
  "timestamp": "2025-07-07T23:32:20.112654",
  "path": "uri=/api/v1/rede-credenciada/cpf/12345678901"
}
```

## 📊 **Comparação de Performance**

| Característica     | HTTP Client Java 17  | OkHttp + Retrofit | Feign + Spring |
| ------------------ | -------------------- | ----------------- | -------------- |
| **Dependências**   | ❌ Nenhuma           | ✅ ~5MB           | ✅ ~8MB        |
| **Startup Time**   | ⚡ Rápido            | ⚠️ Médio          | ⚠️ Lento       |
| **Memory Usage**   | 💚 Baixo             | ⚠️ Médio          | ⚠️ Alto        |
| **HTTP/2**         | ✅ Nativo            | ✅                | ✅             |
| **Async**          | ✅ CompletableFuture | ✅                | ✅             |
| **Learning Curve** | 💚 Baixa             | ⚠️ Média          | ⚠️ Alta        |

## 🔧 **Configurações**

### **application.properties**

```properties
# Server configuration
server.port=8080

# Logging
logging.level.com.amil.rede.credenciadasimples=INFO

# JSON configuration
spring.jackson.property-naming-strategy=SNAKE_CASE
spring.jackson.default-property-inclusion=NON_NULL
```

## 📚 **Benefícios da Implementação**

### ✅ **Para Desenvolvedores**

- **Menos Dependências**: Projeto mais leve
- **Performance Superior**: HTTP/2 nativo
- **Código Limpo**: Seguindo Clean Architecture
- **Fácil Manutenção**: Separação clara de responsabilidades

### ✅ **Para Produção**

- **Menor Footprint**: Menos memória utilizada
- **Startup Rápido**: Menos dependências para carregar
- **Segurança**: Sem vulnerabilidades de libs externas
- **Estabilidade**: Usando APIs nativas do Java

## 🎯 **Casos de Uso Ideais**

1. **Microserviços**: HTTP Client leve para comunicação entre serviços
2. **APIs Gateway**: Performance superior para proxy de requisições
3. **Integrações Simples**: Consumo direto de APIs REST
4. **Aplicações Cloud Native**: Menor overhead e startup rápido

## 🏆 **Conclusão**

Esta implementação demonstra como o **HTTP Client nativo do Java 17** pode ser uma excelente escolha para projetos modernos, oferecendo:

- 🚀 **Performance excepcional** com HTTP/2
- 🏗️ **Arquitetura limpa** e maintível
- ⚡ **Zero dependências externas**
- 🔧 **Integração perfeita** com Spring Boot
- 📈 **Escalabilidade** com programação assíncrona

O HTTP Client Java 17 prova ser uma alternativa robusta e eficiente para bibliotecas terceiras, especialmente em cenários onde performance, simplicidade e redução de dependências são prioritários.
