package com.amil.rede.credenciadasimples.domain.model;

public record RedeCredenciada(
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
    String unidade) {}
