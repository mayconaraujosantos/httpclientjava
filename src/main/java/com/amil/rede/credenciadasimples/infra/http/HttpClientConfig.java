package com.amil.rede.credenciadasimples.infra.http;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Map;

/**
 * HTTP Client configuration following Single Responsibility Principle. Encapsulates HTTP client
 * creation and configuration.
 */
public class HttpClientConfig {

  private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
  private static final HttpClient.Version HTTP_VERSION = HttpClient.Version.HTTP_2;

  /**
   * Creates a configured HttpClient with optimal settings.
   *
   * @return Configured HttpClient instance
   */
  public static HttpClient createHttpClient() {
    return HttpClient.newBuilder()
        .version(HTTP_VERSION)
        .connectTimeout(DEFAULT_TIMEOUT)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
  }

  /**
   * Creates an HttpRequest.Builder with common headers pre-configured. Note: Some headers like
   * Connection, Accept-Encoding are restricted and managed by the HTTP client.
   *
   * @param uri The target URI
   * @return HttpRequest.Builder with common headers
   */
  public static HttpRequest.Builder createRequestBuilder(String uri) {
    return HttpRequest.newBuilder()
        .uri(java.net.URI.create(uri))
        .timeout(DEFAULT_TIMEOUT)
        .header("Accept", "application/json, text/plain, */*")
        .header("Accept-Language", "pt-BR,pt;q=0.8,en-US;q=0.5,en;q=0.3")
        .header(
            "User-Agent",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:139.0) Gecko/20100101 Firefox/139.0");
  }

  /**
   * Adds custom headers to the request builder.
   *
   * @param builder The HttpRequest.Builder
   * @param headers Map of headers to add
   * @return The same builder for method chaining
   */
  public static HttpRequest.Builder addCustomHeaders(
      HttpRequest.Builder builder, Map<String, String> headers) {
    if (headers != null) {
      headers.forEach(builder::header);
    }
    return builder;
  }
}
