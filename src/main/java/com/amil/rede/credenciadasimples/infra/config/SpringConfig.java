package com.amil.rede.credenciadasimples.infra.config;

import com.amil.rede.credenciadasimples.application.usecase.FindRedeCredenciadaByCpfUseCaseImpl;
import com.amil.rede.credenciadasimples.application.usecase.IFindRedeCredenciadaByCpfUseCase;
import com.amil.rede.credenciadasimples.domain.repository.RedeCredenciadaRepository;
import com.amil.rede.credenciadasimples.infra.http.HttpClientConfig;
import com.amil.rede.credenciadasimples.infra.mapper.RedeCredenciadaJsonMapper;
import com.amil.rede.credenciadasimples.infra.repository.HttpRedeCredenciadaRepository;
import java.net.http.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for dependency injection. Follows Clean Architecture principles with proper
 * separation of concerns.
 */
@Configuration
public class SpringConfig {

  /**
   * Creates and configures the HttpClient bean. Using the factory method from our HttpClientConfig.
   */
  @Bean
  public HttpClient httpClient() {
    return HttpClientConfig.createHttpClient();
  }

  /** Creates the JSON mapper bean. Singleton instance for JSON parsing operations. */
  @Bean
  public RedeCredenciadaJsonMapper redeCredenciadaJsonMapper() {
    return new RedeCredenciadaJsonMapper();
  }

  /** Creates the repository bean. Injects HttpClient and JsonMapper dependencies. */
  @Bean
  public RedeCredenciadaRepository redeCredenciadaRepository(
      HttpClient httpClient, RedeCredenciadaJsonMapper jsonMapper) {
    return new HttpRedeCredenciadaRepository(httpClient, jsonMapper);
  }

  /**
   * Creates the use case bean. Follows Clean Architecture by depending on repository abstraction.
   */
  @Bean
  public IFindRedeCredenciadaByCpfUseCase findRedeCredenciadaByCpfUseCase(
      RedeCredenciadaRepository repository) {
    return new FindRedeCredenciadaByCpfUseCaseImpl(repository);
  }
}
