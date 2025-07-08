package com.amil.rede.credenciadasimples.presentation.controller;

import com.amil.rede.credenciadasimples.application.usecase.IFindRedeCredenciadaByCpfUseCase;
import com.amil.rede.credenciadasimples.application.usecase.RedeCredenciadaFilter;
import com.amil.rede.credenciadasimples.application.usecase.RedeCredenciadaFilterBuilder;
import com.amil.rede.credenciadasimples.presentation.dto.ApiResponse;
import com.amil.rede.credenciadasimples.presentation.dto.RedeCredenciadaDto;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

/**
 * REST Controller for RedeCredenciada operations. Follows Clean Architecture by depending on use
 * cases instead of direct repository access. Uses Spring Boot for HTTP handling and async
 * processing.
 */
@RestController
@RequestMapping("/api/v1/rede-credenciada")
@CrossOrigin(origins = "*") // For demo purposes - configure properly in production
public class RedeCredenciadaRestController {

  private final IFindRedeCredenciadaByCpfUseCase findRedeCredenciadaByCpfUseCase;

  public RedeCredenciadaRestController(
      IFindRedeCredenciadaByCpfUseCase findRedeCredenciadaByCpfUseCase) {
    this.findRedeCredenciadaByCpfUseCase = findRedeCredenciadaByCpfUseCase;
  }

  /**
   * Finds rede credenciada by CPF.
   *
   * @param cpf The CPF to search for (should be 11 digits)
   * @return CompletableFuture with API response containing list of RedeCredenciadaDto
   */
  @GetMapping("/cpf/{cpf}")
  public CompletableFuture<ResponseEntity<ApiResponse<List<RedeCredenciadaDto>>>> findByCpf(
      @PathVariable String cpf, WebRequest request) {

    return findRedeCredenciadaByCpfUseCase
        .execute(cpf)
        .thenApply(
            redeCredenciadaList -> {
              List<RedeCredenciadaDto> dtoList =
                  redeCredenciadaList.stream().map(RedeCredenciadaDto::from).toList();

              String message =
                  String.format("Encontrados %d registros para CPF %s", dtoList.size(), cpf);

              ApiResponse<List<RedeCredenciadaDto>> response =
                  ApiResponse.success(message, dtoList);
              response.setPath(request.getDescription(false));

              return ResponseEntity.ok(response);
            })
        .exceptionally(
            throwable -> {
              String errorMessage =
                  "Erro ao buscar dados da rede credenciada: " + throwable.getMessage();

              ApiResponse<List<RedeCredenciadaDto>> errorResponse = ApiResponse.error(errorMessage);
              errorResponse.setPath(request.getDescription(false));

              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            });
  }

  /**
   * Finds rede credenciada by CPF with optional filters. Example:
   * /api/v1/rede-credenciada/cpf/01596670207?codigoRede=879&modalidade=SAUDE&unidade=Rio%20de%20Janeiro
   *
   * @param cpf The CPF to search for (should be 11 digits)
   * @param codigoRede Optional filter for codigoRede
   * @param modalidade Optional filter for modalidade
   * @param unidade Optional filter for unidade
   * @param numeroCarterinha Optional filter for numeroCarterinha
   * @param contexto Optional filter for contexto
   * @param request WebRequest for path information
   * @return CompletableFuture with API response containing filtered list of RedeCredenciadaDto
   */
  @GetMapping("/cpf/{cpf}/filters")
  public CompletableFuture<ResponseEntity<ApiResponse<List<RedeCredenciadaDto>>>>
      findByCpfWithFilters(
          @PathVariable String cpf,
          @RequestParam(required = false) Integer codigoRede,
          @RequestParam(required = false) String modalidade,
          @RequestParam(required = false) String unidade,
          @RequestParam(required = false) String numeroCarterinha,
          @RequestParam(required = false) String contexto,
          WebRequest request) {

    List<RedeCredenciadaFilter> filters =
        buildFilters(codigoRede, modalidade, unidade, numeroCarterinha, contexto);

    return findRedeCredenciadaByCpfUseCase
        .execute(cpf, filters)
        .thenApply(
            redeCredenciadaList -> {
              List<RedeCredenciadaDto> dtoList =
                  redeCredenciadaList.stream().map(RedeCredenciadaDto::from).toList();

              String message =
                  String.format(
                      "Encontrados %d registros para CPF %s com filtros aplicados",
                      dtoList.size(), cpf);

              ApiResponse<List<RedeCredenciadaDto>> response =
                  ApiResponse.success(message, dtoList);
              response.setPath(request.getDescription(false));

              return ResponseEntity.ok(response);
            })
        .exceptionally(
            throwable -> {
              String errorMessage =
                  "Erro ao buscar dados da rede credenciada: " + throwable.getMessage();

              ApiResponse<List<RedeCredenciadaDto>> errorResponse = ApiResponse.error(errorMessage);
              errorResponse.setPath(request.getDescription(false));

              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            });
  }

  /**
   * Builds filters from query parameters.
   *
   * @param codigoRede Optional codigoRede filter
   * @param modalidade Optional modalidade filter
   * @param unidade Optional unidade filter
   * @param numeroCarterinha Optional numeroCarterinha filter
   * @param contexto Optional contexto filter
   * @return List of filters to apply
   */
  private List<RedeCredenciadaFilter> buildFilters(
      Integer codigoRede,
      String modalidade,
      String unidade,
      String numeroCarterinha,
      String contexto) {

    return RedeCredenciadaFilterBuilder.create()
        .codigoRede(codigoRede)
        .modalidade(modalidade)
        .unidade(unidade)
        .numeroCarterinha(numeroCarterinha)
        .contexto(contexto)
        .build();
  }

  /**
   * Health check endpoint.
   *
   * @return Simple health status
   */
  @GetMapping("/health")
  public ResponseEntity<ApiResponse<String>> health() {
    ApiResponse<String> response =
        ApiResponse.success(
            "RedeCredenciada API está funcionando", "HTTP Client Java 17 + Spring Boot");
    return ResponseEntity.ok(response);
  }

  /**
   * Get API information.
   *
   * @return API information and capabilities
   */
  @GetMapping("/info")
  public ResponseEntity<ApiResponse<Object>> info() {
    var info =
        new Object() {
          public final String name = "Rede Credenciada API";
          public final String version = "1.0.0";
          public final String description =
              "API para consulta de rede credenciada Amil usando HTTP Client Java 17";
          public final String[] features = {
            "HTTP Client nativo Java 17",
            "Programação assíncrona com CompletableFuture",
            "Clean Architecture",
            "Princípios SOLID",
            "Spring Boot integration",
            "Validação de CPF",
            "Tratamento de erros",
            "Filtros flexíveis"
          };
          public final String[] endpoints = {
            "GET /api/v1/rede-credenciada/cpf/{cpf} - Buscar por CPF",
            "GET /api/v1/rede-credenciada/cpf/{cpf}/filters - Buscar por CPF com filtros",
            "GET /api/v1/rede-credenciada/health - Status da API",
            "GET /api/v1/rede-credenciada/info - Informações da API"
          };
          public final String[] filterParams = {
            "codigoRede - Filtro por código da rede (ex: 879)",
            "modalidade - Filtro por modalidade (ex: SAUDE)",
            "unidade - Filtro por unidade (ex: Rio de Janeiro)",
            "numeroCarterinha - Filtro por número da carteirinha",
            "contexto - Filtro por contexto (ex: AMIL)"
          };
        };

    ApiResponse<Object> response = ApiResponse.success("Informações da API", info);
    return ResponseEntity.ok(response);
  }
}
