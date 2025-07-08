package com.amil.rede.credenciadasimples.infra.mapper;

import com.amil.rede.credenciadasimples.domain.model.RedeCredenciada;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JSON Mapper for RedeCredenciada objects using native Java parsing. Follows Single Responsibility
 * Principle and encapsulates JSON mapping logic.
 */
public class RedeCredenciadaJsonMapper {

  private static final Pattern JSON_OBJECT_PATTERN = Pattern.compile("\\{[^{}]*\\}");
  private static final Pattern FIELD_PATTERN =
      Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\"([^\"]*)\"|([^,}]+))");

  /**
   * Maps JSON string to List of RedeCredenciada objects.
   *
   * @param json The JSON string to parse
   * @return List of RedeCredenciada objects
   * @throws RuntimeException if JSON parsing fails
   */
  public List<RedeCredenciada> mapToRedeCredenciadaList(String json) {
    try {
      List<RedeCredenciada> result = new ArrayList<>();
      Matcher objectMatcher = JSON_OBJECT_PATTERN.matcher(json);

      while (objectMatcher.find()) {
        String jsonObject = objectMatcher.group();
        RedeCredenciada redeCredenciada = parseJsonObject(jsonObject);
        result.add(redeCredenciada);
      }

      return result;
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse JSON response", e);
    }
  }

  private RedeCredenciada parseJsonObject(String jsonObject) {
    Integer codigoRede = null;
    String situacao = null;
    String nomePlanoCartao = null;
    String nomeDoPlano = null;
    String registroAns = null;
    String classificacao = null;
    String beneficiarioStatus = null;
    String contexto = null;
    String operadora = null;
    String modalidade = null;
    String numeroCarterinha = null;
    String unidade = null;

    Matcher fieldMatcher = FIELD_PATTERN.matcher(jsonObject);

    while (fieldMatcher.find()) {
      String fieldName = fieldMatcher.group(1);
      String fieldValue =
          fieldMatcher.group(3) != null ? fieldMatcher.group(3) : fieldMatcher.group(4);

      if (fieldValue != null && !"null".equals(fieldValue)) {
        switch (fieldName) {
          case "codigoRede":
            codigoRede = parseInteger(fieldValue);
            break;
          case "situacao":
            situacao = fieldValue;
            break;
          case "nomePlanoCartao":
            nomePlanoCartao = fieldValue;
            break;
          case "nomeDoPlano":
            nomeDoPlano = fieldValue;
            break;
          case "registroAns":
            registroAns = fieldValue;
            break;
          case "classificacao":
            classificacao = fieldValue;
            break;
          case "beneficiarioStatus":
            beneficiarioStatus = fieldValue;
            break;
          case "contexto":
            contexto = fieldValue;
            break;
          case "operadora":
            operadora = fieldValue;
            break;
          case "modalidade":
            modalidade = fieldValue;
            break;
          case "numeroCarterinha":
            numeroCarterinha = fieldValue;
            break;
          case "unidade":
            unidade = fieldValue;
            break;
        }
      }
    }

    return new RedeCredenciada(
        codigoRede,
        situacao,
        nomePlanoCartao,
        nomeDoPlano,
        registroAns,
        classificacao,
        beneficiarioStatus,
        contexto,
        operadora,
        modalidade,
        numeroCarterinha,
        unidade);
  }

  private Integer parseInteger(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
