package com.amil.rede.credenciadasimples.presentation.dto;

import com.amil.rede.credenciadasimples.domain.model.RedeCredenciada;

/**
 * Data Transfer Object for RedeCredenciada responses. Follows DTO pattern to separate internal
 * domain models from API contracts.
 */
public record RedeCredenciadaDto(
    Integer codigoRede,
    String situacao,
    String nomePlanoCartao,
    String nomeDoPlano,
    String registroAns,
    String classificacao,
    String beneficiarioStatus,
    String contexto,
    String operadora,
    String modalidade,
    String numeroCarterinha,
    String unidade) {

  /**
   * Static factory method to create DTO from domain entity.
   *
   * @param rede The domain entity
   * @return DTO representation
   */
  public static RedeCredenciadaDto from(RedeCredenciada rede) {
    return new RedeCredenciadaDto(
        rede.codigoRede(),
        rede.situacao(),
        rede.nomePlanoCartao(),
        rede.nomeDoPlano(),
        rede.registroAns(),
        rede.classificacao(),
        rede.beneficiarioStatus(),
        rede.contexto(),
        rede.operadora(),
        rede.modalidade(),
        rede.numeroCarterinha(),
        rede.unidade());
  }
}
