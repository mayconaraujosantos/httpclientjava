package com.amil.rede.credenciadasimples.infra.repository;

import com.amil.rede.credenciadasimples.domain.model.RedeCredenciada;
import com.amil.rede.credenciadasimples.domain.repository.RedeCredenciadaRepository;
import com.amil.rede.credenciadasimples.infra.http.HttpClientConfig;
import com.amil.rede.credenciadasimples.infra.mapper.RedeCredenciadaJsonMapper;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP implementation of RedeCredenciadaRepository using Java 17 native HTTP Client. Follows
 * Dependency Inversion Principle and Single Responsibility Principle.
 */
public class HttpRedeCredenciadaRepository implements RedeCredenciadaRepository {

  private static final String BASE_URL =
      "https://amil.com.br/institucional/api/InstitucionalMiddleware/RedeCredenciadaSimples";

  private final HttpClient httpClient;
  private final RedeCredenciadaJsonMapper jsonMapper;

  public HttpRedeCredenciadaRepository() {
    this.httpClient = HttpClientConfig.createHttpClient();
    this.jsonMapper = new RedeCredenciadaJsonMapper();
  }

  // Constructor for dependency injection (follows Dependency Inversion Principle)
  public HttpRedeCredenciadaRepository(
      HttpClient httpClient, RedeCredenciadaJsonMapper jsonMapper) {
    this.httpClient = httpClient;
    this.jsonMapper = jsonMapper;
  }

  @Override
  public CompletableFuture<List<RedeCredenciada>> findByCpf(String cpf) {
    String url = BASE_URL + "/" + cpf;

    Map<String, String> customHeaders = createCustomHeaders();

    HttpRequest request = HttpClientConfig.createRequestBuilder(url).GET().build();

    // Add custom headers
    HttpRequest.Builder builder =
        HttpRequest.newBuilder().uri(request.uri()).timeout(request.timeout().orElse(null)).GET();

    // Add all headers from the configured builder
    request
        .headers()
        .map()
        .forEach((name, values) -> values.forEach(value -> builder.header(name, value)));

    // Add custom headers specific to Amil API
    HttpClientConfig.addCustomHeaders(builder, customHeaders);

    HttpRequest finalRequest = builder.build();

    return httpClient
        .sendAsync(finalRequest, HttpResponse.BodyHandlers.ofString())
        .thenApply(HttpResponse::body)
        .thenApply(jsonMapper::mapToRedeCredenciadaList)
        .exceptionally(
            throwable -> {
              throw new RuntimeException(
                  "Failed to fetch RedeCredenciada data for CPF: " + cpf, throwable);
            });
  }

  /**
   * Creates custom headers specific to the Amil API. These headers are extracted from the curl
   * command provided. Note: Restricted headers like Connection, Accept-Encoding are handled
   * automatically by HttpClient.
   */
  private Map<String, String> createCustomHeaders() {
    Map<String, String> headers = new HashMap<>();

    // Essential headers for the Amil API
    headers.put("CorrelationId", java.util.UUID.randomUUID().toString());
    headers.put("Authorization", "Bearer undefined");
    headers.put("clientIp", "127.0.0.1"); // Using localhost IP for testing
    headers.put("Referer", "https://www.amil.com.br/institucional/");

    // Note: Cookie header is omitted for security reasons
    // In a real implementation, you would handle authentication properly

    return headers;
  }
}
