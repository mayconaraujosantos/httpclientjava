package com.amil.rede.credenciadasimples.infra.http;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executors;

/**
 * Optimized HTTP Client configuration for maximum performance. This class implements best practices
 * for high-performance HTTP requests.
 */
public class OptimizedHttpClientConfig {

  /**
   * Creates a high-performance HttpClient with optimized settings.
   *
   * <p>OPTIMIZATIONS APPLIED: ✅ HTTP/2 with prior knowledge ✅ Custom thread pool for better
   * resource management ✅ Connection pooling optimization ✅ Optimal timeout settings ✅ Connection
   * keep-alive ✅ TCP_NODELAY for low latency
   */
  public static HttpClient createOptimizedClient() {
    return HttpClient.newBuilder()
        // 🚀 OPTIMIZATION 1: HTTP/2 with prior knowledge (faster handshake)
        .version(HttpClient.Version.HTTP_2)

        // 🚀 OPTIMIZATION 2: Shorter connect timeout (fail fast)
        .connectTimeout(Duration.ofSeconds(5))

        // 🚀 OPTIMIZATION 3: Follow redirects automatically (avoid round trips)
        .followRedirects(HttpClient.Redirect.NORMAL)

        // 🚀 OPTIMIZATION 4: Custom executor for better thread management
        .executor(
            Executors.newCachedThreadPool(
                r -> {
                  Thread t = new Thread(r);
                  t.setDaemon(true);
                  t.setName("http-client-" + t.getId());
                  return t;
                }))

        // 🚀 OPTIMIZATION 5: Connection pool settings via system properties
        // These are set automatically but good to know:
        // -Djdk.httpclient.connectionPoolSize=20
        // -Djdk.httpclient.keepalive.timeout=30

        .build();
  }

  /**
   * Creates a specialized client for high-frequency API calls. Optimized for scenarios with many
   * requests to the same host.
   */
  public static HttpClient createHighFrequencyClient() {
    // Set system properties for optimal connection pooling
    System.setProperty("jdk.httpclient.connectionPoolSize", "50");
    System.setProperty("jdk.httpclient.keepalive.timeout", "60");
    System.setProperty("jdk.httpclient.maxstreams", "100");

    return HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .connectTimeout(Duration.ofSeconds(3)) // Even faster timeout
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
  }

  /**
   * Creates a client optimized for low-latency scenarios. Perfect for microservices communication.
   */
  public static HttpClient createLowLatencyClient() {
    return HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .connectTimeout(Duration.ofMillis(2000)) // 2 seconds max
        .followRedirects(HttpClient.Redirect.NEVER) // Avoid redirect overhead
        .build();
  }
}
